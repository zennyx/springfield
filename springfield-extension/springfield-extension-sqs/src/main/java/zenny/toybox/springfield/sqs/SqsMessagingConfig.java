package zenny.toybox.springfield.sqs;

import java.util.HashMap;
import java.util.Map;

public class SqsMessagingConfig {

  private String region = "us-east-1";
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private int waitTimeSeconds = 20;
  private int visibilityTimeoutSeconds = 60;
  private int renewIntervalSeconds = 20;
  private int maxInFlight = 1000;
  private int maxFetchSize = 10;
  private int maxRetries = 3;
  private int pollDelaySeconds = 1;
  private final Map<String, ChannelProperties> channels = new HashMap<>();

  public static class ChannelProperties {

    private String queueUrl;
    private boolean fifo;
    private String messageGroupId;

    public String getQueueUrl() {
      return this.queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
      this.queueUrl = queueUrl;
    }

    public boolean isFifo() {
      return this.fifo;
    }

    public void setFifo(boolean fifo) {
      this.fifo = fifo;
    }

    public String getMessageGroupId() {
      return this.messageGroupId;
    }

    public void setMessageGroupId(String messageGroupId) {
      this.messageGroupId = messageGroupId;
    }
  }

  public String getRegion() {
    return this.region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getAccessKey() {
    return this.accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return this.secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public int getWaitTimeSeconds() {
    return this.waitTimeSeconds;
  }

  public void setWaitTimeSeconds(int waitTimeSeconds) {
    this.waitTimeSeconds = waitTimeSeconds;
  }

  public int getVisibilityTimeoutSeconds() {
    return this.visibilityTimeoutSeconds;
  }

  public void setVisibilityTimeoutSeconds(int visibilityTimeoutSeconds) {
    this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
  }

  public int getRenewIntervalSeconds() {
    return this.renewIntervalSeconds;
  }

  public void setRenewIntervalSeconds(int renewIntervalSeconds) {
    this.renewIntervalSeconds = renewIntervalSeconds;
  }

  public int getMaxInFlight() {
    return this.maxInFlight;
  }

  public void setMaxInFlight(int maxInFlight) {
    this.maxInFlight = maxInFlight;
  }

  public int getMaxFetchSize() {
    return this.maxFetchSize;
  }

  public void setMaxFetchSize(int maxFetchSize) {
    this.maxFetchSize = maxFetchSize;
  }

  public int getMaxRetries() {
    return this.maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public int getPollDelaySeconds() {
    return this.pollDelaySeconds;
  }

  public void setPollDelaySeconds(int pollDelaySeconds) {
    this.pollDelaySeconds = pollDelaySeconds;
  }

  public Map<String, ChannelProperties> getChannels() {
    return this.channels;
  }
}
