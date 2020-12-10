package zenny.toybox.springfield.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;

@Deprecated
public class BijectionHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {

  private static final long serialVersionUID = -1657284938629009738L;

  private final Map<V, K> mirror;

  public BijectionHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    this.mirror = new HashMap<>(initialCapacity, loadFactor);
  }

  public BijectionHashMap(int initialCapacity) {
    super(initialCapacity);
    this.mirror = new HashMap<>(initialCapacity);
  }

  public BijectionHashMap() {
    super();
    this.mirror = new HashMap<>();
  }

  public BijectionHashMap(@Nullable Map<? extends K, ? extends V> m) {
    this();
    this.putAll(m);
  }

  @Override
  public V put(@Nullable K key, @Nullable V value) {
    if (super.containsKey(key) && this.mirror.containsKey(value)) {
      return value;
    }

    if (super.containsKey(key)) {
      this.mirror.remove(super.get(key));
    } else if (this.mirror.containsKey(value)) {
      super.remove(key);
    }

    return super.put(this.mirror.put(value, key), value);
  }

  @Override
  public V remove(@Nullable Object key) {
    V value = super.remove(key);
    this.mirror.remove(value);

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
    this.mirror.clear();
    super.clear();
  }

  @Override
  public Set<V> values() {
    return this.mirror.keySet();
  }

  public K removeValue(@Nullable Object value) {
    K key = this.mirror.get(value);
    this.remove(key);

    return key;
  }
}
