package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

public interface KeyValueHolder {

  @Nullable
  Map<?, ?> get(String name);

  boolean isEmpty();
}
