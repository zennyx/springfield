package zenny.toybox.springfield.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;

/**
 * A concurrent hash map backed (actually a {@link ConcurrentHashMap} instance)
 * set supporting full concurrency of retrievals and high expected concurrency
 * for updates.
 *
 * @author Zenny Xu
 * @param <E> the type of elements maintained by this set
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {

  /**
   * The backing {@link ConcurrentHashMap}
   */
  private final Map<E, Boolean> map;

  /**
   * A Set view of the keys contained in the backing map
   */
  private transient Set<E> keys;

  /**
   * Creates a new, empty set; the backing {@link ConcurrentHashMap} instance has
   * default initial capacity (16) and load factor (0.75).
   */
  public ConcurrentHashSet() {
    this.map = new ConcurrentHashMap<>();
    this.keys = this.map.keySet();
  }

  /**
   * Constructs a new, empty set; the backing {@link ConcurrentHashMap} instance
   * has the specified initial capacity and the specified load factor.
   *
   * @param initialCapacity the initial capacity of the hash map sizing to
   * accommodate this many elements.
   * @throws IllegalArgumentException if the initial capacity is less than zero
   */
  public ConcurrentHashSet(int initialCapacity) {
    this.map = new ConcurrentHashMap<>(initialCapacity);
    this.keys = this.map.keySet();
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#add(java.lang.Object)
   */
  @Override
  public boolean add(@Nullable E e) {
    if (e == null) {
      return false;
    }

    return this.map.put(e, Boolean.TRUE) == null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#remove(java.lang.Object)
   */
  @Override
  public boolean remove(@Nullable Object o) {
    if (o == null) {
      return false;
    }

    return this.map.remove(o) != null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#contains(java.lang.Object)
   */
  @Override
  public boolean contains(@Nullable Object o) {
    if (o == null) {
      return false;
    }

    return this.map.containsKey(o);
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#size()
   */
  @Override
  public int size() {
    return this.map.size();
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#clear()
   */
  @Override
  public void clear() {
    this.map.clear();
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#iterator()
   */
  @Override
  public Iterator<E> iterator() {
    return this.keys.iterator();
  }

  /**
   * {@inheritDoc}
   *
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public boolean containsAll(@Nullable Collection<?> c) {
    return this.keys.containsAll(c);
  }

  /**
   * {@inheritDoc}
   *
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public boolean removeAll(@Nullable Collection<?> c) {
    return this.keys.removeAll(c);
  }

  /**
   * {@inheritDoc}
   *
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public boolean retainAll(@Nullable Collection<?> c) {
    return this.keys.retainAll(c);
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#toArray()
   */
  @Override
  public Object[] toArray() {
    return this.keys.toArray();
  }

  /**
   * {@inheritDoc}
   * 
   * @throws ArrayStoreException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public <T> T[] toArray(T[] a) {
    return this.keys.toArray(a);
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractSet#equals(java.lang.Object)
   */
  @Override
  public boolean equals(@Nullable Object o) {
    return o == this || this.keys.equals(o);
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractSet#hashCode()
   */
  @Override
  public int hashCode() {
    return this.keys.hashCode();
  }

  /*
   * (non-Javadoc)
   * @see java.util.AbstractCollection#toString()
   */
  @Override
  public String toString() {
    return this.keys.toString();
  }
}
