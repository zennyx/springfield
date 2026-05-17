package zenny.toybox.springfield.messaging.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import zenny.toybox.springfield.messaging.MessageHandler;
import zenny.toybox.springfield.messaging.MessageHandlerRegistry;
import zenny.toybox.springfield.messaging.MessageListener;
import zenny.toybox.springfield.messaging.MessageListenerFactory;

public class MessageListenerMethodProcessor implements SmartInitializingSingleton {

  protected final Log log = LogFactory.getLog(this.getClass());

  private final ApplicationContext applicationContext;
  private final MessageHandlerRegistry registry;
  private final List<MessageListenerFactory> factories;

  public MessageListenerMethodProcessor(
      ApplicationContext applicationContext,
      List<MessageListenerFactory> factories,
      MessageHandlerRegistry registry) {
    this.applicationContext = applicationContext;
    this.factories = new ArrayList<>(factories);
    this.registry = registry;
  }

  @Override
  public void afterSingletonsInstantiated() {
    String[] beanNames = this.applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      Object bean = this.applicationContext.getBean(beanName);
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      Map<Method, MessageListener> annotatedMethods =
          MethodIntrospector.selectMethods(
              targetClass,
              (MethodIntrospector.MetadataLookup<MessageListener>)
                  method ->
                      AnnotatedElementUtils.findMergedAnnotation(method, MessageListener.class));
      for (Map.Entry<Method, MessageListener> entry : annotatedMethods.entrySet()) {
        Method method = entry.getKey();
        MessageListener annotation = entry.getValue();
        if (annotation == null) {
          continue;
        }
        MessageHandler handler = null;
        for (MessageListenerFactory factory : this.factories) {
          handler = factory.createMessageHandler(bean, method, annotation);
          if (handler != null) {
            break;
          }
        }
        if (handler == null) {
          throw new IllegalStateException("no MessageListenerFactory supports: " + method);
        }
        this.registry.register(handler);
        this.log.info(
            "registered @MessageListener channel="
                + annotation.channel()
                + " group="
                + annotation.group()
                + " method="
                + targetClass.getSimpleName()
                + "."
                + method.getName());
      }
    }
  }
}
