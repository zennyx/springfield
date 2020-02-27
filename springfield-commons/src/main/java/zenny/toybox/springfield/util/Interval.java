package zenny.toybox.springfield.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

public final class Interval<E extends Comparable<E>> {

  private final boolean empty;

  private final Class<E> type;

  @Nullable
  private final Endpoint<E> left;

  @Nullable
  private final Endpoint<E> right;

  private Interval(Class<E> type) {
    this(type, null, null);
  }

  private Interval(Class<E> type, @Nullable Endpoint<E> left, @Nullable Endpoint<E> right) {
    this.empty = isEmptyInterval(left, right);
    this.type = type;
    this.left = this.empty ? null : left;
    this.right = this.empty ? null : right;
  }

  private static <E extends Comparable<E>> boolean isEmptyInterval(@Nullable Endpoint<E> left,
      @Nullable Endpoint<E> right) {
    if (left == null || right == null) {
      return true;
    }

    // assume a < b
    // [b, a] = (b, a) = [b, a) = (b, a] = Ø
    int comparasion = left.compareTo(right);
    if (comparasion > 0) {
      return true;
    }

    // (a, a) = [a, a) = (a, a] = Ø
    if (comparasion == 0 && (left.bounded == false || right.bounded == false)) {
      return true;
    }

    return false;
  }

  public Intervals<E> difference(Interval<E> other) {
    Assert.notNull(other, "The given interval must not be null");

    if (this.empty || other.empty) {
      return new Intervals<>(this);
    }

    Collection<Interval<E>> intervals = new HashSet<>();
    if (!this.hasIntersectionWith(other)) {
      intervals.add(this);
      return new Intervals<>(intervals);
    }

    if (this.isSubsetOf(other)) {
      intervals.add(new Interval<>(this.type));
      return new Intervals<>(intervals);
    }

    if (this.left.compareTo(other.left) < 0) {
      intervals
          .add(new Interval<>(this.type, this.left, Endpoint.copyOf(other.left, other.isLeftClosed() ? false : true)));
    }
    if (this.right.compareTo(other.right) > 0) {
      intervals.add(
          new Interval<>(this.type, Endpoint.copyOf(other.right, other.isRightClosed() ? false : true), this.right));
    }

    return new Intervals<>(intervals);
  }

  public Intervals<E> difference(Intervals<E> others) {
    return Intervals.differenceBetween(this, others);
  }

  public Intervals<E> symmetricDifference(Interval<E> other) {
    return Intervals.symmetricDifferenceBetween(this, other);
  }

  public Interval<E> intersect(Interval<E> other) {
    Assert.notNull(other, "The given interval must not be null");

    if (this.empty) {
      return this;
    }

    if (other.empty) {
      return other;
    }

    if (!this.hasIntersectionWith(other)) {
      return new Interval<>(this.type);
    }

    if (this.isSubsetOf(other)) {
      return this;
    }
    if (this.isSupersetOf(other)) {
      return other;
    }

    int leftComparasion = this.left.compareTo(other.left);
    int rightComparasion = this.right.compareTo(other.right);
    Endpoint<E> leftEnd = leftComparasion >= 0 ? this.left : other.left;
    boolean leftBounded = leftComparasion > 0 ? this.left.bounded
        : leftComparasion < 0 ? other.left.bounded : this.isLeftClosed() && other.isLeftClosed() ? true : false;
    Endpoint<E> rightEnd = rightComparasion >= 0 ? other.right : this.right;
    boolean rightBounded = rightComparasion > 0 ? other.right.bounded
        : leftComparasion < 0 ? this.right.bounded : this.isRightClosed() && other.isRightClosed() ? true : false;

    return new Interval<>(this.type, Endpoint.copyOf(leftEnd, leftBounded), Endpoint.copyOf(rightEnd, rightBounded));
  }

  public Intervals<E> intersect(Intervals<E> others) {
    return Intervals.intersectBetween(others, this);
  }

