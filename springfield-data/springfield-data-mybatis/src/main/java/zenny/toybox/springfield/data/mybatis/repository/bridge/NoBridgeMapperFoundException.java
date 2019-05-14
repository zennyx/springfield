package zenny.toybox.springfield.data.mybatis.repository.bridge;

@SuppressWarnings("serial")
public class NoBridgeMapperFoundException extends BridgeException {

  public NoBridgeMapperFoundException(String msg) {
    super(msg);
  }
}
