package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import zenny.toybox.springfield.keyvalue.KeyValueAssembler;
import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;

public class DefaultKeyValueAssembler implements KeyValueAssembler {

  @Override
  public void assamble(Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    if (holder instanceof KeyValueHolderSupport) {
      KeyValueHolderSupport kvsHolder = (KeyValueHolderSupport) holder;
      loaders.forEach((name, loader) -> kvsHolder.put(name, loader));

      return;
    }

    loaders.forEach((name, loader) -> holder.put(name, loader.load()));
  }
}
