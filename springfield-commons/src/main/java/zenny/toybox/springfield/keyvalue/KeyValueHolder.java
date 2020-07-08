package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueHolder {

  @Nullable
  <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType);

  void put(String name, @Nullable Map<?, ?> keyValues);

  int size();
}
