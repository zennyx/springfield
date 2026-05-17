package zenny.toybox.springfield.messaging.support;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.converter.Converter;
import zenny.toybox.springfield.messaging.Header;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageHandler;
import zenny.toybox.springfield.messaging.MessageListener;
import zenny.toybox.springfield.messaging.Payload;
import zenny.toybox.springfield.util.Assert;

public class MessageListenerMethodAdapter implements MessageHandler {

  protected final Log log = LogFactory.getLog(this.getClass());

  private enum ResolutionStrategy {
    ANNOTATION,
    CONVERTER
  }

  private final Object bean;
  private final Method method;
  private final String channel;
  private final String group;
  private final String condition;
  private final Converter<Message<?>, Object[]> converter;
  private final MessageConditionEvaluator conditionEvaluator;
  private final ResolutionStrategy strategy;

  public MessageListenerMethodAdapter(
      Object bean,
      Method method,
      MessageListener annotation,
      ApplicationContext applicationContext) {
    Assert.state(
        Modifier.isPublic(method.getModifiers()),
        () -> "@MessageListener method must be public: " + method);
    this.bean = bean;
    this.method = method;
    this.channel = annotation.channel();
    this.group = annotation.group();
    this.condition = annotation.condition();
    this.conditionEvaluator = new MessageConditionEvaluator();

    if (this.hasAnnotationBasedParameters(method)) {
      this.strategy = ResolutionStrategy.ANNOTATION;
      this.converter = null;
    } else {
      this.strategy = ResolutionStrategy.CONVERTER;
      this.converter = this.resolveConverter(annotation.converter(), applicationContext);
    }
  }

  protected boolean hasAnnotationBasedParameters(Method method) {
    for (java.lang.reflect.Parameter param : method.getParameters()) {
      if (AnnotatedElementUtils.hasAnnotation(param, Payload.class)
          || AnnotatedElementUtils.hasAnnotation(param, Header.class)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  protected Converter<Message<?>, Object[]> resolveConverter(
      Class<? extends Converter<Message<?>, Object[]>> converterType, ApplicationContext context) {
    try {
      return context.getBean(converterType);
    } catch (Exception noBean) {
      try {
        var ctor = converterType.getDeclaredConstructor();
        Assert.state(
            Modifier.isPublic(ctor.getModifiers()),
            () -> "converter no-arg constructor must be public: " + converterType.getName());
        return ctor.newInstance();
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException(
            "converter bean not found and no public no-arg constructor: " + converterType.getName(),
            e);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(
            "failed to instantiate converter: " + converterType.getName(), e);
      }
    }
  }

  @Override
  public String getChannel() {
    return this.channel;
  }

  @Override
  public String getGroup() {
    return this.group;
  }

  @Override
  public void handle(Message<?> message) {
    if (!this.passCondition(message)) {
      if (this.log.isDebugEnabled()) {
        this.log.debug(
            "condition not met, skipping handler channel="
                + this.channel
                + " group="
                + this.group
                + " method="
                + this.method.getName());
      }
      return;
    }
    this.beforeHandle(message);
    Object[] args =
        (this.strategy == ResolutionStrategy.ANNOTATION)
            ? this.resolveByAnnotations(message)
            : this.converter.convert(message);
    Assert.isTrue(
        args != null && args.length == this.method.getParameterCount(),
        () ->
            "args length mismatch, expected="
                + this.method.getParameterCount()
                + ", actual="
                + (args == null ? 0 : args.length));
    try {
      if (this.log.isTraceEnabled()) {
        this.log.trace(
            "invoking handler channel="
                + this.channel
                + " group="
                + this.group
                + " method="
                + this.method.getName());
      }
      this.method.invoke(this.bean, args);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("invoke message handler failed: " + this.method, e);
    }
    this.afterHandle(message);
  }

  protected void beforeHandle(Message<?> message) {}

  protected void afterHandle(Message<?> message) {}

  protected Object[] resolveByAnnotations(Message<?> message) {
    java.lang.reflect.Parameter[] parameters = this.method.getParameters();
    Object[] args = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      java.lang.reflect.Parameter param = parameters[i];

      Payload payload = AnnotatedElementUtils.findMergedAnnotation(param, Payload.class);
      Header header = AnnotatedElementUtils.findMergedAnnotation(param, Header.class);

      if (payload != null) {
        String path = payload.value();
        if (path.isEmpty()) {
          args[i] = message.getPayload();
        } else {
          BeanWrapperImpl wrapper = new BeanWrapperImpl(message.getPayload());
          args[i] = wrapper.getPropertyValue(path);
        }
      } else if (header != null) {
        args[i] = message.getHeaders().get(header.value());
      } else {
        Assert.isTrue(
            param.getType().isInstance(message.getPayload()),
            () ->
                "cannot resolve parameter '"
                    + param.getName()
                    + "': no @Payload/@Header and type mismatch with payload");
        args[i] = message.getPayload();
      }
    }
    return args;
  }

  protected boolean passCondition(Message<?> message) {
    if (this.condition.isEmpty()) {
      return true;
    }
    return this.conditionEvaluator.evaluate(this.condition, message);
  }
}
