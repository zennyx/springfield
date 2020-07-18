package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKeyValueHolder extends AbstractKeyValueHolder {

  private final Map<String, Map<?, ?>> cache = new ConcurrentHashMap<>();

  @Override
  public Map<?, ?> get(String name) {
    return this.cache.get(name);
  }

  @Override
  public int size() {
    return this.cache.size();
  }

  @Override
  protected void doPut(String name, Map<?, ?> keyValues) {
    this.cache.put(name, keyValues);
  }
}
