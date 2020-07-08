package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.util.Assert;

public abstract class AbstractKeyValueHolder implements KeyValueHolder {

  @Nullable
  @Override
  public <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
    Assert.hasText(name, "Name must not be empty");

    Map<?, ?> raw = this.getRawKeyValues(name);
    if (raw == null) {
      return null;
    }

    return this.resolveRawKeyValues(raw, keyType, valueType);
  }

  @Nullable
  protected abstract Map<?, ?> getRawKeyValues(String name);

  @SuppressWarnings("unchecked")
  protected <K, V> Map<K, V> resolveRawKeyValues(Map<?, ?> raw, Class<K> keyType, Class<V> valueType) {
    return (Map<K, V>) raw;
  }
}
