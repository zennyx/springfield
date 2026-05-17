package zenny.toybox.springfield.util;

import java.util.Collection;
import java.util.Set;

public interface PitStop<E> extends Pit<E>, Collection<E> {

  int count(Object object);

  boolean add(E element, int copies);

  boolean remove(Object object, int copies);

  Set<E> set();
}
