package zenny.toybox.springfield.messaging.support;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import zenny.toybox.springfield.messaging.DispatchStrategy;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageHandler;

public class RandomDispatchStrategy implements DispatchStrategy {

  @Override
  public List<MessageHandler> select(List<MessageHandler> handlers, Message<?> message) {
    if (handlers.size() <= 1) {
      return handlers;
    }
    return List.of(handlers.get(ThreadLocalRandom.current().nextInt(handlers.size())));
  }
}
