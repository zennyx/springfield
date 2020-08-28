package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValueRefresher;

public class DefaultKeyValueRefresher implements KeyValueRefresher {

  @Override
  public void refresh(String name, Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {

    KeyValueLoader<?, ?> loader = loaders.get(name);
    if (loader == null) {
      throw new NoKeyValueLoaderFoundException("No loader found for name: [" + name + "]");
    }

    if (holder instanceof KeyValueHolderSupport) {
      ((KeyValueHolderSupport) holder).put(name, loader);

      return;
    }

    holder.put(name, loader.load());
  }
}
