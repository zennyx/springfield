package zenny.toybox.springfield.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.algorithm.Hashing;

/**
 * Hash table based implementation of the {@code BidirectionalMap} interface.
 *
 * @author Zenny Xu
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see HashMap
 */
public class HashedBidirectionalMap<K, V> extends AbstractBidirectionalMap<K, V> implements Cloneable, Serializable {

  /**
   * Serialization version
   */
  private static final long serialVersionUID = -819444677160561728L; // TODO

  /* ---------------- Constants -------------- */

  /**
   * The load factor used when none specified in constructor.
   */
  static final float DEFAULT_LOAD_FACTOR = 0.75f;

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

  /* ---------------- Fields -------------- */

  /**
   * The table, initialized on first use, and resized as necessary. When
   * allocated, length is always a power of two.
   */
  final transient Nodes<K, V> bi; // TODO transient?

  /**
   * The mirror to the {@code bi} table, initialized on first use, and resized as
   * necessary. When allocated, length is always a power of two.
   */
  final transient Nodes<V, K> di;

  /**
   * Holds cached {@code entrySet()}. Note that {@link AbstractBidirectionalMap}
   * fields are used for {@code keySet()} and {@code values()}.
   */
  transient Set<Entry<K, V>> entrySet;

  /**
   * Holds cached {@code inversedEntrySet()}. Note that
   * {@link AbstractBidirectionalMap} fields are used for {@code keySet()} and
   * {@code values()}.
   */
  transient Set<Map.Entry<V, K>> inverseEntrySet;

  /**
   * Each of these fields are initialized to contain an instance of the
   * appropriate view the first time this view is requested. The views are
   * stateless, so there's no reason to create more than one of each.
   * <p>
   * Since there is no synchronization performed while accessing these fields, it
   * is expected that java.util.Map view classes using these fields have no
   * non-final fields (or any fields at all except for outer-this). Adhering to
   * this rule would make the races on these fields benign.
   * <p>
   * It is also imperative that implementations read the field only once, as in:
   *
   * <pre>
   * public Set<K> keySet() {
   *   Set<K> ks = keySet; // single racy read
   *   if (ks == null) {
   *     ks = new KeySet();
   *     keySet = ks;
   *   }
   *   return ks;
   * }
   * </pre>
   */
  transient Set<K> keySet;
  transient Set<V> values;

  /* ---------------- Constructors -------------- */

  /**
   * Constructs an empty {@code HashedBidirectionalMap} with the default initial
   * capacity ({@code 16}), the default load factor ({@code 0.75}) and the default
   * hasher ({@link Hashing#HASHMAP}).
   */
  public HashedBidirectionalMap() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  /**
   * Constructs an empty {@code HashedBidirectionalMap} with the specified initial
   * capacity and the default load factor ({@code 0.75}) and the default hasher
   * ({@link Hashing#HASHMAP}).
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
   * @param hasher the hasher to calculate hash-code for objects
   * @throws IllegalArgumentException if the initial capacity is negative or the
   * load factor is nonpositive
   */
  public HashedBidirectionalMap(int initialCapacity, float loadFactor, @Nullable Hasher<Object> hasher) {
    Assert.isTrue(initialCapacity >= 0, "Illegal initial capacity: " + initialCapacity);
    Assert.isTrue(loadFactor > 0 && !Float.isNaN(loadFactor), "Illegal load factor: " + loadFactor);

    if (initialCapacity > MAXIMUM_CAPACITY) {
      initialCapacity = MAXIMUM_CAPACITY;
    }
    if (hasher == null) {
      hasher = DEFAULT_HASHER;
    }

    this.bi = new Nodes<>(initialCapacity, loadFactor, hasher);
    this.di = new Nodes<>(initialCapacity, loadFactor, hasher);

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
    // TODO this#putMapEntries(m, false);
    this();
  }

  /**
   * A private constructor for the {@code HashedBidirectionalMap#inverse} method
   * implementation.
   */
  private HashedBidirectionalMap(Nodes<K, V> bi, Nodes<V, K> di) {
    this.bi = bi;
    this.di = di;
  }

  /* ---------------- Operations -------------- */

  /**
   * Returns the number of key-value mappings in this map.
   *
   * @return the number of key-value mappings in this map
   */
  @Override
  public int size() {
    return this.bi.size;
  }

  /**
   * Returns {@code true} if this map contains no key-value mappings.
   *
   * @return {@code true} if this map contains no key-value mappings
   */
  @Override
  public boolean isEmpty() {
    return this.bi.size == 0;
  }

  /**
   * Returns {@code true} if this map contains a mapping for the specified key.
   *
   * @param key The key whose presence in this map is to be tested
   * @return {@code true} if this map contains a mapping for the specified key.
   */
  @Override
  public boolean containsKey(@Nullable Object key) {
    return this.bi.get(key) != null;
  }

  /**
   * Returns {@code true} if this map maps one or more keys to the specified
   * value.
   *
   * @param value value whose presence in this map is to be tested
   * @return {@code true} if this map maps one or more keys to the specified value
   */
  @Override
  public boolean containsValue(@Nullable Object value) {
    return this.di.get(value) != null;
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
  @Nullable
  @Override
  public V get(@Nullable Object key) {
    Node<K, V> e;
    return (e = this.bi.get(key)) == null ? null : e.value;
  }

  @Nullable
  @Override
  public K getKey(@Nullable Object value) {
    Node<V, K> e;
    return (e = this.di.get(value)) == null ? null : e.value;
  }

  /**
   * Associates the specified value with the specified key in this map. If the map
   * previously contained a mapping for the key, the old value is replaced.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return the previous value associated with {@code key}, or {@code null} if
   * there was no mapping for {@code key}. (A {@code null} return can also
   * indicate that the map previously associated {@code null} with {@code key}.)
   */
  @Nullable
  @Override
  public V put(@Nullable K key, @Nullable V value) {
    return this.doPut(key, value, false);
  }

  /**
   * Implements Map.put and related methods
   *
   * @param key the key
   * @param value the value to put
   * @param onlyIfAbsent if true, don't change existing value
   * @return previous value, or {@code null} if none
   */
  @Nullable
  final V doPut(@Nullable K key, @Nullable V value, boolean onlyIfAbsent) {
    Optional<V> returned = this.bi.put(key, value, onlyIfAbsent);
    if (returned == null) { // no mapping found, put directly
      this.di.put(value, key, onlyIfAbsent);
    }

    V oldValue = returned.orElse(null);
    if (!Objects.equals(value, oldValue)) { // do nothing if value has no change
      this.di.put(value, key, onlyIfAbsent);
      this.di.remove(oldValue, null, false, true);
    }

    return oldValue;
  }

  /**
   * Removes the mapping for the specified key from this map if present.
   *
   * @param key key whose mapping is to be removed from the map
   * @return the previous value associated with {@code key}, or {@code null} if
   * there was no mapping for {@code key}. (A {@code null} return can also
   * indicate that the map previously associated {@code null} with {@code key}.)
   */
  @Override
  public final V remove(@Nullable Object key) { // final for ValueSet#remove
    return this.doRemove(key);
  }

  /**
   * Implements Map.remove and related methods
   *
   * @param key the key
   * @return the node, or {@code null} if none
   */
  final V doRemove(@Nullable Object key) {
    Node<K, V> e = this.bi.remove(key, null, false, true);
    if (e != null) {
      V value = e.value;
      this.di.remove(value, null, false, true);
      return value;
    }

    return null;
  }

  /**
   * Copies all of the mappings from the specified map to this map. These mappings
   * will replace any mappings that this map had for any of the keys currently in
   * the specified map.
   *
   * @param m mappings to be stored in this map
   * @throws NullPointerException if the specified map is null
   */
  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    this.putMapEntries(m, true);
  }

