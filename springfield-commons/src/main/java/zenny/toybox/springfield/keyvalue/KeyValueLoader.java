package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.HierarchicalBuilder;

@FunctionalInterface
public interface KeyValueLoader<K, V> {

  @Nullable
  Map<K, V> load();

  interface Builder<K, V> extends HierarchicalBuilder<KeyValueLoader<K, V>, Builder<K, V>> {

  }
}
