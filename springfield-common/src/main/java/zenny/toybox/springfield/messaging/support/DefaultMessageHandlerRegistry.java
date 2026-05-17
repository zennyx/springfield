package zenny.toybox.springfield.messaging.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zenny.toybox.springfield.messaging.MessageHandler;
import zenny.toybox.springfield.messaging.MessageHandlerRegistry;

public class DefaultMessageHandlerRegistry implements MessageHandlerRegistry {

  private final Map<String, Map<String, List<MessageHandler>>> handlerMap = new HashMap<>();

  @Override
  public void register(MessageHandler handler) {
    this.handlerMap
        .computeIfAbsent(handler.getChannel(), k -> new HashMap<>())
        .computeIfAbsent(handler.getGroup(), k -> new ArrayList<>())
        .add(handler);
  }

  @Override
  public Map<String, Map<String, List<MessageHandler>>> getHandlerMap() {
    return Map.copyOf(this.handlerMap);
  }
}
