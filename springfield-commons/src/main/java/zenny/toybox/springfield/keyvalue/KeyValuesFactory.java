package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValuesFactory {

  KeyValues getKeyValues(Map<String, KeyValueLoader<?, ?>> loaders);

  KeyValues getKeyValues(KeyValueHolder holder);

  KeyValues getKeyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, @Nullable KeyValueHolder holder);
}
