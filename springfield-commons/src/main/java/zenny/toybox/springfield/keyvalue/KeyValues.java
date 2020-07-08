package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;

public final class KeyValues {

  private final KeyValueManager manager;

  public KeyValues(KeyValueManager manager) {
    Assert.notNull(manager, "KeyValueManager must not be null");

    this.manager = manager;
  }

  @Nullable
  public Map<String, String> get(String name) {
    return this.get(name, String.class, String.class);
  }

  @Nullable
  public <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
    return this.manager.getHolder().get(name, keyType, valueType);
  }

  public void refresh(String name) {
    this.manager.refresh(name);
  }
}
