package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValues;
import zenny.toybox.springfield.keyvalue.KeyValuesFactory;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public class DefaultKeyValuesFactory implements KeyValuesFactory {

  @Override
  public KeyValues getKeyValues(Map<String, KeyValueLoader<?, ?>> loaders) {
    return this.getKeyValues(loaders, new InMemoryKeyValueHolder());
  }

  @Override
  public KeyValues getKeyValues(KeyValueHolder holder) {
    return this.getKeyValues(null, holder);
  }

  @Override
  public KeyValues getKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, @Nullable KeyValueHolder holder) {
    return this.getKeyValues(loaders, holder, null);
  }

  public KeyValues getKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, @Nullable KeyValueHolder holder,
      @Nullable Function<String, BiConsumer<Map<String, KeyValueLoader<?, ?>>, KeyValueHolder>> refresher) {

    this.check(loaders, holder);
    this.assamble(loaders, holder);
    refresher = Optional.of(refresher).orElse(this.getDefaultRefresher());

    return new DefaultKeyValues(loaders, holder, refresher);
  }

  protected void check(Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");

    if (loaders != null) {
      Assert.isTrue(!CollectionUtils.isEmpty(loaders) && !CollectionUtils.hasNullElements(loaders),
          "KeyValueLoaders must contain entries");
      Assert.isTrue(holder instanceof AbstractKeyValueHolder, "KeyValueHolder must be mutable");

      return;
    }

    // Handle the scenario where users use no loader but put all key-values into the
    // holder
    Assert.isTrue(!holder.isEmpty(), "KeyValueHolder must contain entries");
  }

  protected void assamble(Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    AbstractKeyValueHolder kvsHolder = (AbstractKeyValueHolder) holder;
    loaders.forEach((name, loader) -> kvsHolder.put(name, loader));
  }

  protected Function<String, BiConsumer<Map<String, KeyValueLoader<?, ?>>, KeyValueHolder>> getDefaultRefresher() {

    return n -> {
      return (l, h) -> {
        KeyValueLoader<?, ?> loader = l.get(n);
        if (loader == null) {
          throw new NoKeyValueLoaderFoundException("No loader found for name: [" + n + "]");
        }

        if (h instanceof AbstractKeyValueHolder) {
          ((AbstractKeyValueHolder) h).put(n, loader);
        }
      };
    };
  }

  protected static class DefaultKeyValues implements KeyValues {

    /**
     * Logger used by this class. Available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final Map<String, KeyValueLoader<?, ?>> loaders;

    private final KeyValueHolder holder;

    private final Function<String, BiConsumer<Map<String, KeyValueLoader<?, ?>>, KeyValueHolder>> refresher;

    protected DefaultKeyValues(Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder,
        Function<String, BiConsumer<Map<String, KeyValueLoader<?, ?>>, KeyValueHolder>> refresher) {
      this.loaders = loaders;
      this.holder = holder;
      this.refresher = refresher;
    }

    @Override
    public void refresh(String name) {
      Assert.hasText(name, "Name must not be empty");

      if (this.loaders == null) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("There is no KeyValueLoader available, unable to refresh");
        }

        return;
      }

      this.refresher.apply(name).accept(this.loaders, this.holder);
    }

    @Override
    public <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
      Assert.hasText(name, "Name must not be empty");
      Assert.notNull(keyType, "Key-type must not be null");
      Assert.notNull(valueType, "Value-type must not be null");

      return this.resolveRawValue(this.holder.get(name), keyType, valueType);
    }

    @SuppressWarnings("unchecked")
    protected <K, V> Map<K, V> resolveRawValue(Map<?, ?> raw, Class<K> keyType, Class<V> valueType) {
      return (Map<K, V>) raw;
    }
  }
}
