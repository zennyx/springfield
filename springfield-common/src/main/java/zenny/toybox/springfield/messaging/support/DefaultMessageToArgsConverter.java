package zenny.toybox.springfield.messaging.support;

import org.springframework.core.convert.converter.Converter;
import zenny.toybox.springfield.messaging.Message;

public class DefaultMessageToArgsConverter implements Converter<Message<?>, Object[]> {

  @Override
  public Object[] convert(Message<?> message) {
    return new Object[] {message.getPayload()};
  }
}
