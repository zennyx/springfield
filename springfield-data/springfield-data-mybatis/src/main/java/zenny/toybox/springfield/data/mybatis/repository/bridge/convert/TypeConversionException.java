package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import zenny.toybox.springfield.data.mybatis.repository.bridge.BridgeException;

/**
 * Thrown on an unrecoverable problem encountered in the {@literal convert}
 * package or sub-packages.
 *
 * @author Zenny Xu
 */
@SuppressWarnings("serial")
public abstract class TypeConversionException extends BridgeException {

  /**
   * Create a new conversion exception.
   *
   * @param message the exception message
   */
  public TypeConversionException(String message) {
    super(message);
  }

  /**
   * Create a new conversion exception.
   *
   * @param message the exception message
   * @param cause the cause
   */
  public TypeConversionException(String message, Throwable cause) {
    super(message, cause);
  }

  interface TypeConverter<S, T> {

    ResolvedType<T> convert(ResolvedType<S> source);
  }
}
