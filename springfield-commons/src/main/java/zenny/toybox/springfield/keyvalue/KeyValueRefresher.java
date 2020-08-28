package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueRefresher {

  void refresh(String name, @Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder);
}
