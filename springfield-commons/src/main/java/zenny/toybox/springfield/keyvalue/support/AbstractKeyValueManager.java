package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValueManager;
import zenny.toybox.springfield.keyvalue.NoKeyValueLoaderFoundException;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public abstract class AbstractKeyValueManager implements KeyValueManager {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  @Nullable
  private final Map<String, KeyValueLoader<?, ?>> loaders;

  private final KeyValueHolder holder;

  private final KeyValueHolder proxy;

  public AbstractKeyValueManager(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    Assert.notNull(holder, "KeyValueHolder must not be null");
    if (loaders != null) {
      Assert.isTrue(!CollectionUtils.isEmpty(loaders) && !CollectionUtils.hasNullElements(loaders),
          "KeyValueLoaders must contain entries");
    } else {

      // Handle the scenario where users use no loader but put all key-values into the
      // holder
      Assert.isTrue(holder.size() > 0, "KeyValueHolder must contain entries");
    }

    this.loaders = loaders;
    this.holder = this.resolveHolder(holder);
    this.proxy = new KeyValueHolderProxy(this.holder);

    if (this.loaders != null) {
      this.storeKeyValues(loaders, holder);
    }
  }

  @Nullable
  @Override
  public KeyValueLoader<?, ?> getLoader(String name) {
    Assert.hasText(name, "Name must not be empty");

    if (this.loaders == null) {
      return null;
    }

    KeyValueLoader<?, ?> loader = this.loaders.get(name);
    if (loader == null) {
      throw new NoKeyValueLoaderFoundException("No loader found with name: [" + name + "]");
    }

    return loader;
  }

  @Override
  public KeyValueHolder getHolder() {
    return this.proxy;
  }

  @Override
  @Nullable
  public <K, V> Map<K, V> getValue(String name, Class<K> keyType, Class<V> valueType) {
    Assert.hasText(name, "Name must not be empty");
    Assert.notNull(keyType, "");
    Assert.notNull(valueType, "");

    return this.resolveRawValue(this.holder.get(name), keyType, valueType);
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

    this.tryRefreshing(name, this.loaders, this.holder);

  }

  protected KeyValueHolder resolveHolder(KeyValueHolder holder) {
    if (holder instanceof KeyValueSourceHolder) {
      return holder;
    }

    return new KeyValueSourceHolder(holder);
  }

  @SuppressWarnings("unchecked")
  protected <K, V> Map<K, V> resolveRawValue(Map<?, ?> raw, Class<K> keyType, Class<V> valueType) {
    return (Map<K, V>) raw;
  }

  protected abstract void storeKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder);

  protected abstract void tryRefreshing(String name, Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder);

  private static final class KeyValueHolderProxy implements KeyValueHolder {

    private final KeyValueHolder holder;

    public KeyValueHolderProxy(KeyValueHolder holder) {
      this.holder = holder;
    }

    @Override
    public Map<?, ?> get(String name) {
      return this.holder.get(name);
    }

    @Override
    public void put(String name, Map<?, ?> source) {
      // Do nothing
    }

    @Override
    public int size() {
      return this.holder.size();
    }
  }
}
