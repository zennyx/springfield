package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueManager {

  @Nullable
  KeyValueLoader<?, ?> getLoader(String name);

  KeyValueHolder getHolder();

  @Nullable
  <K, V> Map<K, V> getValue(String name, Class<K> keyType, Class<V> valueType);

  void refresh(String name);
}
