package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.NoKeyValueLoaderFoundException;

public class DefaultKeyValueManager extends AbstractKeyValueManager {

  public DefaultKeyValueManager(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    super(loaders, holder);
  }

  @Override
  protected void storeKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    if (holder instanceof KeyValueHolderSupport) {
      KeyValueHolderSupport kvsHolder = (KeyValueHolderSupport) holder;
      loaders.forEach((name, loader) -> kvsHolder.put(name, loader));

      return;
    }

    loaders.forEach((name, loader) -> holder.put(name, loader.load()));
  }

  @Override
  protected void tryRefreshing(String name, Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    KeyValueLoader<?, ?> loader = loaders.get(name);
    if (loader == null) {
      throw new NoKeyValueLoaderFoundException("No loader found with name: [" + name + "]");
    }

    if (holder instanceof KeyValueHolderSupport) {
      ((KeyValueHolderSupport) holder).put(name, loader);

      return;
    }

    holder.put(name, loader.load());
  }
}
