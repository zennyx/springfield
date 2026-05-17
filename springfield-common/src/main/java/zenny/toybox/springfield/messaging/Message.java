package zenny.toybox.springfield.messaging;

import java.util.Map;

public interface Message<T> {

  String getChannel();

  T getPayload();

  Map<String, Object> getHeaders();

  void acknowledge();
}
