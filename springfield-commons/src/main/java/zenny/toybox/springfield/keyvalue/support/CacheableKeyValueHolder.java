package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.util.Assert;

public class CacheableKeyValueHolder implements KeyValueHolder {

  public static final String CACHE_NAME = "key-values";

  public static final String CACHE_KEY = "#name";

  private final KeyValueHolder holder;

  public CacheableKeyValueHolder(KeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");

    this.holder = holder;
  }

  @Cacheable(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Nullable
  @Override
  public Map<?, ?> get(String name) {
    return this.holder.get(name);
  }

  @CachePut(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Override
  public void put(String name, @Nullable Map<?, ?> keyValues) {
    this.holder.put(name, keyValues);
  }

  @Override
  public int size() {
    return this.holder.size();
  }
}
