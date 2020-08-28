package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueAssembler {

  void assamble(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder);
}
