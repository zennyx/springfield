package zenny.toybox.springfield.keyvalue.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;

public class MapBasedKeyValueLoader<K, V> implements KeyValueLoader<K, V> {

  private final Map<? extends K, ? extends V> source;

  public MapBasedKeyValueLoader(Map<? extends K, ? extends V> source) {
    Assert.notNull(source, "Source must not be null");

    this.source = Collections.unmodifiableMap(source);
  }

  public static <K, V> MapBasedKeyValueLoaderBuilder<K, V> from(@Nullable Map<? extends K, ? extends V> source) {
    return new MapBasedKeyValueLoaderBuilder<>(source);
  }

  public static <K, V> MapBasedKeyValueLoaderBuilder<K, V> of(@Nullable K key, @Nullable V value) {
    return new MapBasedKeyValueLoaderBuilder<K, V>().append(key, value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<K, V> load() {
    return (Map<K, V>) this.source;
  }

  public static class MapBasedKeyValueLoaderBuilder<K, V>
      implements KeyValueLoader.Builder<MapBasedKeyValueLoaderBuilder<K, V>, K, V> {

    private Map<K, V> source = new HashMap<>();

    public MapBasedKeyValueLoaderBuilder() {
      this(null);
    }

    public MapBasedKeyValueLoaderBuilder(@Nullable Map<? extends K, ? extends V> source) {
      if (this.source != null) {
        this.source.putAll(source);
      }
    }

    public MapBasedKeyValueLoaderBuilder<K, V> append(@Nullable K key, @Nullable V value) {
      this.source.put(key, value);

      return this.self();
    }

    public MapBasedKeyValueLoaderBuilder<K, V> appendMore(@Nullable Map<? extends K, ? extends V> source) {
      this.source.putAll(source);

      return this.self();
    }

    public MapBasedKeyValueLoaderBuilder<K, V> remove(@Nullable Object key) {
      this.source.remove(key);

      return this.self();
    }

    public MapBasedKeyValueLoaderBuilder<K, V> clear() {
      this.source.clear();

      return this.self();
    }

    @Override
    public MapBasedKeyValueLoaderBuilder<K, V> self() {
      return this;
    }

    @Override
    public KeyValueLoader<K, V> build() {
      return new MapBasedKeyValueLoader<>(this.source);
    }
  }
}
