package zenny.toybox.springfield.util.id.support;

import org.springframework.core.AttributeAccessor;
import org.springframework.lang.Nullable;

public interface Configuration extends AttributeAccessor {

  @FunctionalInterface
  interface Customizer {
    void customize(@Nullable Configuration config);
  }
}
