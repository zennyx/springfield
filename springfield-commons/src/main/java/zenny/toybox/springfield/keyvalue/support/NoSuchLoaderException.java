package zenny.toybox.springfield.keyvalue.support;

import zenny.toybox.springfield.keyvalue.KeyValueException;

@SuppressWarnings("serial")
public class NoSuchLoaderException extends KeyValueException {

  public NoSuchLoaderException(String msg) {
    super(msg);
  }
}
