package zenny.toybox.springfield.sqs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import zenny.toybox.springfield.messaging.GenericMessage;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessagePublisher;

public class SqsMessageReceiver {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private final SqsClient sqsClient;
  private final SqsMessagingConfig config;
  private final MessagePublisher messagePublisher;
  private final ScheduledExecutorService pollerExecutor;
  private final ScheduledExecutorService renewPool;
  private final Map<String, SqsMessagingConfig.ChannelProperties> channelMap;
  private final Semaphore inFlight;
  private final List<ScheduledFuture<?>> pollFutures = new ArrayList<>();

  public SqsMessageReceiver(
      SqsClient sqsClient, SqsMessagingConfig config, MessagePublisher messagePublisher) {
    this.sqsClient = sqsClient;
    this.config = config;
    this.messagePublisher = messagePublisher;
    this.pollerExecutor =
        Executors.newScheduledThreadPool(Math.max(config.getChannels().size(), 1));
    this.renewPool = Executors.newScheduledThreadPool(2);
    this.channelMap = config.getChannels();
    this.inFlight = new Semaphore(config.getMaxInFlight());
  }

  @PostConstruct
  public void start() {
    for (Map.Entry<String, SqsMessagingConfig.ChannelProperties> entry :
        this.channelMap.entrySet()) {
      String channel = entry.getKey();
      String queueUrl = entry.getValue().getQueueUrl();
      ScheduledFuture<?> future =
          this.pollerExecutor.scheduleWithFixedDelay(
              () -> this.pollChannel(channel, queueUrl),
              0,
              this.config.getPollDelaySeconds(),
              TimeUnit.SECONDS);
      this.pollFutures.add(future);
    }
    this.log.info("SqsMessageReceiver started, channels={}", this.channelMap.keySet());
  }

  protected void pollChannel(String channel, String queueUrl) {
    int available = this.inFlight.availablePermits();
    if (available <= 0) {
      return;
    }

    int fetchSize = Math.min(Math.min(this.config.getMaxFetchSize(), 10), available);
    ReceiveMessageRequest request =
        ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .waitTimeSeconds(this.config.getWaitTimeSeconds())
            .maxNumberOfMessages(fetchSize)
            .visibilityTimeout(this.config.getVisibilityTimeoutSeconds())
            .messageAttributeNames("All")
            .build();

    try {
      List<software.amazon.awssdk.services.sqs.model.Message> messages =
          this.sqsClient.receiveMessage(request).messages();
      for (software.amazon.awssdk.services.sqs.model.Message msg : messages) {
        if (!this.inFlight.tryAcquire()) {
          break;
        }
        this.processMessage(channel, queueUrl, msg);
      }
    } catch (Exception e) {
      this.log.error("failed to poll SQS channel={} queueUrl={}", channel, queueUrl, e);
    }
  }

  protected void processMessage(
      String channel, String queueUrl, software.amazon.awssdk.services.sqs.model.Message sqsMsg) {
    AtomicBoolean done = new AtomicBoolean(false);
    ScheduledFuture<?> renewTask =
        this.renewPool.scheduleAtFixedRate(
            () -> this.renewVisibility(queueUrl, sqsMsg.receiptHandle(), done),
            this.config.getRenewIntervalSeconds(),
            this.config.getRenewIntervalSeconds(),
            TimeUnit.SECONDS);

    try {
      Message<?> message =
          new GenericMessage<>(
              channel,
              sqsMsg.body(),
              convertAttributes(sqsMsg.messageAttributes()),
              () -> this.deleteMessage(queueUrl, sqsMsg.receiptHandle()));
      this.messagePublisher.publish(message);
      message.acknowledge();
    } catch (Exception e) {
      this.log.error(
          "failed to process SQS message channel={} messageId={} receiptHandle={}",
          channel,
          sqsMsg.messageId(),
          sqsMsg.receiptHandle() != null
              ? sqsMsg.receiptHandle().substring(0, Math.min(20, sqsMsg.receiptHandle().length()))
              : "null",
          e);
    } finally {
      done.set(true);
      renewTask.cancel(false);
      this.inFlight.release();
    }
  }

  protected void deleteMessage(String queueUrl, String receiptHandle) {
    try {
      this.sqsClient.deleteMessage(
          DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(receiptHandle).build());
    } catch (Exception e) {
      this.log.error(
          "failed to delete SQS message queueUrl={} receiptHandle={}", queueUrl, receiptHandle, e);
    }
  }

  protected void renewVisibility(String queueUrl, String receiptHandle, AtomicBoolean done) {
    if (done.get()) {
      return;
    }
    try {
      this.sqsClient.changeMessageVisibility(
          ChangeMessageVisibilityRequest.builder()
              .queueUrl(queueUrl)
              .receiptHandle(receiptHandle)
              .visibilityTimeout(this.config.getVisibilityTimeoutSeconds())
              .build());
    } catch (Exception e) {
      this.log.warn(
          "failed to renew visibility queueUrl={} receiptHandle={}", queueUrl, receiptHandle, e);
    }
  }

  @PreDestroy
  public void shutdown() {
    this.log.info("SqsMessageReceiver shutting down...");
    for (ScheduledFuture<?> future : this.pollFutures) {
      future.cancel(false);
    }
    this.pollerExecutor.shutdown();
    this.renewPool.shutdown();

    int waitSeconds = this.config.getVisibilityTimeoutSeconds() + 10;
    try {
      if (this.inFlight.tryAcquire(this.config.getMaxInFlight(), waitSeconds, TimeUnit.SECONDS)) {
        this.log.info("all in-flight messages completed");
        this.inFlight.release(this.config.getMaxInFlight());
      } else {
        this.log.warn("timeout waiting for in-flight messages to complete");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      this.log.warn("interrupted while waiting for in-flight messages");
    }

    try {
      if (!this.pollerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        this.pollerExecutor.shutdownNow();
      }
      if (!this.renewPool.awaitTermination(5, TimeUnit.SECONDS)) {
        this.renewPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      this.pollerExecutor.shutdownNow();
      this.renewPool.shutdownNow();
    }
    this.log.info("SqsMessageReceiver shutdown complete");
  }

  private static Map<String, Object> convertAttributes(
      Map<String, MessageAttributeValue> attributes) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, MessageAttributeValue> entry : attributes.entrySet()) {
      MessageAttributeValue attr = entry.getValue();
      if ("String".equals(attr.dataType()) || "Number".equals(attr.dataType())) {
        result.put(entry.getKey(), attr.stringValue());
      }
    }
    return result;
  }
}
