package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public class EnumBasedKeyValueLoader<K, V, E extends Enum<E>> implements KeyValueLoader<K, V> {

  private final Class<E> enumClass;

  private final Function<E, K> keyIteratee;

  private final Function<E, V> valueIteratee;

  public EnumBasedKeyValueLoader(Class<E> enumClass, Function<E, K> keyIteratee, Function<E, V> valueIteratee) {
    Assert.isTrue(Enum.class.isAssignableFrom(enumClass), "EnumClass must be assigned to objects of Enum");
    Assert.notNull(keyIteratee, "KeyIteratee must not be null");
    Assert.notNull(valueIteratee, "ValueIteratee must not be null");

    this.enumClass = enumClass;
    this.keyIteratee = keyIteratee;
    this.valueIteratee = valueIteratee;
  }

  @Override
  @Nullable
  public Map<K, V> load() {
    return CollectionUtils.toMap(this.enumClass, this.keyIteratee, this.valueIteratee);
  }
}
