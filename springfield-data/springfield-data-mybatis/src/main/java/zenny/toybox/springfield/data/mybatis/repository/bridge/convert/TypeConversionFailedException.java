package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

/**
 * Exception to be thrown when an actual type conversion attempt fails.
 *
 * @author Zenny Xu
 */
@SuppressWarnings("serial")
public class TypeConversionFailedException extends TypeConversionException {

  /**
   * The source type.
   */
  @Nullable
  private final Type sourceType;

  /**
   * Create a new conversion exception.
   *
   * @param sourceType the source type requested to convert from
   */
  public TypeConversionFailedException(@Nullable Type sourceType) {
    super("Failed to convert for type [" + sourceType + "]");
    this.sourceType = sourceType;
  }

  /**
   * Create a new conversion exception.
   *
   * @param sourceType the source type requested to convert from
   * @param cause the cause of the conversion failure
   */
  public TypeConversionFailedException(@Nullable Type sourceType, Throwable cause) {
    super("Failed to convert for type [" + sourceType + "]", cause);
    this.sourceType = sourceType;
  }

  /**
   * Return the source type that was requested to convert from.
   *
   * @return the source type that was requested to convert from
   */
  @Nullable
  public Type getSourceType() {
    return this.sourceType;
  }
}
