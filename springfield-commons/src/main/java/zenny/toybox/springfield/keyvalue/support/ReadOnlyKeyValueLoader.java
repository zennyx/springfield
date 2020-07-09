package zenny.toybox.springfield.keyvalue.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;

public class ReadOnlyKeyValueLoader<K, V> implements KeyValueLoader<K, V> {

  private final KeyValueLoader<K, V> loader;

  public ReadOnlyKeyValueLoader(KeyValueLoader<K, V> loader) {
    Assert.notNull(loader, "KeyValueLoader must not be null");

    this.loader = loader;
  }

  @Nullable
  @Override
  public Map<K, V> load() {
    Map<K, V> loaded = this.loader.load();

    if (this.loader != null) {
      loaded = Collections.unmodifiableMap(loaded);
    }
    return loaded;
  }
}
