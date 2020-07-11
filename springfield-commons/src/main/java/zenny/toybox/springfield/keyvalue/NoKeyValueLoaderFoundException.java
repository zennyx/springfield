package zenny.toybox.springfield.keyvalue;

@SuppressWarnings("serial")
public class NoKeyValueLoaderFoundException extends KeyValueException {

  public NoKeyValueLoaderFoundException(String msg) {
    super(msg);
  }
}
