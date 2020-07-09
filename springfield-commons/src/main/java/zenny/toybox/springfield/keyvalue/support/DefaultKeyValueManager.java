package zenny.toybox.springfield.keyvalue.support;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;

public class DefaultKeyValueManager extends AbstractKeyValueManager {

  public DefaultKeyValueManager(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    super(loaders, holder);
  }

  @Override
  protected void storeKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    loaders.forEach((name, loader) -> holder.put(name, new KeyValueWrapper<>(loader)));

  }

  @Override
  protected void tryRefreshing(String name, Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    KeyValueLoader<?, ?> loader = loaders.get(name);
    if (loader == null) {
      throw new NoSuchLoaderException("No loader found with name: [" + name + "]");
    }

    holder.put(name, new KeyValueWrapper<>(loader));
  }

  private static final class KeyValueWrapper<K, V> implements KeyValueLoader<K, V>, Map<K, V> {

    private final KeyValueLoader<K, V> loader;

    private Optional<Map<K, V>> keyvalues;

    public KeyValueWrapper(KeyValueLoader<K, V> loader) {
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

    @Nullable
    @Override
    public Map<K, V> load() {
      return this.get().orElse(null);
    }

    private Optional<Map<K, V>> get() {
      if (this.keyvalues == null) {
        this.keyvalues = Optional.ofNullable(this.loader.load());
      }

      return this.keyvalues;
    }
  }
}
