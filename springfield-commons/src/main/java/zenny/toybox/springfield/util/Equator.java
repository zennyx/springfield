package zenny.toybox.springfield.util;

import org.springframework.lang.Nullable;

/**
 * An equation function, which determines equality between objects of type T.
 * <p>
 * It is the functional sibling of {@link java.util.Comparator}; {@link Equator}
 * is to {@link Object} as {@link java.util.Comparator} is to
 * {@link java.lang.Comparable}.
 *
 * @author Zenny Xu
 * @param <T> the types of object this {@link Equator} can evaluate
 */
public interface Equator<T> extends Hasher<T> {

  /**
   * Evaluates the two arguments for their equality.
   *
   * @param object1 the first object to be equated
   * @param object2 the second object to be equated
   * @return whether the two objects are equal
   */
  boolean equate(@Nullable T object1, @Nullable T object2);

  /**
   * Returns {@code true} if the arguments are equal to each other (i.e., both
   * arguments and their hash codes are equal) and {@code false} otherwise.
   *
   * @param object1 object1 the first object to be equated
   * @param object2 object2 the second object to be equated
   * @return whether the two objects are equal
   */
  default boolean equals(@Nullable T object1, @Nullable T object2) {
    return this.equate(object1, object2) && this.hash(object1) == this.hash(object2);
  }
}
