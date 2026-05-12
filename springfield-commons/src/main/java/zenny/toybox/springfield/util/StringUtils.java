package zenny.toybox.springfield.util;

/**
 * Extension of Spring's {@link org.springframework.util.StringUtils}, providing additional
 * conversion and manipulation capabilities.
 *
 * <p>This class is not instantiable.
 *
 * @see org.springframework.util.StringUtils
 */
public final class StringUtils extends org.springframework.util.StringUtils {

  /** Suppresses default constructor, ensuring non-instantiability. */
  private StringUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}
