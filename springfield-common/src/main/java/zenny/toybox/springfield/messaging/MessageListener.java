package zenny.toybox.springfield.messaging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.convert.converter.Converter;
import zenny.toybox.springfield.messaging.support.DefaultMessageToArgsConverter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageListener {

  String channel();

  String group() default "";

  String condition() default "";

  Class<? extends Converter<Message<?>, Object[]>> converter() default
      DefaultMessageToArgsConverter.class;
}