  public Intervals<E> union(Interval<E> other) {
    Assert.notNull(other, "The given interval must not be null");

    if (this.isEmpty() && other.isEmpty()) {
      return new Intervals<>(Collections.emptySet());
    }

    Collection<Interval<E>> intervals = new HashSet<>();
    if (!this.hasIntersectionWith(other)) {
      intervals.add(this);
      intervals.add(other);
      return new Intervals<>(intervals);
    }

    if (this.isSubsetOf(other)) {
      intervals.add(other);
      return new Intervals<>(intervals);
    }
    if (this.isSupersetOf(other)) {
      intervals.add(this);
      return new Intervals<>(intervals);
    }

    int leftComparasion = this.left.compareTo(other.left);
    int rightComparasion = this.right.compareTo(other.right);
    Endpoint<E> leftEnd = leftComparasion >= 0 ? other.left : this.left;
    boolean leftBound = leftComparasion > 0 ? other.left.bounded
        : leftComparasion < 0 ? this.left.bounded : this.isLeftClosed() || other.isLeftClosed() ? true : false;
    Endpoint<E> rightEnd = rightComparasion >= 0 ? this.right : other.right;
    boolean rightBound = rightComparasion > 0 ? this.right.bounded
        : leftComparasion < 0 ? other.right.bounded : this.isRightClosed() || other.isRightClosed() ? true : false;

    Interval<E> unioned = new Interval<>(this.type, Endpoint.copyOf(leftEnd, leftBound),
        Endpoint.copyOf(rightEnd, rightBound));

    intervals.add(unioned);
    return new Intervals<>(intervals);
  }

  public Intervals<E> union(Intervals<E> others) {
    return Intervals.unionBetween(others, this);
  }

  public boolean isSupersetOf(Interval<E> other) {
    if (other == null) {
      return false;
    }

    if (this.empty) {
      return false;
    }

    if (other.empty) {
      return true;
    }

    int leftComparasion = this.left.compareTo(other.left);
    int rightComparasion = this.right.compareTo(other.right);
    if (leftComparasion > 0 || rightComparasion < 0) {
      return false;
    }
    if (leftComparasion == 0 && this.isLeftOpen() && other.isLeftClosed()
        || rightComparasion == 0 && this.isRightOpen() && other.isRightClosed()) {
      return false;
    }

    return true;
  }

  public boolean isSubsetOf(@Nullable Interval<E> other) {
    if (other == null) {
      return false;
    }

    if (this.empty) { // the empty interval is unique, and, it is a subset of itself
      return true;
    }

    int leftComparasion = this.left.compareTo(other.left);
    int rightComparasion = this.right.compareTo(other.right);
    if (leftComparasion < 0 || rightComparasion > 0) {
      return false;
    }
    if (leftComparasion == 0 && this.isLeftClosed() && other.isLeftOpen()
        || rightComparasion == 0 && this.isRightClosed() && other.isRightOpen()) {
      return false;
    }

    return true;
  }

  public boolean hasIntersectionWith(@Nullable Interval<E> other) {
    if (other == null) {
      return false;
    }

    if (this.empty || other.empty) {
      return true;
    }

    if (this.left.compareTo(other.right) <= 0 && this.right.compareTo(other.left) >= 0) {
      return true;
    }

    return false;
  }

  public boolean contains(@Nullable E value) {
    if (value == null || this.empty) {
      return false;
    }

    Endpoint<E> endpoint = Endpoint.finity(value, true);
    if (this.isDegenerate()) {
      return this.left.compareTo(endpoint) == 0;
    }

    int leftComparasion = endpoint.compareTo(this.left);
    int rightComparasion = endpoint.compareTo(this.right);
    if (leftComparasion < 0 || rightComparasion > 0) {
      return false;
    }

    if (leftComparasion == 0 && this.isLeftOpen()) {
      return false;
    }
    if (rightComparasion == 0 && this.isRightOpen()) {
      return false;
    }

    return true;
  }

  public boolean containsNegativeInfinity() {
    if (this.empty) {
      return false;
    }

    if (this.left.isNegativeInfinity() && this.isLeftClosed()
        || this.right.isNegativeInfinity() && this.isRightClosed()) {
      return true;
    }

    return false;
  }

  public boolean containsPositiveInfinity() {
    if (this.empty) {
      return false;
    }

    if (this.left.isPositiveInfinity() && this.isLeftClosed()
        || this.right.isPositiveInfinity() && this.isRightClosed()) {
      return true;
    }

    return false;
  }

