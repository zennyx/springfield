package zenny.toybox.springfield.util.keyvalue.support;

import zenny.toybox.springfield.util.keyvalue.KeyValueException;

@SuppressWarnings("serial")
public class NoKeyValueLoaderFoundException extends KeyValueException {

  public NoKeyValueLoaderFoundException(String msg) {
    super(msg);
  }
}
