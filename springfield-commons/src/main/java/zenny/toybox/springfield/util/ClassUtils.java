package zenny.toybox.springfield.util;

import zenny.toybox.springfield.lang.Internal;

@Internal
public final class ClassUtils extends org.springframework.util.ClassUtils {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private ClassUtils() {
    throw new Error("No instances");
  }
}
