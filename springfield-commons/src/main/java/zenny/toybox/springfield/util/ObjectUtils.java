package zenny.toybox.springfield.util;

import zenny.toybox.springfield.lang.Internal;

@Internal
public final class ObjectUtils extends org.springframework.util.ObjectUtils {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private ObjectUtils() {
    throw new Error("No instances");
  }
}
