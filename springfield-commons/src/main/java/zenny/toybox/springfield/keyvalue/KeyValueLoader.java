package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface KeyValueLoader<K, V> {

  @Nullable
  Map<K, V> load();
}