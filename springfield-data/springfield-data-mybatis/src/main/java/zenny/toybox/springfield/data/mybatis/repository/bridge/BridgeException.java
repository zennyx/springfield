package zenny.toybox.springfield.data.mybatis.repository.bridge;

import org.springframework.core.NestedRuntimeException;

/**
 * Abstract superclass for all exceptions thrown in the {@literal bridge}
 * package or sub-packages.
 *
 * @author Zenny Xu
 */
@SuppressWarnings("serial")
public abstract class BridgeException extends NestedRuntimeException {

  /**
   * Create a new {@code BridgeException} with the specified message.
   *
   * @param msg the detail message
   */
  public BridgeException(String msg) {
    super(msg);
  }

  /**
   * Create a new {@code BridgeException} with the specified message and root
   * cause.
   *
   * @param msg the detail message
   * @param cause the root cause
   */
  public BridgeException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
