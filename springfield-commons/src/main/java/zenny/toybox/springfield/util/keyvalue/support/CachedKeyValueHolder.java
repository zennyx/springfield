package zenny.toybox.springfield.util.keyvalue.support;

import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.keyvalue.KeyValueHolder;

public class CachedKeyValueHolder implements KeyValueHolder {

  public static final String CACHE_NAME = "key-values";

  public static final String CACHE_KEY = "#name";

  private final KeyValueHolder delegate;

  public CachedKeyValueHolder(KeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");
    Assert.isTrue(!(holder instanceof AbstractKeyValueHolder), "KeyValueHolder must be immutable");

    this.delegate = holder;
  }

  @Cacheable(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Nullable
  @Override
  public Map<?, ?> get(String name) {
    return this.delegate.get(name);
  }

  @Override
  public boolean isEmpty() {
    return this.delegate.isEmpty();
  }
}