  final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    // TODO
  }

  /**
   * Removes all of the mappings from this map. The map will be empty after this
   * call returns.
   */
  @Override
  public void clear() {
    this.bi.clear();
    this.di.clear();
  }

  /**
   * Returns a {@link Set} view of the keys contained in this map. The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa. If the map is modified while an iteration over the set is in
   * progress (except through the iterator's own {@code remove} operation), the
   * results of the iteration are undefined. The set supports element removal,
   * which removes the corresponding mapping from the map, via the
   * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll},
   * {@code retainAll}, and {@code clear} operations. It does not support the
   * {@code add} or {@code addAll} operations.
   *
   * @return a set view of the keys contained in this map
   */
  @Override
  public Set<K> keySet() {
    Set<K> ks = this.keySet;
    if (ks == null) {
      ks = new KeySet();
      this.keySet = ks;
    }
    return ks;
  }

  /**
   * Returns a {@link Set} view of the values contained in this map. The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa. If the map is modified while an iteration over the set is in
   * progress (except through the iterator's own {@code remove} operation), the
   * results of the iteration are undefined. The set supports element removal,
   * which removes the corresponding mapping from the map, via the
   * {@code Iterator.remove}, {@code Set.remove}, {@code removeAll},
   * {@code retainAll}, and {@code clear} operations. It does not support the
   * {@code add} or {@code addAll} operations.
   *
   * @return a set view of the values contained in this map
   */
  @Override
  public Set<V> values() {
    Set<V> vs = this.values;
    if (vs == null) {
      vs = new ValueSet();
      this.values = vs;
    }
    return vs;
  }

  /**
   * Returns a {@link Set} view of the mappings contained in this map. The set is
   * backed by the map, so changes to the map are reflected in the set, and
   * vice-versa. If the map is modified while an iteration over the set is in
   * progress (except through the iterator's own {@code remove} operation, or
   * through the {@code setValue} operation on a map entry returned by the
   * iterator) the results of the iteration are undefined. The set supports
   * element removal, which removes the corresponding mapping from the map, via
   * the {@code Iterator.remove}, {@code Set.remove}, {@code removeAll},
   * {@code retainAll} and {@code clear} operations. It does not support the
   * {@code add} or {@code addAll} operations.
   *
   * @return a set view of the mappings contained in this map
   */
  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> es;
    return (es = this.entrySet) == null ? (this.entrySet = new EntrySet()) : es;
  }

  /**
   * Returns a {@link Set} view of the inverse-mappings contained in this map. The
   * set is backed by the map, so changes to the map are reflected in the set, and
   * vice-versa. If the map is modified while an iteration over the set is in
   * progress (except through the iterator's own {@code remove} operation, or
   * through the {@code setValue} operation on a map entry returned by the
   * iterator) the results of the iteration are undefined. The set supports
   * element removal, which removes the corresponding mapping from the map, via
   * the {@code Iterator.remove}, {@code Set.remove}, {@code removeAll},
   * {@code retainAll} and {@code clear} operations. It does not support the
   * {@code add} or {@code addAll} operations.
   *
   * @return a set view of the mappings contained in this map
   */
  @Override
  protected Set<Entry<V, K>> inverseEntrySet() { // protected for Collections#unmodifiableMap
    Set<Map.Entry<V, K>> ies;
    return (ies = this.inverseEntrySet) == null ? (this.inverseEntrySet = new InverseEntrySet()) : ies;
  }

  @Override
  public BidirectionalMap<V, K> inverse() {
    return new HashedBidirectionalMap<>(this.di, this.bi);
  }

  /* ---------------- Overrides of JDK8 Map extension methods -------------- */

  /*
   * (non-Javadoc)
   * @see java.util.Map#getOrDefault(java.lang.Object, java.lang.Object)
   */
  @Nullable
  @Override
  public V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
    Node<K, V> e;
    return (e = this.bi.get(key)) == null ? defaultValue : e.value;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
   */
  @Nullable
  @Override
  public V putIfAbsent(@Nullable K key, @Nullable V value) {
    return this.doPut(key, value, true);
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    Node<K, V> removed = this.bi.remove(key, value, true, true);
    if (removed != null) {
      this.di.remove(value, null, false, true);
      return true;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replace(java.lang.Object, java.lang.Object,
   * java.lang.Object)
   */
  @Override
  public boolean replace(@Nullable K key, @Nullable V oldValue, @Nullable V newValue) {
    Node<K, V> e;
    V v;
    if ((e = this.bi.get(key)) != null && ((v = e.value) == oldValue || Objects.equals(v, oldValue))) {
      e.value = newValue;
      if (!Objects.equals(v, newValue)) {
        this.di.put(newValue, key, false);
        this.di.remove(oldValue, null, false, true);
      }
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
   */
  @Nullable
  @Override
  public V replace(@Nullable K key, @Nullable V value) {
    Node<K, V> e;
    if ((e = this.bi.get(key)) != null) {
      V oldValue = e.value;
      e.value = value;
      if (!Objects.equals(value, oldValue)) {
        this.di.put(value, key, false);
        this.di.remove(oldValue, null, false, true);
      }
      return oldValue;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#computeIfAbsent(java.lang.Object,
   * java.util.function.Function)
   */
  @Nullable
  @Override
  public V computeIfAbsent(@Nullable K key, Function<? super K, ? extends V> mappingFunction) {
    return null; // TODO
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#computeIfPresent(java.lang.Object,
   * java.util.function.BiFunction)
   */
  @Nullable
  @Override
  public V computeIfPresent(@Nullable K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return null; // TODO
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
   */
  @Nullable
  @Override
  public V compute(@Nullable K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return null; // TODO
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#merge(java.lang.Object, java.lang.Object,
   * java.util.function.BiFunction)
   */
  @Nullable
  @Override
  public V merge(@Nullable K key, @Nullable V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    return null; // TODO
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#forEach(java.util.function.BiConsumer)
   */
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * @see java.util.Map#replaceAll(java.util.function.BiFunction)
   */
  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    // TODO
  }

  /* ---------------- Cloning and serialization -------------- */

  /**
   * Returns a shallow copy of this {@code HashedBidirectionalMap} instance: the
   * keys and values themselves are not cloned.
   *
   * @return a shallow copy of this map
   */
  @Override
  public Object clone() {
    return null; // TODO
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    // TODO
  }

  private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
    // TODO
  }

  /* ---------------- Nodes -------------- */

  /**
   * Create a regular (non-tree) node
   */
  static <K, V> Node<K, V> newNode(int hash, @Nullable K key, @Nullable V value, @Nullable Node<K, V> next) {
    return new Node<>(hash, key, value, next);
  }

  /**
   * For conversion from TreeNodes to plain nodes
   */
  static <K, V> Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
    return new Node<>(p.hash, p.key, p.value, next);
  }

  /**
   * Create a tree bin node
   */
  static <K, V> TreeNode<K, V> newTreeNode(int hash, @Nullable K key, @Nullable V value, @Nullable Node<K, V> next) {
    return new TreeNode<>(hash, key, value, next);
  }

  /**
   * For treeifyBin
   */
  static <K, V> TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
    return new TreeNode<>(p.hash, p.key, p.value, next);
  }

  /**
   * Basic hash bin node, used for most entries. (See below for TreeNode subclass,
   * and in LinkedHashMap for its Entry subclass.)
   *
   * @param <K> the type of the key
   * @param <V> the type of the value
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
      return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
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
        return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
      }

      return false;
    }
  }

  static final class TreeNode<K, V> extends Node<K, V> {
    TreeNode<K, V> parent; // red-black tree links
    TreeNode<K, V> left;
    TreeNode<K, V> right;
    TreeNode<K, V> prev; // needed to unlink next upon deletion
    boolean red;

    TreeNode(int hash, K key, V val, Node<K, V> next) {
      super(hash, key, val, next);
    }

    /**
     * Returns root of tree containing this node.
     */
    TreeNode<K, V> root() {
      for (TreeNode<K, V> r = this, p;;) {
        if ((p = r.parent) == null) {
          return r;
        }

        r = p;
      }
    }

    /**
     * Finds the node starting at root p with the given hash and key. The kc
     * argument caches comparableClassFor(key) upon first use comparing keys.
     */
    TreeNode<K, V> find(int h, Object k, Class<?> kc) {
      TreeNode<K, V> p = this;
      do {
        int ph, dir;
        K pk;
        TreeNode<K, V> pl = p.left, pr = p.right, q;
        if ((ph = p.hash) > h) {
          p = pl;
        } else if (ph < h) {
          p = pr;
        } else if ((pk = p.key) == k || k != null && k.equals(pk)) {
          return p;
        } else if (pl == null) {
          p = pr;
        } else if (pr == null) {
          p = pl;
        } else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != 0) {
          p = dir < 0 ? pl : pr;
        } else if ((q = pr.find(h, k, kc)) != null) {
          return q;
        } else {
          p = pl;
        }
      } while (p != null);

      return null;
    }

    /**
     * Calls find for root node.
     */
    TreeNode<K, V> getTreeNode(int h, Object k) {
      return (this.parent != null ? this.root() : this).find(h, k, null);
    }

    /**
     * Forms tree of the nodes linked from this node.
     */
    void treeify(Node<K, V>[] tab) {
      TreeNode<K, V> root = null;
      for (TreeNode<K, V> x = this, next; x != null; x = next) {
        next = (TreeNode<K, V>) x.next;
        x.left = x.right = null;
        if (root == null) {
          x.parent = null;
          x.red = false;
          root = x;
        } else {
          K k = x.key;
          int h = x.hash;
          Class<?> kc = null;
          for (TreeNode<K, V> p = root;;) {
            int dir, ph;
            K pk = p.key;
            if ((ph = p.hash) > h) {
              dir = -1;
            } else if (ph < h) {
              dir = 1;
            } else if (kc == null && (kc = comparableClassFor(k)) == null
                || (dir = compareComparables(kc, k, pk)) == 0) {
              dir = tieBreakOrder(k, pk);
            }

            TreeNode<K, V> xp = p;
            if ((p = dir <= 0 ? p.left : p.right) == null) {
              x.parent = xp;
              if (dir <= 0) {
                xp.left = x;
              } else {
                xp.right = x;
              }
              root = balanceInsertion(root, x);
              break;
            }
          }
        }
      }

      moveRootToFront(tab, root);
    }

    /**
     * Returns a list of non-TreeNodes replacing those linked from this node.
     */
    Node<K, V> untreeify() {
      Node<K, V> hd = null, tl = null;
      for (Node<K, V> q = this; q != null; q = q.next) {
        Node<K, V> p = replacementNode(q, null);
        if (tl == null) {
          hd = p;
        } else {
          tl.next = p;
        }
        tl = p;
      }

      return hd;
    }

    /**
     * Tree version of putVal.
     */
    TreeNode<K, V> putTreeVal(Node<K, V>[] tab, int h, K k, V v) {
      Class<?> kc = null;
      boolean searched = false;
      TreeNode<K, V> root = this.parent != null ? this.root() : this;
      for (TreeNode<K, V> p = root;;) {
        int dir, ph;
        K pk;
        if ((ph = p.hash) > h) {
          dir = -1;
        } else if (ph < h) {
          dir = 1;
        } else if ((pk = p.key) == k || k != null && k.equals(pk)) {
          return p;
        } else if (kc == null && (kc = comparableClassFor(k)) == null || (dir = compareComparables(kc, k, pk)) == 0) {
          if (!searched) {
            TreeNode<K, V> q, ch;
            searched = true;
            if ((ch = p.left) != null && (q = ch.find(h, k, kc)) != null
                || (ch = p.right) != null && (q = ch.find(h, k, kc)) != null) {
              return q;
            }
          }
          dir = tieBreakOrder(k, pk);
        }

        TreeNode<K, V> xp = p;
        if ((p = dir <= 0 ? p.left : p.right) == null) {
          Node<K, V> xpn = xp.next;
          TreeNode<K, V> x = newTreeNode(h, k, v, xpn);
          if (dir <= 0) {
            xp.left = x;
          } else {
            xp.right = x;
          }
          xp.next = x;
          x.parent = x.prev = xp;
          if (xpn != null) {
            ((TreeNode<K, V>) xpn).prev = x;
          }
          moveRootToFront(tab, balanceInsertion(root, x));
          return null;
        }
      }
    }

    /**
     * Returns x's Class if it is of the form "class C implements Comparable<C>",
     * else null.
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
          for (Type type : ts) {
            if ((t = type) instanceof ParameterizedType && (p = (ParameterizedType) t).getRawType() == Comparable.class
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
    @SuppressWarnings({ "rawtypes", "unchecked" }) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
      return x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x);
    }

    /**
     * Tie-breaking utility for ordering insertions when equal hashCodes and
     * non-comparable. We don't require a total order, just a consistent insertion
     * rule to maintain equivalence across rebalancings. Tie-breaking further than
     * necessary simplifies testing a bit.
     */
    static int tieBreakOrder(Object a, Object b) {
      int d;
      if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0) {
        d = System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
      }

      return d;
    }

    /**
     * Removes the given node, that must be present before this call. This is
     * messier than typical red-black deletion code because we cannot swap the
     * contents of an interior node with a leaf successor that is pinned by "next"
     * pointers that are accessible independently during traversal. So instead we
     * swap the tree linkages. If the current tree appears to have too few nodes,
     * the bin is converted back to a plain bin. (The test triggers somewhere
     * between 2 and 6 nodes, depending on tree structure).
     */
    void removeTreeNode(Node<K, V>[] tab, boolean movable) {
      int n;
      if (tab == null || (n = tab.length) == 0) {
        return;
      }
      int index = n - 1 & this.hash;
      TreeNode<K, V> first = (TreeNode<K, V>) tab[index], root = first, rl;
      TreeNode<K, V> succ = (TreeNode<K, V>) this.next, pred = this.prev;
      if (pred == null) {
        tab[index] = first = succ;
      } else {
        pred.next = succ;
      }
      if (succ != null) {
        succ.prev = pred;
      }
      if (first == null) {
        return;
      }
      if (root.parent != null) {
        root = root.root();
      }
      if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) {
        tab[index] = first.untreeify(); // too small
        return;
      }

      TreeNode<K, V> p = this, pl = this.left, pr = this.right, replacement;
      if (pl != null && pr != null) {
        TreeNode<K, V> s = pr, sl;
        while ((sl = s.left) != null) {
          s = sl;
        }
        boolean c = s.red;
        s.red = p.red;
        p.red = c; // swap colors
        TreeNode<K, V> sr = s.right;
        TreeNode<K, V> pp = p.parent;
        if (s == pr) { // p was s's direct parent
          p.parent = s;
          s.right = p;
        } else {
          TreeNode<K, V> sp = s.parent;
          if ((p.parent = sp) != null) {
            if (s == sp.left) {
              sp.left = p;
            } else {
              sp.right = p;
            }
          }
          if ((s.right = pr) != null) {
            pr.parent = s;
          }
        }
        p.left = null;
        if ((p.right = sr) != null) {
          sr.parent = p;
        }
        if ((s.left = pl) != null) {
          pl.parent = s;
        }
        if ((s.parent = pp) == null) {
          root = s;
        } else if (p == pp.left) {
          pp.left = s;
        } else {
          pp.right = s;
        }
        if (sr != null) {
          replacement = sr;
        } else {
          replacement = p;
        }
      } else if (pl != null) {
        replacement = pl;
      } else if (pr != null) {
        replacement = pr;
      } else {
        replacement = p;
      }
      if (replacement != p) {
        TreeNode<K, V> pp = replacement.parent = p.parent;
        if (pp == null) {
          root = replacement;
        } else if (p == pp.left) {
          pp.left = replacement;
        } else {
          pp.right = replacement;
        }
        p.left = p.right = p.parent = null;
      }

      TreeNode<K, V> r = p.red ? root : balanceDeletion(root, replacement);

      if (replacement == p) { // detach
        TreeNode<K, V> pp = p.parent;
        p.parent = null;
        if (pp != null) {
          if (p == pp.left) {
            pp.left = null;
          } else if (p == pp.right) {
            pp.right = null;
          }
        }
      }
      if (movable) {
        moveRootToFront(tab, r);
      }
    }

    /**
     * Ensures that the given root is the first node of its bin.
     */
    static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
      int n;
      if (root != null && tab != null && (n = tab.length) > 0) {
        int index = n - 1 & root.hash;
        TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
        if (root != first) {
          Node<K, V> rn;
          tab[index] = root;
          TreeNode<K, V> rp = root.prev;
          if ((rn = root.next) != null) {
            ((TreeNode<K, V>) rn).prev = rp;
          }
          if (rp != null) {
            rp.next = rn;
          }
          if (first != null) {
            first.prev = root;
          }
          root.next = first;
          root.prev = null;
        }
        assert checkInvariants(root);
      }
    }

    /**
     * Splits nodes in a tree bin into lower and upper tree bins, or untreeifies if
     * now too small. Called only from resize; see above discussion about split bits
     * and indices.
     *
     * @param tab the table for recording bin heads
     * @param index the index of the table being split
     * @param bit the bit of hash to split on
     */
    void split(Node<K, V>[] tab, int index, int bit) {
      TreeNode<K, V> b = this;
      // Relink into lo and hi lists, preserving order
      TreeNode<K, V> loHead = null, loTail = null;
      TreeNode<K, V> hiHead = null, hiTail = null;
      int lc = 0, hc = 0;
      for (TreeNode<K, V> e = b, next; e != null; e = next) {
        next = (TreeNode<K, V>) e.next;
        e.next = null;
        if ((e.hash & bit) == 0) {
          if ((e.prev = loTail) == null) {
            loHead = e;
          } else {
            loTail.next = e;
          }
          loTail = e;
          ++lc;
        } else {
          if ((e.prev = hiTail) == null) {
            hiHead = e;
          } else {
            hiTail.next = e;
          }
          hiTail = e;
          ++hc;
        }
      }

      if (loHead != null) {
        if (lc <= UNTREEIFY_THRESHOLD) {
          tab[index] = loHead.untreeify();
        } else {
          tab[index] = loHead;
          if (hiHead != null) {
            loHead.treeify(tab);
          }
        }
      }
      if (hiHead != null) {
        if (hc <= UNTREEIFY_THRESHOLD) {
          tab[index + bit] = hiHead.untreeify();
        } else {
          tab[index + bit] = hiHead;
          if (loHead != null) {
            hiHead.treeify(tab);
          }
        }
      }
    }

    /* ------------------------------------------------------------ */
    // Red-black tree methods, all adapted from CLR

    static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
      TreeNode<K, V> r, pp, rl;
      if (p != null && (r = p.right) != null) {
        if ((rl = p.right = r.left) != null) {
          rl.parent = p;
        }
        if ((pp = r.parent = p.parent) == null) {
          (root = r).red = false;
        } else if (pp.left == p) {
          pp.left = r;
        } else {
          pp.right = r;
        }
        r.left = p;
        p.parent = r;
      }
      return root;
    }

    static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
      TreeNode<K, V> l, pp, lr;
      if (p != null && (l = p.left) != null) {
        if ((lr = p.left = l.right) != null) {
          lr.parent = p;
        }
        if ((pp = l.parent = p.parent) == null) {
          (root = l).red = false;
        } else if (pp.right == p) {
          pp.right = l;
        } else {
          pp.left = l;
        }
        l.right = p;
        p.parent = l;
      }
      return root;
    }

    static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
      x.red = true;
      for (TreeNode<K, V> xp, xpp, xppl, xppr;;) {
        if ((xp = x.parent) == null) {
          x.red = false;
          return x;
        } else if (!xp.red || (xpp = xp.parent) == null) {
          return root;
        }
        if (xp == (xppl = xpp.left)) {
          if ((xppr = xpp.right) != null && xppr.red) {
            xppr.red = false;
            xp.red = false;
            xpp.red = true;
            x = xpp;
          } else {
            if (x == xp.right) {
              root = rotateLeft(root, x = xp);
              xpp = (xp = x.parent) == null ? null : xp.parent;
            }
            if (xp != null) {
              xp.red = false;
              if (xpp != null) {
                xpp.red = true;
                root = rotateRight(root, xpp);
              }
            }
          }
        } else {
          if (xppl != null && xppl.red) {
            xppl.red = false;
            xp.red = false;
            xpp.red = true;
            x = xpp;
          } else {
            if (x == xp.left) {
              root = rotateRight(root, x = xp);
              xpp = (xp = x.parent) == null ? null : xp.parent;
            }
            if (xp != null) {
              xp.red = false;
              if (xpp != null) {
                xpp.red = true;
                root = rotateLeft(root, xpp);
              }
            }
          }
        }
      }
    }

    static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
      for (TreeNode<K, V> xp, xpl, xpr;;) {
        if (x == null || x == root) {
          return root;
        } else if ((xp = x.parent) == null) {
          x.red = false;
          return x;
        } else if (x.red) {
          x.red = false;
          return root;
        } else if ((xpl = xp.left) == x) {
          if ((xpr = xp.right) != null && xpr.red) {
            xpr.red = false;
            xp.red = true;
            root = rotateLeft(root, xp);
            xpr = (xp = x.parent) == null ? null : xp.right;
          }
          if (xpr == null) {
            x = xp;
          } else {
            TreeNode<K, V> sl = xpr.left, sr = xpr.right;
            if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
              xpr.red = true;
              x = xp;
            } else {
              if (sr == null || !sr.red) {
                if (sl != null) {
                  sl.red = false;
                }
                xpr.red = true;
                root = rotateRight(root, xpr);
                xpr = (xp = x.parent) == null ? null : xp.right;
              }
              if (xpr != null) {
                xpr.red = xp != null && xp.red;
                if ((sr = xpr.right) != null) {
                  sr.red = false;
                }
              }
              if (xp != null) {
                xp.red = false;
                root = rotateLeft(root, xp);
              }
              x = root;
            }
          }
        } else { // symmetric
          if (xpl != null && xpl.red) {
            xpl.red = false;
            xp.red = true;
            root = rotateRight(root, xp);
            xpl = (xp = x.parent) == null ? null : xp.left;
          }
          if (xpl == null) {
            x = xp;
          } else {
            TreeNode<K, V> sl = xpl.left, sr = xpl.right;
            if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
              xpl.red = true;
              x = xp;
            } else {
              if (sl == null || !sl.red) {
                if (sr != null) {
                  sr.red = false;
                }
                xpl.red = true;
                root = rotateLeft(root, xpl);
                xpl = (xp = x.parent) == null ? null : xp.left;
              }
              if (xpl != null) {
                xpl.red = xp != null && xp.red;
                if ((sl = xpl.left) != null) {
                  sl.red = false;
                }
              }
              if (xp != null) {
                xp.red = false;
                root = rotateRight(root, xp);
              }
              x = root;
            }
          }
        }
      }
    }

    /**
     * Recursive invariant check
     */
    static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
      TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
      if (tb != null && tb.next != t) {
        return false;
      }
      if (tn != null && tn.prev != t) {
        return false;
      }
      if (tp != null && t != tp.left && t != tp.right) {
        return false;
      }
      if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
        return false;
      }
      if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
        return false;
      }
      if (t.red && tl != null && tl.red && tr != null && tr.red) {
        return false;
      }
      if (tl != null && !checkInvariants(tl)) {
        return false;
      }
      return tr == null || checkInvariants(tr);
    }
  }

  static class Nodes<K, V> {

    final float loadFactor;
    final Hasher<Object> hasher;
    int size;
    int modCount;
    int threshold;
    Node<K, V>[] table;

    Nodes(int initialCapacity, float loadFactor, Hasher<Object> hasher) {
      this.threshold = tableSizeFor(initialCapacity);
      this.loadFactor = loadFactor;
      this.hasher = hasher;
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    static int tableSizeFor(int cap) {
      int n = cap - 1;
      n |= n >>> 1;
      n |= n >>> 2;
      n |= n >>> 4;
      n |= n >>> 8;
      n |= n >>> 16;
      return n < 0 ? 1 : n >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : n + 1;
    }

    Node<K, V> get(Object key) {
      int hash = this.hasher.hash(key);
      Node<K, V>[] tab;
      Node<K, V> first, e;
      int n;
      K k;
      if ((tab = this.table) != null && (n = tab.length) > 0 && (first = tab[n - 1 & hash]) != null) {
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || key != null && key.equals(k))) {
          return first;
        }
        if ((e = first.next) != null) {
          if (first instanceof TreeNode) {
            return ((TreeNode<K, V>) first).getTreeNode(hash, key);
          }
          do {
            if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k))) {
              return e;
            }
          } while ((e = e.next) != null);
        }
      }
      return null;
    }

    Optional<V> put(K key, V value, boolean onlyIfAbsent) {
      int hash = this.hasher.hash(key);
      Node<K, V>[] tab;
      Node<K, V> p;
      int n, i;
      if ((tab = this.table) == null || (n = tab.length) == 0) {
        n = (tab = this.resize()).length;
      }
      if ((p = tab[i = n - 1 & hash]) == null) {
        tab[i] = newNode(hash, key, value, null);
      } else {
        Node<K, V> e;
        K k;
        if (p.hash == hash && ((k = p.key) == key || key != null && key.equals(k))) {
          e = p;
        } else if (p instanceof TreeNode) {
          e = ((TreeNode<K, V>) p).putTreeVal(tab, hash, key, value);
        } else {
          for (int binCount = 0;; ++binCount) {
            if ((e = p.next) == null) {
              p.next = newNode(hash, key, value, null);
              if (binCount >= TREEIFY_THRESHOLD - 1) {
                this.treeify(hash);
              }
              break;
            }
            if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k))) {
              break;
            }
            p = e;
          }
        }
        if (e != null) { // existing mapping for key
          V oldValue = e.value;
          if (!onlyIfAbsent) {
            e.value = value;
          }
          return Optional.ofNullable(oldValue);
        }
      }
      ++this.modCount;
      if (++this.size > this.threshold) {
        this.resize();
      }
      return null;
    }

    Node<K, V> remove(Object key, Object value, boolean matchValue, boolean movable) {
      int hash = this.hasher.hash(key);
      Node<K, V>[] tab;
      Node<K, V> p;
      int n, index;
      if ((tab = this.table) != null && (n = tab.length) > 0 && (p = tab[index = n - 1 & hash]) != null) {
        Node<K, V> node = null, e;
        K k;
        V v;
        if (p.hash == hash && ((k = p.key) == key || key != null && key.equals(k))) {
          node = p;
        } else if ((e = p.next) != null) {
          if (p instanceof TreeNode) {
            node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
          } else {
            do {
              if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k))) {
                node = e;
                break;
              }
              p = e;
            } while ((e = e.next) != null);
          }
        }
        if (node != null && (!matchValue || (v = node.value) == value || value != null && value.equals(v))) {
          if (node instanceof TreeNode) {
            ((TreeNode<K, V>) node).removeTreeNode(tab, movable);
          } else if (node == p) {
            tab[index] = node.next;
          } else {
            p.next = node.next;
          }
          ++this.modCount;
          --this.size;
          return node;
        }
      }
      return null;
    }

    void clear() {
      Node<K, V>[] tab;
      this.modCount++;
      if ((tab = this.table) != null && this.size > 0) {
        this.size = 0;
        for (int i = 0; i < tab.length; ++i) {
          tab[i] = null;
        }
      }
    }

    Node<K, V>[] resize() {
      Node<K, V>[] oldTab = this.table;
      int oldCap = oldTab == null ? 0 : oldTab.length;
      int oldThr = this.threshold;
      int newCap, newThr = 0;
      if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
          this.threshold = Integer.MAX_VALUE;
          return oldTab;
        } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) {
          newThr = oldThr << 1; // double threshold
        }
      } else if (oldThr > 0) {
        newCap = oldThr;
      } else { // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
      }
      if (newThr == 0) {
        float ft = newCap * this.loadFactor;
        newThr = newCap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE;
      }
      this.threshold = newThr;

      @SuppressWarnings({ "unchecked" })
      Node<K, V>[] newTab = new Node[newCap];
      this.table = newTab;

      if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
          Node<K, V> e;
          if ((e = oldTab[j]) != null) {
            oldTab[j] = null;
            if (e.next == null) {
              newTab[e.hash & newCap - 1] = e;
            } else if (e instanceof TreeNode) {
              ((TreeNode<K, V>) e).split(newTab, j, oldCap);
            } else { // preserve order
              Node<K, V> loHead = null, loTail = null;
              Node<K, V> hiHead = null, hiTail = null;
              Node<K, V> next;
              do {
                next = e.next;
                if ((e.hash & oldCap) == 0) {
                  if (loTail == null) {
                    loHead = e;
                  } else {
                    loTail.next = e;
                  }
                  loTail = e;
                } else {
                  if (hiTail == null) {
                    hiHead = e;
                  } else {
                    hiTail.next = e;
                  }
                  hiTail = e;
                }
              } while ((e = next) != null);
              if (loTail != null) {
                loTail.next = null;
                newTab[j] = loHead;
              }
              if (hiTail != null) {
                hiTail.next = null;
                newTab[j + oldCap] = hiHead;
              }
            }
          }
        }
      }
      return newTab;
    }

    void treeify(int hash) { // similar to HashMap#treeifyBin()
      int n, index;
      Node<K, V> e;
      if (this.table == null || (n = this.table.length) < MIN_TREEIFY_CAPACITY) {
        this.resize();
      } else if ((e = this.table[index = n - 1 & hash]) != null) {
        TreeNode<K, V> hd = null, tl = null;
        do {
          TreeNode<K, V> p = replacementTreeNode(e, null);
          if (tl == null) {
            hd = p;
          } else {
            p.prev = tl;
            tl.next = p;
          }
          tl = p;
        } while ((e = e.next) != null);
        if ((this.table[index] = hd) != null) {
          hd.treeify(this.table);
        }
      }
    }
  }

  /* ---------------- Spliterators -------------- */

  static class HashedMapSpliterator<K, V> {
    final Nodes<K, V> bi;
    final Nodes<V, K> di;
    Node<K, V> current; // current node
    int index; // current index, modified on advance/split
    int fence; // one past last index
    int est; // size estimate
    int expectedModCount; // for comodification checks

    HashedMapSpliterator(Nodes<K, V> b, Nodes<V, K> d, int origin, int fence, int est, int expectedModCount) {
      this.bi = b;
      this.di = d;
      this.index = origin;
      this.fence = fence;
      this.est = est;
      this.expectedModCount = expectedModCount;
    }

    final int getFence() { // initialize fence and size on first use
      int hi;
      if ((hi = this.fence) < 0) {
        Nodes<K, V> b = this.bi;
        this.est = b.size;
        this.expectedModCount = b.modCount + this.di.modCount;
        Node<K, V>[] tab = b.table;
        hi = this.fence = tab == null ? 0 : tab.length;
      }
      return hi;
    }

    public final long estimateSize() {
      this.getFence(); // force init
      return this.est;
    }
  }

  static final class KeySpliterator<K, V> extends HashedMapSpliterator<K, V> implements Spliterator<K> {
    KeySpliterator(Nodes<K, V> b, Nodes<V, K> d, int origin, int fence, int est, int expectedModCount) {
      super(b, d, origin, fence, est, expectedModCount);
    }

    @Override
    public KeySpliterator<K, V> trySplit() {
      int hi = this.getFence(), lo = this.index, mid = lo + hi >>> 1;
      return lo >= mid || this.current != null ? null
          : new KeySpliterator<>(this.bi, this.di, lo, this.index = mid, this.est >>>= 1, this.expectedModCount);
    }

    @Override
    public void forEachRemaining(Consumer<? super K> action) {
      int i, hi, mc;
      if (action == null) {
        throw new NullPointerException();
      }
      Nodes<K, V> b = this.bi;
      Nodes<V, K> d = this.di;
      Node<K, V>[] tab = b.table;
      if ((hi = this.fence) < 0) {
        mc = this.expectedModCount = b.modCount + d.modCount;
        hi = this.fence = tab == null ? 0 : tab.length;
      } else {
        mc = this.expectedModCount;
      }
      if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
        Node<K, V> p = this.current;
        this.current = null;
        do {
          if (p == null) {
            p = tab[i++];
          } else {
            action.accept(p.key);
            p = p.next;
          }
        } while (p != null || i < hi);
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super K> action) {
      int hi;
      if (action == null) {
        throw new NullPointerException();
      }
      Node<K, V>[] tab = this.bi.table;
      if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
        while (this.current != null || this.index < hi) {
          if (this.current == null) {
            this.current = tab[this.index++];
          } else {
            K k = this.current.key;
            this.current = this.current.next;
            action.accept(k);
            if (this.bi.modCount + this.di.modCount != this.expectedModCount) {
              throw new ConcurrentModificationException();
            }
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public int characteristics() {
      return (this.fence < 0 || this.est == this.bi.size ? Spliterator.SIZED : 0) | Spliterator.DISTINCT;
    }
  }

  static final class ValueSpliterator<K, V> extends HashedMapSpliterator<K, V> implements Spliterator<V> {
    ValueSpliterator(Nodes<K, V> b, Nodes<V, K> d, int origin, int fence, int est, int expectedModCount) {
      super(b, d, origin, fence, est, expectedModCount);
    }

    @Override
    public ValueSpliterator<K, V> trySplit() {
      int hi = this.getFence(), lo = this.index, mid = lo + hi >>> 1;
      return lo >= mid || this.current != null ? null
          : new ValueSpliterator<>(this.bi, this.di, lo, this.index = mid, this.est >>>= 1, this.expectedModCount);
    }

    @Override
    public void forEachRemaining(Consumer<? super V> action) {
      int i, hi, mc;
      if (action == null) {
        throw new NullPointerException();
      }
      Nodes<K, V> b = this.bi;
      Nodes<V, K> d = this.di;
      Node<K, V>[] tab = b.table;
      if ((hi = this.fence) < 0) {
        mc = this.expectedModCount = b.modCount + d.modCount;
        hi = this.fence = tab == null ? 0 : tab.length;
      } else {
        mc = this.expectedModCount;
      }
      if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
        Node<K, V> p = this.current;
        this.current = null;
        do {
          if (p == null) {
            p = tab[i++];
          } else {
            action.accept(p.value);
            p = p.next;
          }
        } while (p != null || i < hi);
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super V> action) {
      int hi;
      if (action == null) {
        throw new NullPointerException();
      }
      Node<K, V>[] tab = this.bi.table;
      if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
        while (this.current != null || this.index < hi) {
          if (this.current == null) {
            this.current = tab[this.index++];
          } else {
            V v = this.current.value;
            this.current = this.current.next;
            action.accept(v);
            if (this.bi.modCount + this.di.modCount != this.expectedModCount) {
              throw new ConcurrentModificationException();
            }
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public int characteristics() {
      return this.fence < 0 || this.est == this.bi.size ? Spliterator.SIZED : 0;
    }
  }

  static final class EntrySpliterator<K, V> extends HashedMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
    EntrySpliterator(Nodes<K, V> b, Nodes<V, K> d, int origin, int fence, int est, int expectedModCount) {
      super(b, d, origin, fence, est, expectedModCount);
    }

    @Override
    public EntrySpliterator<K, V> trySplit() {
      int hi = this.getFence(), lo = this.index, mid = lo + hi >>> 1;
      return lo >= mid || this.current != null ? null
          : new EntrySpliterator<>(this.bi, this.di, lo, this.index = mid, this.est >>>= 1, this.expectedModCount);
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
      int i, hi, mc;
      if (action == null) {
        throw new NullPointerException();
      }
      Nodes<K, V> b = this.bi;
      Nodes<V, K> d = this.di;
      Node<K, V>[] tab = b.table;
      if ((hi = this.fence) < 0) {
        mc = this.expectedModCount = b.modCount + d.modCount;
        hi = this.fence = tab == null ? 0 : tab.length;
      } else {
        mc = this.expectedModCount;
      }
      if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
        Node<K, V> p = this.current;
        this.current = null;
        do {
          if (p == null) {
            p = tab[i++];
          } else {
            action.accept(p);
            p = p.next;
          }
        } while (p != null || i < hi);
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
      int hi;
      if (action == null) {
        throw new NullPointerException();
      }
      Node<K, V>[] tab = this.bi.table;
      if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
        while (this.current != null || this.index < hi) {
          if (this.current == null) {
            this.current = tab[this.index++];
          } else {
            Node<K, V> e = this.current;
            this.current = this.current.next;
            action.accept(e);
            if (this.bi.modCount + this.di.modCount != this.expectedModCount) {
              throw new ConcurrentModificationException();
            }
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public int characteristics() {
      return (this.fence < 0 || this.est == this.bi.size ? Spliterator.SIZED : 0) | Spliterator.DISTINCT;
    }
  }

  /* ---------------- Iterators -------------- */

  abstract static class HashedIterator<K, V> {
    final Nodes<K, V> nodes;
    final Nodes<V, K> inverse;
    Node<K, V> next; // next entry to return
    Node<K, V> current; // current entry
    int expectedModCount; // for fast-fail
    int index; // current slot

    HashedIterator(Nodes<K, V> nodes, Nodes<V, K> inverse) {
      this.nodes = nodes;
      this.inverse = inverse;

      Node<K, V>[] t = nodes.table;
      this.expectedModCount = nodes.modCount + inverse.modCount;

      this.current = this.next = null;
      this.index = 0;
      if (t != null && nodes.size > 0) { // advance to first entry
        do {
        } while (this.index < t.length && (this.next = t[this.index++]) == null);
      }
    }

    public final boolean hasNext() {
      return this.next != null;
    }

    final Node<K, V> nextNode() {
      Node<K, V>[] t;
      Node<K, V> e = this.next;
      if (this.nodes.modCount + this.inverse.modCount != this.expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (e == null) {
        throw new NoSuchElementException();
      }
      if ((this.next = (this.current = e).next) == null && (t = this.nodes.table) != null) {
        do {
        } while (this.index < t.length && (this.next = t[this.index++]) == null);
      }
      return e;
    }

    public final void remove() {
      Node<K, V> p = this.current;
      if (p == null) {
        throw new IllegalStateException();
      }
      if (this.nodes.modCount + this.inverse.modCount != this.expectedModCount) {
        throw new ConcurrentModificationException();
      }
      this.current = null;
      K key = p.key;
      Node<K, V> removed = this.nodes.remove(key, null, false, false);
      if (removed != null) {
        this.inverse.remove(removed.value, null, false, false);
      }
      this.expectedModCount = this.nodes.modCount + this.inverse.modCount;
    }
  }

  final class KeyIterator extends HashedIterator<K, V> implements Iterator<K> {
    KeyIterator() {
      super(HashedBidirectionalMap.this.bi, HashedBidirectionalMap.this.di);
    }

    @Override
    public K next() {
      return this.nextNode().key;
    }
  }

  final class ValueIterator extends HashedIterator<K, V> implements Iterator<V> {
    ValueIterator() {
      super(HashedBidirectionalMap.this.bi, HashedBidirectionalMap.this.di);
    }

    @Override
    public V next() {
      return this.nextNode().value;
    }
  }

  final class EntryIterator extends HashedIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    EntryIterator() {
      super(HashedBidirectionalMap.this.bi, HashedBidirectionalMap.this.di);
    }

    @Override
    public Map.Entry<K, V> next() {
      return this.nextNode();
    }
  }

  final class InverseEntryIterator extends HashedIterator<V, K> implements Iterator<Map.Entry<V, K>> {
    InverseEntryIterator() {
      super(HashedBidirectionalMap.this.di, HashedBidirectionalMap.this.bi);
    }

    @Override
    public Map.Entry<V, K> next() {
      return this.nextNode();
    }
  }

  /* ---------------- Sets -------------- */

  final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    @Override
    public int size() {
      return HashedBidirectionalMap.this.bi.size;
    }

    @Override
    public void clear() {
      HashedBidirectionalMap.this.clear();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Node<K, V> candidate = HashedBidirectionalMap.this.bi.get(key);
      return candidate != null && candidate.equals(e);
    }

    @Override
    public boolean remove(Object o) {
      if (o instanceof Map.Entry) {
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        Object key = e.getKey();
        Object value = e.getValue();
        return HashedBidirectionalMap.this.remove(key, value);
      }
      return false;
    }

    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
      return new EntrySpliterator<>(HashedBidirectionalMap.this.bi, HashedBidirectionalMap.this.di, 0, -1, 0, 0);
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
      Node<K, V>[] tab;
      if (action == null) {
        throw new NullPointerException();
      }

      Nodes<K, V> b = HashedBidirectionalMap.this.bi;
      Nodes<V, K> d = HashedBidirectionalMap.this.di;
      if (b.size > 0 && (tab = b.table) != null) {
        int mc = b.modCount + d.modCount;
        for (int i = 0; i < tab.length; ++i) {
          for (Node<K, V> e = tab[i]; e != null; e = e.next) {
            action.accept(e);
          }
        }
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }
  }

  final class InverseEntrySet extends AbstractSet<Map.Entry<V, K>> {
    @Override
    public int size() {
      return HashedBidirectionalMap.this.bi.size;
    }

    @Override
    public void clear() {
      HashedBidirectionalMap.this.clear();
    }

    @Override
    public Iterator<Map.Entry<V, K>> iterator() {
      return new InverseEntryIterator();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Node<V, K> candidate = HashedBidirectionalMap.this.di.get(key);
      return candidate != null && candidate.equals(e);
    }

    @Override
    public boolean remove(Object o) {
      if (o instanceof Map.Entry) {
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        Object key = e.getKey();
        Object value = e.getValue();
        return HashedBidirectionalMap.this.remove(value, key);
      }
      return false;
    }

    @Override
    public Spliterator<Map.Entry<V, K>> spliterator() {
      return new EntrySpliterator<>(HashedBidirectionalMap.this.di, HashedBidirectionalMap.this.bi, 0, -1, 0, 0);
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<V, K>> action) {
      Node<V, K>[] tab;
      if (action == null) {
        throw new NullPointerException();
      }

      Nodes<K, V> b = HashedBidirectionalMap.this.bi;
      Nodes<V, K> d = HashedBidirectionalMap.this.di;
      if (b.size > 0 && (tab = d.table) != null) {
        int mc = b.modCount + d.modCount;
        for (int i = 0; i < tab.length; ++i) {
          for (Node<V, K> e = tab[i]; e != null; e = e.next) {
            action.accept(e);
          }
        }
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }
  }

  final class KeySet extends AbstractSet<K> {
    @Override
    public int size() {
      return HashedBidirectionalMap.this.bi.size;
    }

    @Override
    public void clear() {
      HashedBidirectionalMap.this.clear();
    }

    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    @Override
    public boolean contains(Object o) {
      return HashedBidirectionalMap.this.containsKey(o);
    }

    @Override
    public boolean remove(Object key) {
      return HashedBidirectionalMap.this.remove(key) != null;
    }

    @Override
    public Spliterator<K> spliterator() {
      return new KeySpliterator<>(HashedBidirectionalMap.this.bi, HashedBidirectionalMap.this.di, 0, -1, 0, 0);
    }

    @Override
    public void forEach(Consumer<? super K> action) {
      Node<K, V>[] tab;
      if (action == null) {
        throw new NullPointerException();
      }
      Nodes<K, V> b = HashedBidirectionalMap.this.bi;
      Nodes<V, K> d = HashedBidirectionalMap.this.di;
      if (b.size > 0 && (tab = b.table) != null) {
        int mc = b.modCount + d.modCount;
        for (int i = 0; i < tab.length; ++i) {
          for (Node<K, V> e = tab[i]; e != null; e = e.next) {
            action.accept(e.key);
          }
        }
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }
  }

  final class ValueSet extends AbstractSet<V> {
    @Override
    public int size() {
      return HashedBidirectionalMap.this.bi.size;
    }

    @Override
    public void clear() {
      HashedBidirectionalMap.this.clear();
    }

    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Override
    public boolean contains(Object o) {
      return HashedBidirectionalMap.this.containsValue(o);
    }

    @Override
    public boolean remove(Object key) {
      Node<V, K> e = HashedBidirectionalMap.this.di.remove(key, null, false, true);
      if (e != null) {
        K value = e.value;
        HashedBidirectionalMap.this.bi.remove(value, null, false, true);
      }

      return e != null;
    }

    @Override
    public Spliterator<V> spliterator() {
      return new KeySpliterator<>(HashedBidirectionalMap.this.di, HashedBidirectionalMap.this.bi, 0, -1, 0, 0);
    }

    @Override
    public void forEach(Consumer<? super V> action) {
      Node<V, K>[] tab;
      if (action == null) {
        throw new NullPointerException();
      }
      Nodes<K, V> b = HashedBidirectionalMap.this.bi;
      Nodes<V, K> d = HashedBidirectionalMap.this.di;
      if (b.size > 0 && (tab = d.table) != null) {
        int mc = b.modCount + d.modCount;
        for (int i = 0; i < tab.length; ++i) {
          for (Node<V, K> e = tab[i]; e != null; e = e.next) {
            action.accept(e.key);
          }
        }
        if (b.modCount + d.modCount != mc) {
          throw new ConcurrentModificationException();
        }
      }
    }
  }
}
