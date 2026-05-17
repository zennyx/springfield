package zenny.toybox.springfield.messaging.support;

import java.lang.reflect.Method;
import org.springframework.context.ApplicationContext;
import zenny.toybox.springfield.messaging.MessageHandler;
import zenny.toybox.springfield.messaging.MessageListener;
import zenny.toybox.springfield.messaging.MessageListenerFactory;

public class DefaultMessageListenerFactory implements MessageListenerFactory {

  private final ApplicationContext applicationContext;

  public DefaultMessageListenerFactory(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public MessageHandler createMessageHandler(
      Object bean, Method method, MessageListener annotation) {
    return new MessageListenerMethodAdapter(bean, method, annotation, this.applicationContext);
  }
}
