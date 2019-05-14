package zenny.toybox.springfield.data.mybatis.repository.bridge;

/**
 * Thrown if a {@code Class} file already exists.
 *
 * @author Zenny Xu
 */
@SuppressWarnings("serial")
public class ClassAlreadyExistsException extends BridgeException {

  /**
   * Create a new {@code ClassAlreadyExistsException} with the specified message.
   *
   * @param msg the detail message.
   */
  public ClassAlreadyExistsException(String msg) {
    super(msg);
  }
}
