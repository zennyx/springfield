package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

// Redesign KeyValueManager class:
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
// * KeyValueSupportConfiguration(C): now, KeyValueHolder is not a bean but a
// parameter for the factory methods, and cant't be auto-wired anymore.
public interface KeyValuesFactory {

  KeyValues build(Map<String, KeyValueLoader<?, ?>> loaders);

  KeyValues build(KeyValueHolder holder);

  KeyValues build(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, @Nullable KeyValueHolder holder);
}
