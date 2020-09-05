package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;

public class CacheableKeyValueHolder extends AbstractKeyValueHolder {

  public static final String CACHE_NAME = "key-values";

  public static final String CACHE_KEY = "#name";

  private final AbstractKeyValueHolder delegate;

  public CacheableKeyValueHolder(AbstractKeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");

    // Note:
    // 1. If a holder is decorated by this class, the decorated one must be
    // registered to the application-context
    // 2. Don't set the decorated one lazy again
    this.delegate = holder;
    this.delegate.setLazy(false);
  }

  @Cacheable(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Nullable
  @Override
  public Map<?, ?> get(String name) {
    return this.delegate.get(name);
  }

  @CachePut(cacheNames = CACHE_NAME, key = CACHE_KEY)
  @Override
  void put(String name, Map<?, ?> keyValues) {
    this.delegate.put(name, keyValues);
  }

  @Override
  public boolean isEmpty() {
    return this.delegate.isEmpty();
  }

  @Override
  public void setLazy(boolean lazy) {
    this.delegate.setLazy(false);
  }

  @Override
  protected Map<?, ?> doGet(String name) {
    return this.delegate.doGet(name);
  }

  @Override
  protected void doPut(String name, Map<?, ?> keyValues) {
    this.delegate.doPut(name, keyValues);
  }
}
