package zenny.toybox.springfield.keyvalue.support;

import zenny.toybox.springfield.keyvalue.KeyValueException;

@SuppressWarnings("serial")
public class NoKeyValueLoaderFoundException extends KeyValueException {

  public NoKeyValueLoaderFoundException(String msg) {
    super(msg);
  }
}