  public boolean isDegenerate() {
    return !this.empty && this.isClosed() && this.left.compareTo(this.right) == 0;
  }

  public boolean isEmpty() {
    return this.empty;
  }

  public boolean isOpen() {
    return this.isLeftOpen() && this.isRightOpen();
  }

  public boolean isLeftOpen() {
    return this.left.bounded == false;
  }

  public boolean isRightOpen() {
    return this.right.bounded == false;
  }

  public boolean isClosed() {
    return this.isLeftClosed() && this.isRightClosed();
  }

  public boolean isLeftClosed() {
    return this.left.bounded == true;
  }

  public boolean isRightClosed() {
    return this.right.bounded == true;
  }

  @Nullable
  public E getLeft() {
    if (this.empty || this.left.isInfinity()) {
      return null;
    }

    return this.left.get();
  }

  @Nullable
  public E getLeft(@Nullable E alias) {
    if (this.empty) {
      return null;
    }

    return this.left.or(alias);
  }

  @Nullable
  public E getLeft(@Nullable Supplier<? extends E> supplier) {
    if (this.empty) {
      return null;
    }

    return this.left.or(supplier);
  }

  @Nullable
  public E getRight() {
    if (this.empty || this.right.isInfinity()) {
      return null;
    }

    return this.right.get();
  }

  @Nullable
  public E getRight(@Nullable E alias) {
    if (this.empty) {
      return null;
    }

    return this.right.or(alias);
  }

