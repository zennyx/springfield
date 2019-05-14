package zenny.toybox.springfield.util;

import java.io.Serializable;

public interface Version<T extends Serializable & Comparable<T>> {

  T get();
}
