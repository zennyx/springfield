package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

// TODO Redesign KeyValueManager class:
// - KeyValueManager(I)
// - AbstractKeyValueManager(AC)
// - DefaultKeyValueManager(C)
// - KeyValues(FC)
// + KeyValues(I)
// + KeyValuesFactory(I): to create KeyValues instances, has one or more factory
// methods(e.g. build(loaders), build(holder), build(loaders, holder))
// instances
// + KeyValueAssembler(I): a strategy class to gather key-values from loaders to
// the specified holder
// + KeyValueRefresher(I): a strategy class to refresh the specified key-values
// + DefaultKeyValuesFactory(C): has an assembler and a refresher, create a
// DefaultKeyValues with them
// + DefaultKeyValues(C): holds a holder and a refresher instance, both of them
// are private
// + DefaultKeyValueAssembler(C): must be thread-safe, KeyValueHolderSupport
// aware
// + DefaultKeyValueRefresher(C): must be thread-safe, KeyValueHolderSupport
// aware
// KeyValueSupportConfiguration(C): now, KeyValueHolder is not a bean but a
// parameter for the factory methods, and cant't be auto-wired anymore.
public interface KeyValueManager {

  @Nullable
  KeyValueLoader<?, ?> getLoader(String name);

  KeyValueHolder getHolder();

  @Nullable
  <K, V> Map<K, V> getValue(String name, Class<K> keyType, Class<V> valueType);

  void refresh(String name);
}
