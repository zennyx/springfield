package zenny.toybox.springfield.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.lang.Internal;

@Internal
public final class CollectionUtils extends org.springframework.util.CollectionUtils {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private CollectionUtils() {
    throw new Error("No instances");
  }

  public static boolean hasNullElements(@Nullable Collection<?> collection) {
    if (collection == null) {
      return false;
    }

    for (Object item : collection) {
      if (item == null) {
        return true;
      }
    }

    return false;
  }

  public static boolean hasNullElements(@Nullable Map<?, ?> map) {
    if (map == null) {
      return false;
    }

    for (Object item : map.values()) {
      if (item == null) {
        return true;
      }
    }

    return false;
  }

  @SafeVarargs
  @Nullable
  public static <A> A[] toArray(@Nullable A... array) {
    return array;
  }

  @Nullable
  public static <K extends Enum<K>, V> Map<K, V> toMap(@Nullable Class<K> enumClass,
      @Nullable Function<K, V> iteratee) {
    if (enumClass == null || iteratee == null) {
      return null;
    }

    K[] enumConstants = enumClass.getEnumConstants();
    if (enumConstants.length == 0) {
      return null;
    }

    Map<K, V> map = new EnumMap<>(enumClass);
    for (K key : enumConstants) {
      map.put(key, iteratee.apply(key));
    }

    return map;
  }

  @Nullable
  public static <E extends Enum<E>, K, V> Map<K, V> toMap(@Nullable Class<E> enumClass,
      @Nullable Function<E, K> keyIteratee, @Nullable Function<E, V> valueIteratee) {
    if (enumClass == null || keyIteratee == null || valueIteratee == null) {
      return null;
    }

    E[] enumConstants = enumClass.getEnumConstants();
    if (enumConstants.length == 0) {
      return null;
    }

    Map<K, V> map = new LinkedHashMap<>();
    for (E e : enumConstants) {
      map.put(keyIteratee.apply(e), valueIteratee.apply(e));
    }

    return map;
  }
}
