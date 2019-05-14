package zenny.toybox.springfield.keyvalue.support;

import java.util.function.Function;

public class SimpleEnumBasedKeyValueLoader<E extends Enum<E>> extends EnumBasedKeyValueLoader<Integer, String, E> {

  public SimpleEnumBasedKeyValueLoader(Class<E> enumClass) {
    super(enumClass, toKeyIteratee(), toValueIteratee());
  }

  private static <E extends Enum<E>> Function<E, Integer> toKeyIteratee() {
    return (envm) -> {
      return envm.ordinal();
    };
  }

  private static <E extends Enum<E>> Function<E, String> toValueIteratee() {
    return (envm) -> {
      return envm.toString();
    };
  }
}
