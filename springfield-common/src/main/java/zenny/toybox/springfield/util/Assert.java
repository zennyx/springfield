package zenny.toybox.springfield.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Extension of Spring's {@link org.springframework.util.Assert} providing additional assertion
 * methods for validating collections and maps.
 *
 * <p>This class adds {@link #noNullElements} overloads that delegate to {@link
 * CollectionUtils#hasNullElements} for null-element detection, supporting both {@link Collection}
 * and {@link Map} targets with either a static message string or a lazy message supplier.
 *
 * <p>This class is not instantiable.
 *
 * @see org.springframework.util.Assert
 * @see CollectionUtils#hasNullElements(Collection)
 * @see CollectionUtils#hasNullElements(Map)
 */
public final class Assert extends org.springframework.util.Assert {

  /** Suppresses default constructor, ensuring non-instantiability. */
  private Assert() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Asserts that the given collection does not contain {@code null} elements.
   *
   * <p>If the collection itself is {@code null}, the assertion passes (a null collection is
   * considered to have no null elements). The check is performed by {@link
   * CollectionUtils#hasNullElements(Collection)}.
   *
   * @param collection the collection to check; may be {@code null}
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection contains any {@code null} elements
   * @see CollectionUtils#hasNullElements(Collection)
   */
  public static void noNullElements(@Nullable Collection<?> collection, String message) {
    if (CollectionUtils.hasNullElements(collection)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Asserts that the given collection does not contain {@code null} elements.
   *
   * <p>If the collection itself is {@code null}, the assertion passes (a null collection is
   * considered to have no null elements). The check is performed by {@link
   * CollectionUtils#hasNullElements(Collection)}.
   *
   * @param collection the collection to check; may be {@code null}
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the collection contains any {@code null} elements
   * @see CollectionUtils#hasNullElements(Collection)
   */
  public static void noNullElements(
      @Nullable Collection<?> collection, Supplier<String> messageSupplier) {
    if (CollectionUtils.hasNullElements(collection)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  /**
   * Asserts that the given map does not contain {@code null} values.
   *
   * <p>If the map itself is {@code null}, the assertion passes (a null map is considered to have no
   * null values). Only the values of the map are checked; {@code null} keys are allowed. The check
   * is performed by {@link CollectionUtils#hasNullElements(Map)}.
   *
   * @param map the map to check; may be {@code null}
   * @param message the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the map contains any {@code null} values
   * @see CollectionUtils#hasNullElements(Map)
   */
  public static void noNullElements(@Nullable Map<?, ?> map, String message) {
    if (CollectionUtils.hasNullElements(map)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Asserts that the given map does not contain {@code null} values.
   *
   * <p>If the map itself is {@code null}, the assertion passes (a null map is considered to have no
   * null values). Only the values of the map are checked; {@code null} keys are allowed. The check
   * is performed by {@link CollectionUtils#hasNullElements(Map)}.
   *
   * @param map the map to check; may be {@code null}
   * @param messageSupplier a supplier for the exception message to use if the assertion fails
   * @throws IllegalArgumentException if the map contains any {@code null} values
   * @see CollectionUtils#hasNullElements(Map)
   */
  public static void noNullElements(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
    if (CollectionUtils.hasNullElements(map)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  /**
   * Safely retrieves the message from the given supplier, returning {@code null} if the supplier
   * itself is {@code null}.
   *
   * @param messageSupplier the supplier to invoke; may be {@code null}
   * @return the supplied message, or {@code null} if the supplier is {@code null}
   */
  private static @Nullable String nullSafeGet(@Nullable Supplier<String> messageSupplier) {
    return messageSupplier != null ? messageSupplier.get() : null;
  }
}
