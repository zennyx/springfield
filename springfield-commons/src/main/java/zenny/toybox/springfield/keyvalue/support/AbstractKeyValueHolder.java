package zenny.toybox.springfield.keyvalue.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.util.Assert;

public abstract class AbstractKeyValueHolder implements KeyValueHolder {

  @Override
  public void put(String name, @Nullable Map<?, ?> keyValues) {
    Assert.hasText(name, "Name must not be empty");

    if (keyValues == null || keyValues instanceof KeyValueSourceHolder) {
      this.doPut(name, keyValues);
      return;
    }

    this.doPut(name, Collections.unmodifiableMap(keyValues));
  }

  protected abstract void doPut(String name, @Nullable Map<?, ?> keyValues);
}
