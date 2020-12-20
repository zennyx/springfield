package zenny.toybox.springfield.util.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValues {

  void refresh(String name);

  @Nullable
  <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType);

  @Nullable
  default Map<String, String> get(String name) {
    return this.get(name, String.class, String.class);
  }
}
