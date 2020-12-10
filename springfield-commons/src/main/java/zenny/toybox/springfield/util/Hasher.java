package zenny.toybox.springfield.util;

import org.springframework.lang.Nullable;

/**
 * A hash function, which calculates the hash for the object specified.
 *
 * @author Zenny Xu
 * @param <T> the type of the object
 */
@FunctionalInterface
public interface Hasher<T> {

  /**
   * Gets the hash code for the object specified.
   *
   * @param object the target to get a hash code for
   * @return the hash code
   */
  int hash(@Nullable T object);
}
