package zenny.toybox.springfield.messaging;

import java.util.Map;
import org.jspecify.annotations.Nullable;

public class GenericMessage<T> implements Message<T> {

  private final String channel;
  private final T payload;
  private final Map<String, Object> headers;
  private final @Nullable Runnable acknowledgeCallback;

  public GenericMessage(
      String channel,
      T payload,
      Map<String, Object> headers,
      @Nullable Runnable acknowledgeCallback) {
    this.channel = channel;
    this.payload = payload;
    this.headers = Map.copyOf(headers);
    this.acknowledgeCallback = acknowledgeCallback;
  }

  @Override
  public String getChannel() {
    return this.channel;
  }

  @Override
  public T getPayload() {
    return this.payload;
  }

  @Override
  public Map<String, Object> getHeaders() {
    return this.headers;
  }

  @Override
  public void acknowledge() {
    if (this.acknowledgeCallback != null) {
      this.acknowledgeCallback.run();
    }
  }
}
