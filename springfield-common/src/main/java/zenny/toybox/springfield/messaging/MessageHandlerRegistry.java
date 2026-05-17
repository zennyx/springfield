package zenny.toybox.springfield.messaging;

import java.util.List;
import java.util.Map;

public interface MessageHandlerRegistry {

  void register(MessageHandler handler);

  Map<String, Map<String, List<MessageHandler>>> getHandlerMap();
}
