package zenny.toybox.springfield.keyvalue.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;

public class CacheableKeyValueHolder extends KeyValueSourceHolder {

  public static final String CACHE_NAME = "key-values";

  public static final String CACHE_KEY = "#name";

  public CacheableKeyValueHolder(KeyValueHolder holder) {
    super(holder);
  }

  @Override
  public void put(String name, @Nullable Map<?, ?> keyValues) {
    super.put(name, keyValues);

    if (keyValues != null) {
      keyValues = keyValues instanceof KeyValueSource ? ((KeyValueSource<?, ?>) keyValues).get().orElse(null)
          : Collections.unmodifiableMap(keyValues);
    }
    this.putForCaching(name, keyValues);
  }

  @Cacheable(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Nullable
  @Override
  public Map<?, ?> get(String name) {
    Map<?, ?> keyValues = super.get(name);

    if (keyValues != null && keyValues instanceof KeyValueSource) {
      keyValues = ((KeyValueSource<?, ?>) keyValues).get().orElse(null);
    }
    return keyValues;
  }

  @CachePut(cacheNames = CACHE_NAME, key = CACHE_KEY)
  protected void putForCaching(String name, @Nullable Map<?, ?> keyValues) {
    // Do nothing
  }
}
