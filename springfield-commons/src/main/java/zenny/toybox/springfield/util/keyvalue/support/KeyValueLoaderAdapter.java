package zenny.toybox.springfield.util.keyvalue.support;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.keyvalue.KeyValueLoader;

public class KeyValueLoaderAdapter<K, V> implements KeyValueLoader<K, V> {

  private final Supplier<Map<K, V>> supplier;

  public KeyValueLoaderAdapter(Supplier<Map<K, V>> supplier) {
    Assert.notNull(supplier, "Supplier must not be null");

    this.supplier = supplier;
  }

  @Nullable
  @Override
  public Map<K, V> load() {
    return this.supplier.get();
  }
}
