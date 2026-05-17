package zenny.toybox.springfield.messaging.support;

import java.util.List;
import zenny.toybox.springfield.messaging.DispatchStrategy;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageHandler;

public class BroadcastDispatchStrategy implements DispatchStrategy {

  @Override
  public List<MessageHandler> select(List<MessageHandler> handlers, Message<?> message) {
    return handlers;
  }
}
