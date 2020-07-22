package zenny.toybox.springfield.keyvalue.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public class CompositeKeyValueLoader<K, V> implements KeyValueLoader<K, V> {

  private final List<KeyValueLoader<K, V>> loaders;

  @SafeVarargs
  public CompositeKeyValueLoader(KeyValueLoader<K, V>... loaders) {
    this(Arrays.asList(loaders));
  }

  public CompositeKeyValueLoader(List<KeyValueLoader<K, V>> loaders) {
    Assert.isTrue(!CollectionUtils.isEmpty(loaders) && !CollectionUtils.hasNullElements(loaders),
        "KeyValueLoaders must contain entries");

    this.loaders = loaders;
  }

  @Nullable
  @Override
  public Map<K, V> load() {
    Map<K, V> result = new HashMap<>();
    this.loaders.forEach((l) -> {
      Optional.ofNullable(l.load()).ifPresent(r -> result.putAll(r));
    });

    return result.isEmpty() ? null : result;
  }
}
