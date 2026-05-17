package zenny.toybox.springfield.util.keyvalue;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public abstract class KeyValueException extends NestedRuntimeException {

  public KeyValueException(String msg) {
    super(msg);
  }

  public KeyValueException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
