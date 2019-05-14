package zenny.toybox.springfield.util;

import zenny.toybox.springfield.lang.Internal;

@Internal
public final class StringUtils extends org.springframework.util.StringUtils {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private StringUtils() {
    throw new Error("No instances");
  }
}
