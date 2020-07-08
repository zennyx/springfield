package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;

public class InMemoryKeyValueHolder extends AbstractKeyValueHolder {

  private final Map<String, Map<?, ?>> cache = new ConcurrentHashMap<>();

  @Override
  public void put(String name, @Nullable Map<?, ?> keyValues) {
    Assert.hasText(name, "Name must not be empty");

    this.cache.put(name, this.resolveInput(keyValues));
  }

  @Override
  public int size() {
    return this.cache.size();
  }

  @Nullable
  @Override
  protected Map<?, ?> getRawKeyValues(String name) {
    return this.resolveOutput(this.cache.get(name));
  }

  @Nullable
  protected Map<?, ?> resolveInput(@Nullable Map<?, ?> keyValues) {
    if (keyValues != null && keyValues instanceof KeyValueLoader) {
      return ((KeyValueLoader<?, ?>) keyValues).load();
    }

    return keyValues;
  }

  @Nullable
  protected Map<?, ?> resolveOutput(@Nullable Map<?, ?> keyValues) {
    return keyValues;
  }
}
