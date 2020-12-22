package zenny.toybox.springfield.util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import zenny.toybox.springfield.util.algorithm.Hashing;

public class HashedBidirectionalMap<K, V> extends AbstractBidirectionalMap<K, V> implements Cloneable, Serializable {

  /**
   * Serialization version
   */
  private static final long serialVersionUID = 6061081398830834014L; // TODO

  /**
   * The default initial capacity - MUST be a power of two.
   */
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

  /**
   * The maximum capacity, used if a higher value is implicitly specified by
   * either of the constructors with arguments. MUST be a power of two <= 1<<30.
   */
  static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The load factor used when none specified in constructor.
   */
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * The hasher used when none specified in constructor.
   */
  static final Hasher<Object> DEFAULT_HASHER = Hashing.HASHMAP;

  /**
   * The bin count threshold for using a tree rather than list for a bin. Bins are
   * converted to trees when adding an element to a bin with at least this many
   * nodes. The value must be greater than 2 and should be at least 8 to mesh with
   * assumptions in tree removal about conversion back to plain bins upon
   * shrinkage.
   */
  static final int TREEIFY_THRESHOLD = 8;

  /**
   * The bin count threshold for untreeifying a (split) bin during a resize
   * operation. Should be less than TREEIFY_THRESHOLD, and at most 6 to mesh with
   * shrinkage detection under removal.
   */
  static final int UNTREEIFY_THRESHOLD = 6;

  /**
   * The smallest table capacity for which bins may be treeified. (Otherwise the
   * table is resized if too many nodes in a bin.) Should be at least 4 *
   * TREEIFY_THRESHOLD to avoid conflicts between resizing and treeification
   * thresholds.
   */
  static final int MIN_TREEIFY_CAPACITY = 64;

  /**
   * The table, initialized on first use, and resized as necessary. When
   * allocated, length is always a power of two. (We also tolerate length zero in
   * some operations to allow bootstrapping mechanics that are currently not
   * needed.)
   */
  transient Node<K, V>[] table;

  /**
   * The mirror to the {@code table}, initialized on first use, and resized as
   * necessary. When allocated, length is always a power of two.
   */
  transient Node<V, K>[] mirror;

  /**
   * Holds cached {@code entrySet()}. Note that {@link AbstractBidirectionalMap}
   * fields are used for {@code keySet()} and {@code values()}.
   */
  transient Set<Map.Entry<K, V>> entrySet;

  /**
   * Holds cached {@code inversedEntrySet()}. Note that
   * {@link AbstractBidirectionalMap} fields are used for {@code keySet()} and
   * {@code values()}.
   */
  transient Set<Map.Entry<V, K>> inversedEntrySet;

  /**
   * The number of key-value mappings contained in this map.
   */
  transient int size;

  /**
   * The number of times this map has been structurally modified Structural
   * modifications are those that change the number of mappings in the map or
   * otherwise modify its internal structure (e.g., rehash). This field is used to
   * make iterators on Collection-views of the map fail-fast.
   *
   * @see ConcurrentModificationException
   */
  transient int modCount;

  /**
   * The next size value at which to resize (capacity * load factor).
   * <p>
   * Additionally, if the table array has not been allocated, this field holds the
   * initial array capacity, or zero signifying {@code DEFAULT_INITIAL_CAPACITY}.
   *
   * @serial
   */
  int threshold;

  /**
   * The load factor for the hash table.
   *
   * @serial
   */
  final float loadFactor;

  /**
   * The haser for the hash table.
   *
   * @serial
   */
  final Hasher<Object> haser;

