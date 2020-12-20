package zenny.toybox.springfield.util.keyvalue.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.keyvalue.KeyValueLoader;

abstract class KeyValueHolderSupport extends MutableKeyValueHolder {

  private boolean lazy = true;

  @Nullable
  @Override
  public Map<?, ?> get(String name) {
    Assert.hasText(name, "Name must not be empty");

    Map<?, ?> keyValues = this.doGet(name);

    if (keyValues != null && keyValues instanceof KeyValueSource) {
      keyValues = ((KeyValueSource<?, ?>) keyValues).get().orElse(null);
    }
    return keyValues;
  }

  @Override
  void put(String name, @Nullable Map<?, ?> keyValues) {
    Assert.hasText(name, "Name must not be empty");

    if (keyValues == null || keyValues instanceof KeyValueSource) {
      this.doPut(name, keyValues);
      return;
    }

    this.doPut(name, Collections.unmodifiableMap(keyValues));
  }

  void put(String name, @Nullable KeyValueLoader<?, ?> loader) {
    this.put(name, Optional.ofNullable(loader).map((l) -> {
      return this.lazy ? new KeyValueSource<>(l) : l.load();
    }).orElse(null));
  }

  public void setLazy(boolean lazy) {
    this.lazy = lazy;
  }

  protected abstract Map<?, ?> doGet(String name);

  protected abstract void doPut(String name, @Nullable Map<?, ?> keyValues);

  static class KeyValueSource<K, V> implements Map<K, V> {

    private final KeyValueLoader<K, V> loader;

    private Optional<Map<K, V>> keyvalues;

    public KeyValueSource(KeyValueLoader<K, V> loader) {
      this.loader = loader;
    }

    @Override
    public int size() {
      return this.get().map(v -> v.size()).orElse(0);
    }

    @Override
    public boolean isEmpty() {
      return this.get().map(v -> v.isEmpty()).orElse(true);
    }

    @Override
    public boolean containsKey(Object key) {
      return this.get().map(v -> v.containsKey(key)).orElse(false);
    }

    @Override
    public boolean containsValue(Object value) {
      return this.get().map(v -> v.containsValue(value)).orElse(false);
    }

    @Nullable
    @Override
    public V get(Object key) {
      return this.get().map(v -> v.get(key)).orElse(null);
    }

    @Nullable
    @Override
    public V put(K key, @Nullable V value) {
      return this.get().map(v -> v.put(key, value)).orElse(null);
    }

    @Nullable
    @Override
    public V remove(Object key) {
      return this.get().map(v -> v.remove(key)).orElse(null);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
      this.get().ifPresent(v -> v.putAll(m));
    }

    @Override
    public void clear() {
      this.get().ifPresent(v -> v.clear());
    }

    @Nullable
    @Override
    public Set<K> keySet() {
      return this.get().map(v -> v.keySet()).orElse(null);
    }

    @Nullable
    @Override
    public Collection<V> values() {
      return this.get().map(v -> v.values()).orElse(null);
    }

    @Nullable
    @Override
    public Set<Entry<K, V>> entrySet() {
      return this.get().map(v -> v.entrySet()).orElse(null);
    }

    Optional<Map<K, V>> get() {
      if (this.keyvalues == null) {
        this.keyvalues = Optional.ofNullable(this.loader.load());
      }

      return this.keyvalues;
    }
  }
}
