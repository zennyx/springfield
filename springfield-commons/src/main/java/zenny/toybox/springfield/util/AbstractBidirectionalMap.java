package zenny.toybox.springfield.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;

/**
 * This class provides a skeletal implementation of the {@link BidirectionalMap}
 * interface, to minimize the effort required to implement this interface.
 *
 * @author Zenny Xu
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see Map
 * @see Set
 */
public abstract class AbstractBidirectionalMap<K, V> implements BidirectionalMap<K, V> {

  /**
   * Each of these fields are initialized to contain an instance of the
   * appropriate view the first time this view is requested. The views are
   * stateless, so there's no reason to create more than one of each.
   * <p>
   * Since there is no synchronization performed while accessing these fields, it
   * is expected that java.util.Map view classes using these fields have no
   * non-final fields (or any fields at all except for outer-this). Adhering to
   * this rule would make the races on these fields benign.
   */
  transient Set<K> keySet;
  transient Set<V> values;

  /**
   * Sole constructor. (For invocation by subclass constructors, typically
   * implicit.)
   */
  protected AbstractBidirectionalMap() {
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation returns {@code entrySet().size()}.
   */
  @Override
  public int size() {
    return this.entrySet().size();
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation returns {@code size() == 0}.
   */
  @Override
  public boolean isEmpty() {
    return this.size() == 0;
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over {@code entrySet()} searching for
   * an entry with the specified key. If such an entry is found, {@code true} is
   * returned. If the iteration terminates without finding such an entry,
   * {@code false} is returned. Note that this implementation requires linear time
   * in the size of the map; many implementations will override this method.
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public boolean containsKey(@Nullable Object key) {
    return containsIt(key, this.entrySet().iterator());
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over {@code inversedEntrySet()}
   * searching for an entry with the specified value. If such an entry is found,
   * {@code true} is returned. If the iteration terminates without finding such an
   * entry, {@code false} is returned. Note that this implementation requires
   * linear time in the size of the map.
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Override
  public boolean containsValue(@Nullable Object value) {
    return containsIt(value, this.inverseEntrySet().iterator());
  }

  static <K, V> boolean containsIt(@Nullable Object key, Iterator<Entry<K, V>> iterator) {
    if (key == null) {
      while (iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (e.getKey() == null) {
          return true;
        }
      }
    } else {
      while (iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (key.equals(e.getKey())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over {@code entrySet()} searching for
   * an entry with the specified key. If such an entry is found, the entry's value
   * is returned. If the iteration terminates without finding such an entry,
   * {@code null} is returned. Note that this implementation requires linear time
   * in the size of the map; many implementations will override this method.
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Nullable
  @Override
  public V get(@Nullable Object key) {
    return getIt(key, this.entrySet().iterator());
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over {@code inversedEntrySet()}
   * searching for an entry with the specified value. If such an entry is found,
   * the entry's key is returned. If the iteration terminates without finding such
   * an entry, {@code null} is returned. Note that this implementation requires
   * linear time in the size of the map; many implementations will override this
   * method.
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Nullable
  @Override
  public K getKey(@Nullable Object value) {
    return getIt(value, this.inverseEntrySet().iterator());
  }

  @Nullable
  static <K, V> V getIt(@Nullable Object key, Iterator<Entry<K, V>> iterator) {
    if (key == null) {
      while (iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (e.getKey() == null) {
          return e.getValue();
        }
      }
    } else {
      while (iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (key.equals(e.getKey())) {
          return e.getValue();
        }
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation always throws an
   * {@code UnsupportedOperationException}.
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   */
  @Override
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over {@code entrySet()} searching for
   * an entry with the specified key. If such an entry is found, its value is
   * obtained with its {@code getValue} operation, the entry is removed from the
   * collection (and the backing map) with the iterator's {@code remove}
   * operation, and the saved value is returned. If the iteration terminates
   * without finding such an entry, {@code null} is returned. Note that this
   * implementation requires linear time in the size of the map; many
   * implementations will override this method.
   * <p>
   * Note that this implementation throws an {@code UnsupportedOperationException}
   * if the {@code entrySet} or the {@code inversedEntrySet} iterator does not
   * support the {@code remove} method and this map contains a mapping for the
   * specified key.
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   */
  @Nullable
  @Override
  public V remove(@Nullable Object key) {
    Optional<V> value = removeIt(key, this.entrySet().iterator());
    if (value != null) {
      removeIt(value.orElse(null), this.inverseEntrySet().iterator());
    }

    return value == null ? null : value.orElse(null);
  }

  @Nullable
  static <K, V> Optional<V> removeIt(@Nullable Object key, Iterator<Entry<K, V>> iterator) {
    Entry<K, V> correctEntry = null;
    if (key == null) {
      while (correctEntry == null && iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (e.getKey() == null) {
          correctEntry = e;
        }
      }
    } else {
      while (correctEntry == null && iterator.hasNext()) {
        Entry<K, V> e = iterator.next();
        if (key.equals(e.getKey())) {
          correctEntry = e;
        }
      }
    }

    Optional<V> oldValue = null;
    if (correctEntry != null) {
      oldValue = Optional.ofNullable(correctEntry.getValue());
      iterator.remove();
    }
    return oldValue;
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation iterates over the specified map's
   * {@code entrySet()} collection, and calls this map's {@code put} operation
   * once for each entry returned by the iteration.
   * <p>
   * Note that this implementation throws an {@code UnsupportedOperationException}
   * if this map does not support the {@code put} operation and the specified map
   * is nonempty.
   * @throws UnsupportedOperationException {@inheritDoc}
   * @throws ClassCastException {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   */
  @Override
  public void putAll(@Nullable Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      this.put(e.getKey(), e.getValue());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation calls {@code entrySet().clear()} and
   * {@code inversedEntrySet().clear()}.
   * <p>
   * Note that this implementation throws an {@code UnsupportedOperationException}
   * if the {@code entrySet} or the {@code inversedEntrySet} does not support the
   * {@code clear} operation.
   * @throws UnsupportedOperationException {@inheritDoc}
   */
  @Override
  public void clear() {
    this.entrySet().clear();
    this.inverseEntrySet().clear();
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation returns a set that subclasses
   * {@link AbstractSet}. The subclass's iterator method returns a "wrapper
   * object" over this map's {@code entrySet()} iterator. The {@code size} method
   * delegates to this map's {@code size} method and the {@code contains} method
   * delegates to this map's {@code containsKey} method.
   * <p>
   * The set is created the first time this method is called, and returned in
   * response to all subsequent calls. No synchronization is performed, so there
   * is a slight chance that multiple calls to this method will not all return the
   * same set.
   * @throws UnsupportedOperationException if the
   * {@code add}/{@code clear}/{@code remove}/{@code removeAll} operation is
   * called
   */
  @Override
  public Set<K> keySet() {
    if (this.keySet == null) {
      this.keySet = this.toSet(this.entrySet().iterator(), true);
    }

    return this.keySet;
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec This implementation returns a set that subclasses
   * {@link AbstractSet}. The subclass's iterator method returns a "wrapper
   * object" over this map's {@code inversedEntrySet()} iterator. The {@code size}
   * method delegates to this map's {@code size} method and the {@code contains}
   * method delegates to this map's {@code containsValue} method.
   * <p>
   * The set is created the first time this method is called, and returned in
   * response to all subsequent calls. No synchronization is performed, so there
   * is a slight chance that multiple calls to this method will not all return the
   * same set.
   * @throws UnsupportedOperationException if the
   * {@code add}/{@code clear}/{@code remove}/{@code removeAll} operation is
   * called
   */
  @Override
  public Set<V> values() {
    if (this.values == null) {
      this.values = this.toSet(this.inverseEntrySet().iterator(), false);
    }

    return this.values;
  }

  <TK, TV> Set<TK> toSet(Iterator<Entry<TK, TV>> iterator, boolean isKeySet) {
    return new AbstractSet<TK>() {
      @Override
      public Iterator<TK> iterator() {
        return new Iterator<TK>() {
          private final Iterator<Entry<TK, TV>> i = iterator;

          @Override
          public boolean hasNext() {
            return this.i.hasNext();
          }

          @Override
          public TK next() {
            return this.i.next().getKey();
          }
        };
      }

      @Override
      public int size() {
        return AbstractBidirectionalMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return AbstractBidirectionalMap.this.isEmpty();
      }

      @Override
      public boolean remove(@Nullable Object o) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean contains(@Nullable Object k) {
        if (isKeySet) {
          return AbstractBidirectionalMap.this.containsKey(k);
        }

        return AbstractBidirectionalMap.this.containsValue(k);
      }
    };
  }

  protected abstract Set<Entry<V, K>> inverseEntrySet();

  /**
   * Compares the specified object with this map for equality. Returns
   * {@code true} if the given object is also a map and the two maps represent the
   * same mappings. More formally, two maps {@code m1} and {@code m2} represent
   * the same mappings if {@code m1.entrySet().equals(m2.entrySet())}. This
   * ensures that the {@code equals} method works properly across different
   * implementations of the {@link Map} interface.
   *
   * @implSpec This implementation first checks if the specified object is this
   * map; if so it returns {@code true}. Then, it checks if the specified object
   * is a map whose size is identical to the size of this map; if not, it returns
   * {@code false}. If so, it iterates over this map's {@code entrySet}
   * collection, and checks that the specified map contains each mapping that this
   * map contains. If the specified map fails to contain such a mapping,
   * {@code false} is returned. If the iteration completes, {@code true} is
   * returned.
   * @param o object to be compared for equality with this map
   * @return {@code true} if the specified object is equal to this map
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Map)) {
      return false;
    }

    Map<?, ?> m = (Map<?, ?>) o;
    if (m.size() != this.size()) {
      return false;
    }

    try {
      for (Entry<K, V> e : this.entrySet()) {
        K key = e.getKey();
        V value = e.getValue();
        if (value == null) {
          if (!(m.get(key) == null && m.containsKey(key))) {
            return false;
          }
        } else {
          if (!value.equals(m.get(key))) {
            return false;
          }
        }
      }
    } catch (ClassCastException | NullPointerException unused) {
      return false;
    }

    return true;
  }

  /**
   * Returns the hash code value for this map. The hash code of a map is defined
   * to be the sum of the hash codes of each entry in the map's {@code entrySet()}
   * view. This ensures that {@code m1.equals(m2)} implies that
   * {@code m1.hashCode()==m2.hashCode()} for any two maps {@code m1} and
   * {@code m2}, as required by the general contract of {@link Object#hashCode}.
   *
   * @implSpec This implementation iterates over {@code entrySet()}, calling
   * {@link Map.Entry#hashCode hashCode()} on each element (entry) in the set, and
   * adding up the results.
   * @return the hash code value for this map
   * @see Map.Entry#hashCode()
   * @see Object#equals(Object)
   * @see Set#equals(Object)
   */
  @Override
  public int hashCode() {
    int h = 0;
    for (Entry<K, V> kvEntry : this.entrySet()) {
      h += kvEntry.hashCode();
    }

    return h;
  }

  /**
   * Returns a string representation of this map. The string representation
   * consists of a list of key-value mappings in the order returned by the map's
   * {@code entrySet} view's iterator, enclosed in braces ({@code "{}"}). Adjacent
   * mappings are separated by the characters {@code ", "} (comma and space). Each
   * key-value mapping is rendered as the key followed by an equals sign
   * ({@code "="}) followed by the associated value. Keys and values are converted
   * to strings as by {@link String#valueOf(Object)}.
   *
   * @return a string representation of this map
   */
  @Override
  public String toString() {
    Iterator<Entry<K, V>> i = this.entrySet().iterator();
    if (!i.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (;;) {
      Entry<K, V> e = i.next();
      K key = e.getKey();
      V value = e.getValue();
      sb.append(key == this ? "(this Map)" : key);
      sb.append('=');
      sb.append(value == this ? "(this Map)" : value);
      if (!i.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',').append(' ');
    }
  }

  /**
   * Returns a shallow copy of this {@code AbstractBidirectionalMap} instance: the
   * keys and values themselves are not cloned.
   *
   * @return a shallow copy of this map
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    AbstractBidirectionalMap<?, ?> result = (AbstractBidirectionalMap<?, ?>) super.clone();
    result.keySet = null;
    result.values = null;

    return result;
  }
}
