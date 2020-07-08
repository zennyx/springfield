package zenny.toybox.springfield.keyvalue;

import org.springframework.lang.Nullable;

public interface KeyValueManager {

  @Nullable
  KeyValueLoader<?, ?> getLoader(String name);

  KeyValueHolder getHolder();

  void refresh(String name);
}