  @Nullable
  public E getRight(@Nullable Supplier<? extends E> supplier) {
    if (this.empty) {
      return null;
    }

    return this.right.or(supplier);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.type.hashCode();
    result = prime * result + Boolean.hashCode(this.empty);
    result = prime * result + ObjectUtils.nullSafeHashCode(this.left);
    result = prime * result + ObjectUtils.nullSafeHashCode(this.right);

    return result;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof Interval)) {
      return false;
    }

    Interval<?> otherInterval = (Interval<?>) other;
    return this.type.equals(otherInterval.type) && this.empty == otherInterval.empty
        && ObjectUtils.nullSafeEquals(this.left, otherInterval.left)
        && ObjectUtils.nullSafeEquals(this.right, otherInterval.right);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Interval<");
    builder.append(this.type.getSimpleName()).append(">");

    if (this.empty) {
      builder.append(" Ø");
      return builder.toString();
    }

    builder.append(" ").append(this.left.bounded ? "[" : "(").append(this.left).append(", ").append(this.right)
        .append(this.right.bounded ? "]" : ")");
    return builder.toString();
  }

  public static <E extends Comparable<E>> IntervalBuilder<E> forClass(Class<E> type) {
    return new IntervalBuilder<>(type);
  }

  private static abstract class Endpoint<E extends Comparable<E>> implements Comparable<Endpoint<E>> {

    private final boolean bounded;

    private Endpoint(boolean bounded) {
      this.bounded = bounded;
    }

    public static <E extends Comparable<E>> Endpoint<E> copyOf(Endpoint<E> endpoint, boolean bounded) {
      if (endpoint instanceof Finity) {
        return Endpoint.finity(((Finity<E>) endpoint).element, bounded);
      }

      Infinity<E> infinity = (Infinity<E>) endpoint;
      return Endpoint.infinity(infinity.type, infinity.isPositiveInfinity(), bounded);
    }

    public static <E extends Comparable<E>> Endpoint<E> finity(E element, boolean bounded) {
      return new Finity<>(element, bounded);
    }

    public static <E extends Comparable<E>> Endpoint<E> infinity(Class<E> type, boolean postive, boolean bounded) {
      if (postive) {
        return new PositiveInfinity<>(type, bounded);
      }

      return new NegativeInfinity<>(type, bounded);
    }

    public abstract boolean isFinity();

    public abstract boolean isInfinity();

    public abstract boolean isPositiveInfinity();

    public abstract boolean isNegativeInfinity();

    @Nullable
    public abstract E get();

    @Nullable
    public abstract E or(@Nullable E defaultValue);

    @Nullable
    public abstract E or(@Nullable Supplier<? extends E> supplier);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object other);

    @Override
    public abstract String toString();

    protected final boolean bounded() {
      return this.bounded;
    }
  }

  private static final class Finity<E extends Comparable<E>> extends Endpoint<E> {

    private final E element;

    private Finity(E element, boolean bounded) {
      super(bounded);

      Assert.notNull(element, "Element must not be null");
      this.element = element;
    }

    @Override
    public boolean isFinity() {
      return true;
    }

    @Override
    public boolean isInfinity() {
      return false;
    }

    @Override
    public boolean isPositiveInfinity() {
      return false;
    }

    @Override
    public boolean isNegativeInfinity() {
      return false;
    }

    @Override
    public E get() {
      return this.element;
    }

    @Override
    public E or(@Nullable E defaultValue) {
      return this.element;
    }

    @Override
    public E or(@Nullable Supplier<? extends E> supplier) {
      return this.element;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.element.hashCode();
      result = prime * result + Boolean.hashCode(this.bounded());

      return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof Finity)) {
        return false;
      }

      Finity<?> otherEndPoint = (Finity<?>) other;
      return this.element.equals(otherEndPoint.element) && this.bounded() == otherEndPoint.bounded();
    }

    @Override
    public String toString() {
      return this.element.toString();
    }

    @Override
    public int compareTo(Endpoint<E> other) {
      if (other.isPositiveInfinity()) {
        return -1;
      }

      if (other.isNegativeInfinity()) {
        return 1;
      }

      Finity<E> otherFinity = (Finity<E>) other;
      return this.element.compareTo(otherFinity.element);
    }
  }

  private static abstract class Infinity<E extends Comparable<E>> extends Endpoint<E> {

    private final Class<E> type;

    private Infinity(Class<E> type, boolean bounded) {
      super(bounded);

      Assert.notNull(type, "Type must not be null");
      this.type = type;
    }

    @Override
    public final boolean isFinity() {
      return false;
    }

    @Override
    public final boolean isInfinity() {
      return true;
    }

    @Override
    public final E get() {
      throw new SetTheoryViolationException("Unable to retrieve a concrete value from infinity");
    }

    @Override
    @Nullable
    public final E or(@Nullable E defaultValue) {
      return defaultValue;
    }

    @Override
    @Nullable
    public final E or(@Nullable Supplier<? extends E> supplier) {
      if (supplier == null) {
        return null;
      }

      return supplier.get();
    }

    public final Class<E> type() {
      return this.type;
    }

    @Override
    public final int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.type.hashCode();
      result = prime * result + Boolean.hashCode(this.bounded());

      return result;
    }

    @Override
    public String toString() {
      return "∞";
    }
  }

  private static final class PositiveInfinity<E extends Comparable<E>> extends Infinity<E> {

    private PositiveInfinity(Class<E> type, boolean bounded) {
      super(type, bounded);
    }

    @Override
    public boolean isPositiveInfinity() {
      return true;
    }

    @Override
    public boolean isNegativeInfinity() {
      return false;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof PositiveInfinity)) {
        return false;
      }

      PositiveInfinity<?> otherEndPoint = (PositiveInfinity<?>) other;
      return this.type().equals(otherEndPoint.type()) && this.bounded() == otherEndPoint.bounded();
    }

    @Override
    public String toString() {
      return "+∞";
    }

    @Override
    public int compareTo(Endpoint<E> other) {
      if (other instanceof PositiveInfinity) {
        return 0;
      }

      return 1;
    }
  }

  private static final class NegativeInfinity<E extends Comparable<E>> extends Infinity<E> {

    private NegativeInfinity(Class<E> type, boolean bounded) {
      super(type, bounded);
    }

    @Override
    public boolean isPositiveInfinity() {
      return false;
    }

    @Override
    public boolean isNegativeInfinity() {
      return true;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof NegativeInfinity)) {
        return false;
      }

      NegativeInfinity<?> otherEndPoint = (NegativeInfinity<?>) other;
      return this.type().equals(otherEndPoint.type()) && this.bounded() == otherEndPoint.bounded();
    }

    @Override
    public String toString() {
      return "-∞";
    }

    @Override
    public int compareTo(Endpoint<E> other) {
      if (other instanceof NegativeInfinity) {
        return 0;
      }

      return -1;
    }
  }

  public static final class IntervalBuilder<E extends Comparable<E>> {

    private final Class<E> type;

    private EndpointBuilder<E> lepBuilder;

    private EndpointBuilder<E> repBuilder;

    private IntervalBuilder(Class<E> type) {
      Assert.notNull(type, "Type must not be null");

      this.type = type;
    }

    public EndpointBuilder<E> left() {
      if (this.lepBuilder == null) {
        this.lepBuilder = new EndpointBuilder<>(this);
      }

      return this.lepBuilder;
    }

    public EndpointBuilder<E> right() {
      if (this.repBuilder == null) {
        this.repBuilder = new EndpointBuilder<>(this);
      }

      return this.repBuilder;
    }

    public Interval<E> build() {
      return new Interval<>(this.type, this.lepBuilder == null ? null : this.lepBuilder.build(),
          this.repBuilder == null ? null : this.repBuilder.build());
    }

  }

  public static final class EndpointBuilder<E extends Comparable<E>> {

    private final IntervalBuilder<E> intervalBuilder;
    private E element;
    private Boolean postive;
    private boolean bounded;

    private EndpointBuilder(IntervalBuilder<E> intervalBuilder) {
      this.intervalBuilder = intervalBuilder;
      this.element = null;
      this.postive = null;
      this.bounded = true;
    }

    public EndpointBuilder<E> finity(E element) {
      Assert.notNull(element, "Finity must have an actually value");

      this.element = element;
      this.postive = null;

      return this;
    }

    public EndpointBuilder<E> infinity(boolean postive) {
      this.postive = postive;
      this.element = null;

      return this;
    }

    public EndpointBuilder<E> postiveInfinity() {
      return this.infinity(true);
    }

    public EndpointBuilder<E> negativeInfinity() {
      return this.infinity(false);
    }

    public EndpointBuilder<E> bounded() {
      this.bounded = true;

      return this;
    }

    public EndpointBuilder<E> unbounded() {
      this.bounded = false;

      return this;
    }

    public IntervalBuilder<E> then() {
      return this.intervalBuilder;
    }

    private Endpoint<E> build() {
      if (this.element != null) {
        return Endpoint.finity(this.element, this.bounded);
      }

      return Endpoint.infinity(this.intervalBuilder.type, this.bounded, this.postive);
    }
  }

  public static final class Intervals<E extends Comparable<E>> {

    private final Set<Interval<E>> intervals;

    @SafeVarargs
    public Intervals(@Nullable Interval<E>... intervals) {
      this(Arrays.asList(intervals));
    }

    public Intervals(@Nullable Collection<Interval<E>> intervals) {
      this.intervals = Collections.unmodifiableSet(flatten(intervals));
    }

    public static <E extends Comparable<E>> Intervals<E> differenceBetween(Intervals<E> intervals,
        Intervals<E> others) {
      Assert.isTrue(intervals != null && others != null, "The given intervals must not be null");

      if (others.size() == 0) {
        return intervals;
      }

      Class<E> clazz = others.intervals.iterator().next().type;
      Interval<E> universe = new Interval<>(clazz, Endpoint.infinity(clazz, false, true),
          Endpoint.infinity(clazz, true, true)); // [-∞, +∞]

      // Note:
      // (A1 U A2) - (B1 U B2) ≠ (A1 - B1) U (A1 - B2) U (A2 - B1) U (A2 - B2)
      // (A1 U A2) - (B1 U B2) = (A1 U A2) ∩ (Universe - (B1 U B2))
      return intersectBetween(intervals, differenceBetween(universe, others));
    }

    public static <E extends Comparable<E>> Intervals<E> differenceBetween(Intervals<E> intervals, Interval<E> other) {
      Assert.isTrue(intervals != null && other != null, "The given interval(s) must not be null");

      Set<Interval<E>> collection = new HashSet<>();
      for (Interval<E> one : intervals.intervals) {
        collection.addAll(one.difference(other).intervals);
      }

      return new Intervals<>(collection);
    }

    public static <E extends Comparable<E>> Intervals<E> differenceBetween(Interval<E> interval, Intervals<E> others) {
      // De Morgan's laws: A - (B U C) = （A - B) ∩ (A - C)
      Assert.isTrue(interval != null && others != null, "The given interval(s) must not be null");

      Intervals<E> collection = null;
      for (Interval<E> another : others.intervals) {
        if (collection == null) {
          collection = interval.difference(another);
          continue;
        }
        collection = collection.intersect(interval.difference(another));
      }

      return collection;
    }

    public static <E extends Comparable<E>> Intervals<E> symmetricDifferenceBetween(Interval<E> one,
        Interval<E> another) {
      Assert.isTrue(one != null && another != null, "The given interval must not be null");

      // A Δ B = (A − B) U (B − A) or A Δ B = (A U B) − (A ∩ B)
      return unionBetween(one.difference(another), another.difference(one));
    }

    public static <E extends Comparable<E>> Intervals<E> intersectBetween(Intervals<E> intervals, Intervals<E> others) {
      Assert.isTrue(intervals != null && others != null, "The given intervals must not be null");

      Set<Interval<E>> collection = new HashSet<>();
      Interval<E> intersected = null;
      for (Interval<E> one : intervals.intervals) {
        for (Interval<E> another : others.intervals) {
          intersected = one.intersect(another);
          collection.add(intersected);
        }
      }

      return new Intervals<>(collection);
    }

    public static <E extends Comparable<E>> Intervals<E> intersectBetween(Intervals<E> intervals, Interval<E> other) {
      Assert.isTrue(intervals != null && other != null, "The given interval(s) must not be null");

      Set<Interval<E>> collection = new HashSet<>();
      for (Interval<E> one : intervals.intervals) {
        collection.add(one.intersect(other));
      }

      return new Intervals<>(collection);
    }

    public static <E extends Comparable<E>> Intervals<E> unionBetween(Intervals<E> intervals, Intervals<E> others) {
      Assert.isTrue(intervals != null && others != null, "The given intervals must not be null");

      Set<Interval<E>> collection = new HashSet<>();
      collection.addAll(intervals.intervals);
      collection.addAll(others.intervals);

      return new Intervals<>(collection);
    }

    public static <E extends Comparable<E>> Intervals<E> unionBetween(Intervals<E> intervals, Interval<E> other) {
      Assert.isTrue(intervals != null && other != null, "The given interval(s) must not be null");

      Set<Interval<E>> collection = new HashSet<>();
      collection.addAll(intervals.intervals);
      collection.add(other);

      return new Intervals<>(collection);
    }

    public static <E extends Comparable<E>> Set<Interval<E>> flatten(@Nullable Collection<Interval<E>> intervals) {
      if (intervals == null || intervals.isEmpty()) {
        return Collections.emptySet();
      }

      return intervals.stream().distinct().filter(interval -> {
        return interval != null;
      }).sorted((one, another) -> {
        if (one.isEmpty() && another.isEmpty()) {
          return -1;
        }
        if (one.isEmpty()) {
          return -1;
        }
        if (another.isEmpty()) {
          return 1;
        }
        int comparasion = one.left.compareTo(another.left);
        if (comparasion != 0) {
          return comparasion;
        }
        return one.right.compareTo(another.right);
      }).reduce(new HashSet<Interval<E>>(), (collection, interval) -> {
        Interval<E> temp = interval;
        for (Interval<E> item : collection) {
          if (item.hasIntersectionWith(interval)) {
            temp = item.union(interval).intervals.iterator().next();
            collection.remove(item);
            break;
          }
        }
        collection.add(temp);
        return collection;
      }, (collection1, collection2) -> {
        collection1.addAll(collection2);
        return collection1;
      });
    }

    public int size() {
      return this.intervals.size();
    }

    public boolean isEmpty() {
      if (this.intervals.isEmpty()) {
        return true;
      }

      for (Interval<E> interval : this.intervals) {
        if (!interval.isEmpty()) {
          return false;
        }
      }

      return true;
    }

    public Set<Interval<E>> toSet() {
      return this.intervals;
    }

    public Intervals<E> difference(Intervals<E> others) {
      return differenceBetween(this, others);
    }

    public Intervals<E> intersect(Intervals<E> others) {
      return intersectBetween(this, others);
    }

    public Intervals<E> union(Intervals<E> others) {
      return unionBetween(this, others);
    }
  }
}
