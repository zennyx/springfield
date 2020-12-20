package zenny.toybox.springfield.util.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueHolder {

  @Nullable
  Map<?, ?> get(String name);

  boolean isEmpty();
}
