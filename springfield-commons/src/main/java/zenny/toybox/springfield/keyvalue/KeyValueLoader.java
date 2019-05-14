package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface KeyValueLoader<K, V> {

  @Nullable
  Map<K, V> load();

  interface Builder<B extends Builder<B, K, V>, K, V> {

    B self();

    KeyValueLoader<K, V> build();
  }
}
