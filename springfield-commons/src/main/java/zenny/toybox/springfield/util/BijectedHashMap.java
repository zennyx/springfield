package zenny.toybox.springfield.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;

public class BijectedHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {

  private static final long serialVersionUID = -1657284938629009738L;

  private final Map<V, K> reverse;

  public BijectedHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    this.reverse = new HashMap<>(initialCapacity, loadFactor);
  }

  public BijectedHashMap(int initialCapacity) {
    super(initialCapacity);
    this.reverse = new HashMap<>(initialCapacity);
  }

  public BijectedHashMap() {
    super();
    this.reverse = new HashMap<>();
  }

  public BijectedHashMap(@Nullable Map<? extends K, ? extends V> m) {
    this();
    this.putAll(m);
  }

  @Override
  public V put(@Nullable K key, @Nullable V value) {
    return super.put(this.reverse.put(value, key), value);
  }

  @Override
  public V remove(@Nullable Object key) {
    V value = super.remove(key);
    this.reverse.remove(value);

    return value;
  }

  @Override
  public void putAll(@Nullable Map<? extends K, ? extends V> m) {
    if (CollectionUtils.isEmpty(m)) {
      return;
    }

    m.forEach((k, v) -> {
      this.put(k, v);
    });
  }

  @Override
  public void clear() {
    this.reverse.clear();
    super.clear();
  }

  @Override
  public Set<V> values() {
    return this.reverse.keySet();
  }

  public K removeValue(@Nullable Object value) {
    K key = this.reverse.get(value);
    this.remove(key);

    return key;
  }
}
