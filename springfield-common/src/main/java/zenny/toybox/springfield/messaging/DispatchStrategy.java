package zenny.toybox.springfield.messaging;

import java.util.List;

@FunctionalInterface
public interface DispatchStrategy {

  List<MessageHandler> select(List<MessageHandler> handlers, Message<?> message);
}
