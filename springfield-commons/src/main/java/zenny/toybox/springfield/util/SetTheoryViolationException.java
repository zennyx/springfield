package zenny.toybox.springfield.util;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public class SetTheoryViolationException extends NestedRuntimeException {

  public SetTheoryViolationException(String msg) {
    super(msg);
  }
}