  /**
   * Constructs an empty {@code HashedBidirectionalMap} with the default initial
   * capacity ({@code 16}), the default load factor ({@code 0.75}) and the default
   * hasher ({@link Hashing.MURMURHASH3}).
   */
  public HashedBidirectionalMap() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * Constructs an empty {@code HashedBidirectionalMap} with the specified initial
   * capacity and the default load factor ({@code 0.75}) and the default hasher
   * ({@link Hashing.MURMURHASH3}).
   *
   * @param initialCapacity the initial capacity.
   * @throws IllegalArgumentException if the initial capacity is negative.
   */
  public HashedBidirectionalMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_HASHER);
  }

  /**
   * Constructs an empty {@code HashedBidirectionalMap} with the specified initial
   * capacity, load factor and hasher.
   *
   * @param initialCapacity the initial capacity
   * @param loadFactor the load factor
   * @param hasher the hasher to calculate hash codes
   * @throws IllegalArgumentException if the initial capacity is negative or the
   * load factor is nonpositive
   */
  public HashedBidirectionalMap(int initialCapacity, float loadFactor, Hasher<Object> hasher) {
    Assert.isTrue(initialCapacity >= 0, "Illegal initial capacity: " + initialCapacity);
    Assert.isTrue(loadFactor > 0 && !Float.isNaN(loadFactor), "Illegal load factor: " + loadFactor);

    if (initialCapacity > MAXIMUM_CAPACITY) {
      initialCapacity = MAXIMUM_CAPACITY;
    }

    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
    this.haser = hasher;
  }

  /**
   * Constructs a new {@code HashedBidirectionalMap} with the same mappings as the
   * specified {@code Map}. The {@code HashedBidirectionalMap} is created with
   * default load factor ({@code 0.75}) and an initial capacity sufficient to hold
   * the mappings in the specified {@code Map}.
   *
   * @param m the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   */
  public HashedBidirectionalMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    this.haser = DEFAULT_HASHER;
    this.putMapEntries(m, false);
  }

  /**
   * Implements Map.putAll and Map constructor
   *
   * @param m the map
   * @param evict false when initially constructing this map, else true (relayed
   * to method afterNodeInsertion).
   */
  final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    // TODO depend on resize() & putVal()
  }

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map
   */
  @Override
  public int size() {
    return this.size;
  }

  /**
   * Returns {@code true} if this map contains no key-value mappings.
   *
   * @return {@code true} if this map contains no key-value mappings
   */
  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if
   * this map contains no mapping for the key.
   * <p>
   * More formally, if this map contains a mapping from a key {@code k} to a value
   * {@code v} such that {@code (key==null ? k==null :
   * key.equals(k))}, then this method returns {@code v}; otherwise it returns
   * {@code null}. (There can be at most one such mapping.)
   * <p>
   * A return value of {@code null} does not <i>necessarily</i> indicate that the
   * map contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to {@code null}. The {@link #containsKey containsKey}
   * operation may be used to distinguish these two cases.
   *
   * @see #put(Object, Object)
   */
  @Override
  public V get(Object key) {
    return null; // TODO
  }

  /**
   * Returns a power of two size for the given target capacity.
   */
  static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return n < 0 ? 1 : n >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : n + 1;
  }

  /**
   * Returns x's Class if it is of the form "class C implements
   * Comparable&ltC&gt", else null.
   */
  static Class<?> comparableClassFor(Object x) {
    if (x instanceof Comparable) {
      Class<?> c;
      Type[] ts, as;
      Type t;
      ParameterizedType p;
      if ((c = x.getClass()) == String.class) {
        return c;
      }
      if ((ts = c.getGenericInterfaces()) != null) {
        for (int i = 0; i < ts.length; ++i) {
          if ((t = ts[i]) instanceof ParameterizedType && (p = (ParameterizedType) t).getRawType() == Comparable.class
              && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c) {
            return c;
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns k.compareTo(x) if x matches kc (k's screened comparable class), else
   * 0.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  static int compareComparables(Class<?> kc, Object k, Object x) {
    return x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Entry<V, K>> inversedEntrySet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BidirectionalMap<V, K> inverse() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Basic hash bin node, used for most entries. (See below for TreeNode subclass,
   * and in LinkedHashMap for its Entry subclass.)
   */
  static class Node<K, V> implements Map.Entry<K, V> {
    final int hash;
    final K key;
    V value;
    Node<K, V> next;

    Node(int hash, K key, V value, Node<K, V> next) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
    }

    @Override
    public final K getKey() {
      return this.key;
    }

    @Override
    public final V getValue() {
      return this.value;
    }

    @Override
    public final String toString() {
      return this.key + "=" + this.value;
    }

    @Override
    public final int hashCode() {
      return Objects.hashCode(this.key) ^ Objects.hashCode(this.value); // TODO hasher?
    }

    @Override
    public final V setValue(V newValue) {
      V oldValue = this.value;
      this.value = newValue;
      return oldValue;
    }

    @Override
    public final boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (o instanceof Map.Entry) {
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        if (Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue())) {
          return true;
        }
      }
      return false;
    }
  }
}
