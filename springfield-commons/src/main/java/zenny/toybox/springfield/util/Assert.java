package zenny.toybox.springfield.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

public final class Assert extends org.springframework.util.Assert {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private Assert() {
    throw new Error("No instances");
  }

  public static void noNullElements(@Nullable Collection<?> collection, String message) {
    if (CollectionUtils.hasNullElements(collection)) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void noNullElements(@Nullable Collection<?> collection, Supplier<String> messageSupplier) {
    if (CollectionUtils.hasNullElements(collection)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  public static void noNullElements(@Nullable Map<?, ?> map, String message) {
    if (CollectionUtils.hasNullElements(map)) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void noNullElements(@Nullable Map<?, ?> map, Supplier<String> messageSupplier) {
    if (CollectionUtils.hasNullElements(map)) {
      throw new IllegalArgumentException(nullSafeGet(messageSupplier));
    }
  }

  @Nullable
  private static String nullSafeGet(@Nullable Supplier<String> messageSupplier) {
    return messageSupplier != null ? messageSupplier.get() : null;
  }
}
