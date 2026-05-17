package zenny.toybox.springfield.messaging;

import java.lang.reflect.Method;

public interface MessageListenerFactory {

  MessageHandler createMessageHandler(Object bean, Method method, MessageListener annotation);
}
