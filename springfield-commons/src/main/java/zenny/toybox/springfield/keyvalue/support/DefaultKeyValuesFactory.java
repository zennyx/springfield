package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueAssembler;
import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValueRefresher;
import zenny.toybox.springfield.keyvalue.KeyValues;
import zenny.toybox.springfield.keyvalue.KeyValuesFactory;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public class DefaultKeyValuesFactory implements KeyValuesFactory {

  private KeyValueAssembler assembler;

  private KeyValueRefresher refresher;

  public DefaultKeyValuesFactory() {
    this(null, null);
  }

  public DefaultKeyValuesFactory(@Nullable KeyValueAssembler assembler, @Nullable KeyValueRefresher refresher) {
    this.assembler = Optional.of(assembler).orElse(new DefaultKeyValueAssembler());
    this.refresher = Optional.of(refresher).orElse(new DefaultKeyValueRefresher());
  }

  @Override
  public KeyValues build(Map<String, KeyValueLoader<?, ?>> loaders) {
    return this.build(loaders, new InMemoryKeyValueHolder());
  }

  @Override
  public KeyValues build(KeyValueHolder holder) {
    return this.build(null, holder);
  }

  @Override
  public KeyValues build(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");
    if (loaders != null) {
      Assert.isTrue(!CollectionUtils.isEmpty(loaders) && !CollectionUtils.hasNullElements(loaders),
          "KeyValueLoaders must contain entries");
    } else {

      // Handle the scenario where users use no loader but put all key-values into the
      // holder
      Assert.isTrue(holder.size() > 0, "KeyValueHolder must contain entries");
    }

    if (loaders != null) {
      this.assembler.assamble(loaders, holder);
    }
    return new DefaultKeyValues(loaders, holder, this.refresher);
  }

  public void setAssembler(KeyValueAssembler assembler) {
    Assert.notNull(assembler, "KeyValueAssembler must not be null");

    this.assembler = assembler;
  }

  public void setRefresher(KeyValueRefresher refresher) {
    Assert.notNull(refresher, "KeyValueRefresher must not be null");

    this.refresher = refresher;
  }

  protected static class DefaultKeyValues implements KeyValues {

    /**
     * Logger used by this class. Available to subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final Map<String, KeyValueLoader<?, ?>> loaders;

    private final KeyValueHolder holder;

    private final KeyValueRefresher refresher;

    protected DefaultKeyValues(Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder,
        KeyValueRefresher refresher) {
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

      this.refresher.refresh(name, this.loaders, this.holder);
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
