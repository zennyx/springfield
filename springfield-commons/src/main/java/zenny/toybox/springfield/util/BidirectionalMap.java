package zenny.toybox.springfield.util;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.lang.Nullable;

/**
 * A bidirectional map is a map that preserves the uniqueness of its values as
 * well as that of its keys. This constraint enables bidirectional to support an
 * "inverse view", which is another bidirectional containing the same entries as
 * this bidirectional but with reversed keys and values.
 *
 * @author Zenny Xu
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of values maintained by this map
 * @see https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/BiMap.java
 * @see https://github.com/apache/commons-collections/blob/master/src/main/java/org/apache/commons/collections4/BidiMap.java
 */
public interface BidirectionalMap<K, V> extends Map<K, V> {
  // TODO Serialize/Unmodifiable
  @Nullable
  K getKey(@Nullable Object value);

  @Nullable
  K removeByValue(@Nullable Object value);

  @Override
  Set<V> values();

  BidirectionalMap<V, K> inverse();

  /**
   * @param value
   * @param defaultKey
   * @return
   */
  default K getOrDefaultKey(Object value, K defaultKey) {
    K k;
    return (k = getKey(value)) != null || containsValue(value) ? k : defaultKey;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replaceAll(java.util.function.BiFunction)
   */
  @Override
  default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    // TODO should override since the behavior of a BidirectionalMap is different
    // from a normal one
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
   */
  @Override
  default V putIfAbsent(K key, V value) {
    // TODO
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
   */
  @Override
  default boolean remove(Object key, Object value) {
    // TODO
    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replace(java.lang.Object, java.lang.Object,
   * java.lang.Object)
   */
  @Override
  default boolean replace(K key, V oldValue, V newValue) {
    // TODO
    return false;
  }

  /**
   * @param value
   * @param oldKey
   * @param newKey
   * @return
   */
  default boolean replaceByKey(V value, K oldKey, K newKey) {
    Object curKey = getKey(value);
    if (!Objects.equals(curKey, oldKey) || curKey == null && !containsValue(value)) {
      return false;
    }
    put(newKey, value);// TODO need confirm
    return true;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
   */
  @Override
  default V replace(K key, V value) {
    // TODO
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#computeIfAbsent(java.lang.Object,
   * java.util.function.Function)
   */
  @Override
  default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    // TODO
    return null;
  }

  /**
   * @param value
   * @param mappingFunction
   * @return
   */
  default K computeIfKeyAbsent(V value, Function<? super V, ? extends K> mappingFunction) {
    // TODO
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#computeIfPresent(java.lang.Object,
   * java.util.function.BiFunction)
   */
  @Override
  default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    // TODO
    return null;
  }

  /**
   * @param value
   * @param remappingFunction
   * @return
   */
  default K computeIfKeyPresent(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction) {
    // TODO
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
   */
  @Override
  default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    // TODO
    return null;
  }

  /**
   * @param value
   * @param remappingFunction
   * @return
   */
  default K computeKey(V value, BiFunction<? super V, ? super K, ? extends K> remappingFunction) {
    // TODO
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#merge(java.lang.Object, java.lang.Object,
   * java.util.function.BiFunction)
   */
  @Override
  default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    // TODO
    return null;
  }
}
