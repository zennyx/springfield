package zenny.toybox.springfield.util.keyvalue.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public class InMemoryKeyValueHolder extends AbstractKeyValueHolder {

  private final Map<String, Map<?, ?>> cache = new ConcurrentHashMap<>();

  public InMemoryKeyValueHolder(Map<String, Map<?, ?>> source) {
    Assert.isTrue(source != null && !CollectionUtils.isEmpty(source), "Source must not be empty");

    this.cache.putAll(source);
  }

  public InMemoryKeyValueHolder() {
  }

  @Override
  public boolean isEmpty() {
    return this.cache.isEmpty();
  }

  @Override
  protected Map<?, ?> doGet(String name) {
    return this.cache.get(name);
  }

  @Override
  protected void doPut(String name, Map<?, ?> keyValues) {
    this.cache.put(name, keyValues);
  }
}
