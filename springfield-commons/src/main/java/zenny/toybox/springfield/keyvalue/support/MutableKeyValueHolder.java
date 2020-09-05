package zenny.toybox.springfield.keyvalue.support;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;

abstract class MutableKeyValueHolder implements KeyValueHolder {

  abstract void put(String name, @Nullable Map<?, ?> keyValues);
}
