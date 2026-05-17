package zenny.toybox.springfield.sqs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import zenny.toybox.springfield.messaging.Message;

public class SqsMessageSender {

  private final SqsClient sqsClient;
  private final SqsMessagingConfig config;

  public SqsMessageSender(SqsClient sqsClient, SqsMessagingConfig config) {
    this.sqsClient = sqsClient;
    this.config = config;
  }

  public void send(Message<?> message) {
    SqsMessagingConfig.ChannelProperties channelProps =
        this.config.getChannels().get(message.getChannel());
    if (channelProps == null || channelProps.getQueueUrl() == null) {
      throw new IllegalArgumentException(
          "no queue-url mapping for channel: " + message.getChannel());
    }
    Map<String, MessageAttributeValue> attrs = toMessageAttributes(message.getHeaders());
    var requestBuilder =
        SendMessageRequest.builder()
            .queueUrl(channelProps.getQueueUrl())
            .messageBody(String.valueOf(message.getPayload()))
            .messageAttributes(attrs);
    if (channelProps.isFifo()) {
      String groupId =
          channelProps.getMessageGroupId() != null ? channelProps.getMessageGroupId() : "default";
      requestBuilder.messageGroupId(groupId);
      requestBuilder.messageDeduplicationId(UUID.randomUUID().toString());
    }
    this.sqsClient.sendMessage(requestBuilder.build());
  }

  private static Map<String, MessageAttributeValue> toMessageAttributes(
      Map<String, Object> headers) {
    Map<String, MessageAttributeValue> attrs = new HashMap<>();
    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof String s) {
        attrs.put(
            entry.getKey(),
            MessageAttributeValue.builder().dataType("String").stringValue(s).build());
      } else if (value instanceof Number n) {
        attrs.put(
            entry.getKey(),
            MessageAttributeValue.builder().dataType("Number").stringValue(n.toString()).build());
      }
    }
    return attrs;
  }
}
