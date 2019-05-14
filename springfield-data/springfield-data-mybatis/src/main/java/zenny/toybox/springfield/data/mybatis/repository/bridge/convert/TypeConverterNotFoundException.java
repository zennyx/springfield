package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

/**
 * Exception to be thrown when a suitable type-converter could not be found in a
 * given conversion service.
 *
 * @author Zenny Xu
 */
@SuppressWarnings("serial")
public class TypeConverterNotFoundException extends TypeConversionException {

  /**
   * The source type.
   */
  @Nullable
  private final Type sourceType;

  /**
   * Create a new type conversion executor not found exception.
   *
   * @param sourceType the source type requested to convert from
   */
  public TypeConverterNotFoundException(@Nullable Type sourceType) {
    super("No type converter found capable of converting for type [" + sourceType + "]");
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
