package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;

public class CacheableKeyValueHolder extends InMemoryKeyValueHolder {

  public static final String CACHE_NAME = "key-values";

  public static final String CACHE_KEY = "#name";

  @Override
  public void put(String name, @Nullable Map<?, ?> keyValues) {
    super.put(name, keyValues);
    this.putForCache(name, super.resolveInput(keyValues));
  }

  @CachePut(cacheNames = CACHE_NAME, key = CACHE_KEY)
  protected void putForCache(String name, @Nullable Map<?, ?> keyValues) {
    // Do nothing
  }

  @Cacheable(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Nullable
  @Override
  protected Map<?, ?> getRawKeyValues(String name) {
    return super.getRawKeyValues(name);
  }

  @Nullable
  @Override
  protected Map<?, ?> resolveInput(@Nullable Map<?, ?> keyValues) {
    return super.resolveOutput(keyValues);
  }

  @Nullable
  @Override
  protected Map<?, ?> resolveOutput(@Nullable Map<?, ?> keyValues) {
    return super.resolveInput(keyValues);
  }
}
