package zenny.toybox.springfield.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.lang.Nullable;

public class Tuple implements Iterable<Object>, Serializable {

  /**
   * Serialization version
   */
  private static final long serialVersionUID = 8663387671697527863L;

  /**
   * Unique "empty" tuple
   */
  public static final NullTuple EMPTY = new NullTuple();

  private final List<Object> elements;

  private transient int hashCode;

  protected Tuple(@Nullable Object[] elements) {

    // Use an array instead of a "..." argument inside to avoid the issue which is
    // shown below:
    // new Tuple({"a", "b", "c"})
    // => ([ "a", "b", "c" ]) // expected
    // => ("a", "b", "c") // actual
    this.elements = elements == null || elements.length < 1 ? Collections.emptyList() : Arrays.asList(elements);
    this.hashCode = this.calculateHashCode();
  }

  public static Tuple of(@Nullable Object... elements) {
    return new Tuple(elements);
  }

  public static <A> Unit<A> asUnit(@Nullable A element0) {
    return new Unit<>(element0);
  }

  public static <A, B> Pair<A, B> asPair(@Nullable A element0, @Nullable B element1) {
    return new Pair<>(element0, element1);
  }

  public static <A, B, C> Triplet<A, B, C> asTriplet(@Nullable A element0, @Nullable B element1, @Nullable C element2) {
    return new Triplet<>(element0, element1, element2);
  }

  public static <A, B, C, D> Quartet<A, B, C, D> asQuartet(@Nullable A element0, @Nullable B element1,
      @Nullable C element2, @Nullable D element3) {
    return new Quartet<>(element0, element1, element2, element3);
  }

  public static <A, B, C, D, E> Quintet<A, B, C, D, E> asQuartet(@Nullable A element0, @Nullable B element1,
      @Nullable C element2, @Nullable D element3, @Nullable E element4) {
    return new Quintet<>(element0, element1, element2, element3, element4);
  }

  public static <A, B, C, D, E, F> Sextet<A, B, C, D, E, F> asSextet(@Nullable A element0, @Nullable B element1,
      @Nullable C element2, @Nullable D element3, @Nullable E element4, @Nullable F element5) {
    return new Sextet<>(element0, element1, element2, element3, element4, element5);
  }

  public static <A, B, C, D, E, F, G> Septet<A, B, C, D, E, F, G> asSeptet(@Nullable A element0, @Nullable B element1,
      @Nullable C element2, @Nullable D element3, @Nullable E element4, @Nullable F element5, @Nullable G element6) {
    return new Septet<>(element0, element1, element2, element3, element4, element5, element6);
  }

  public static <A, B, C, D, E, F, G, H> Octet<A, B, C, D, E, F, G, H> asOctet(@Nullable A element0,
      @Nullable B element1, @Nullable C element2, @Nullable D element3, @Nullable E element4, @Nullable F element5,
      @Nullable G element6, @Nullable H element7) {
    return new Octet<>(element0, element1, element2, element3, element4, element5, element6, element7);
  }

  public static <A, B, C, D, E, F, G, H, I> Ennead<A, B, C, D, E, F, G, H, I> asEnnead(@Nullable A element0,
      @Nullable B element1, @Nullable C element2, @Nullable D element3, @Nullable E element4, @Nullable F element5,
      @Nullable G element6, @Nullable H element7, @Nullable I element8) {
    return new Ennead<>(element0, element1, element2, element3, element4, element5, element6, element7, element8);
  }

  public static <A, B, C, D, E, F, G, H, I, J> Decade<A, B, C, D, E, F, G, H, I, J> asDecade(@Nullable A element0,
      @Nullable B element1, @Nullable C element2, @Nullable D element3, @Nullable E element4, @Nullable F element5,
      @Nullable G element6, @Nullable H element7, @Nullable I element8, @Nullable J element9) {
    return new Decade<>(element0, element1, element2, element3, element4, element5, element6, element7, element8,
        element9);
  }

  protected final int calculateHashCode() {
    int total = 0;
    for (Object element : this.elements) {
      if (element != null) {
        total ^= element.hashCode();
      }
    }

    return total;
  }

  protected final Object readResolve() {
    this.hashCode = this.calculateHashCode();
    return this;
  }

  public final int size() {
    return this.elements.size();
  }

  public final boolean contains(@Nullable Object element) {
    for (Object el : this.elements) {
      if (ObjectUtils.nullSafeEquals(el, element)) {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public final <T> T get(int index, Class<T> requiredType) {
    Assert.notNull(requiredType, "Type must not be null");

    Object element = this.get(index);
    if (element == null) {
      return null;
    }

    if (!requiredType.isInstance(element)) {
      throw new IllegalArgumentException(
          new StringBuilder("Unable to convert element value of type '").append(element.getClass().getTypeName())
              .append("' to required type '").append(requiredType.getTypeName()).append("'").toString());
    }

    return (T) element;
  }

  @Nullable
  public final Object get(int index) {
    Assert.isTrue(index > 0 && index < this.size(),
        new StringBuilder("Unable to retrieve index ").append(index).append(" in ")
            .append(this.getClass().getTypeName()).append(". Indexes for this class start with 0 and end with ")
            .append(this.size() - 1).toString());

    return this.elements.get(index);
  }

  public final int indexOf(@Nullable Object element) {
    for (int index = 0; index < this.size(); index++) {
      if (ObjectUtils.nullSafeEquals(this.get(index), element)) {
        return index;
      }
    }

    return -1;
  }

  public final int lastIndexOf(@Nullable Object element) {
    for (int index = this.size() - 1; index >= 0; index--) {
      if (ObjectUtils.nullSafeEquals(this.get(index), element)) {
        return index;
      }
    }

    return -1;
  }

  public final Tuple flatten() {
    if (this instanceof NullTuple) {
      return this;
    }

    List<Object> elements = new ArrayList<>();
    for (Object element : this.elements) {
      if (element instanceof Tuple) {
        if (element instanceof NullTuple) {
          continue;
        }

        elements.addAll(((Tuple) element).elements);
      } else {
        elements.add(element);
      }
    }

    return new Tuple(elements.toArray());
  }

  public final Object[] toArray() {
    return this.elements.toArray();
  }

  @Override
  public final Iterator<Object> iterator() {
    return this.elements.iterator();
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof Tuple)) {
      return false;
    }

    Tuple thisTuple = this.flatten();
    Tuple otherTuple = ((Tuple) other).flatten();
    if (thisTuple.size() != otherTuple.size()) {
      return false;
    }

    for (int index = 0; index < thisTuple.size(); index++) {
      if (!ObjectUtils.nullSafeEquals(thisTuple.get(index), otherTuple.get(index))) {
        return false;
      }
    }

    return true;
  }

  @Override
  public final String toString() {
    if (this.size() == 0) {
      return "Ø";
    }

    StringBuilder builder = new StringBuilder("(");
    for (int index = 0; index < this.size(); index++) {
      builder.append(this.elements.get(index));

      if (index < this.size() - 1) {
        builder.append(", ");
      }
    }
    builder.append(")");

    return builder.toString();
  }

  private interface UnitGetter<A> {

    @Nullable
    A get0();
  }

  private interface PairGetter<A, B> extends UnitGetter<A> {

    @Nullable
    B get1();
  }

  private interface TripletGetter<A, B, C> extends PairGetter<A, B> {

    @Nullable
    C get2();
  }

  private interface QuartetGetter<A, B, C, D> extends TripletGetter<A, B, C> {

    @Nullable
    D get3();
  }

  private interface QuintetGetter<A, B, C, D, E> extends QuartetGetter<A, B, C, D> {

    @Nullable
    E get4();
  }

  private interface SextetGetter<A, B, C, D, E, F> extends QuintetGetter<A, B, C, D, E> {

    @Nullable
    F get5();
  }

  private interface SeptetGetter<A, B, C, D, E, F, G> extends SextetGetter<A, B, C, D, E, F> {

    @Nullable
    G get6();
  }

  private interface OctetGetter<A, B, C, D, E, F, G, H> extends SeptetGetter<A, B, C, D, E, F, G> {

    @Nullable
    H get7();
  }

  private interface EnneadGetter<A, B, C, D, E, F, G, H, I> extends OctetGetter<A, B, C, D, E, F, G, H> {

    @Nullable
    I get8();
  }

  private interface DecadeGetter<A, B, C, D, E, F, G, H, I, J> extends EnneadGetter<A, B, C, D, E, F, G, H, I> {

    @Nullable
    J get9();
  }

  public static class NullTuple extends Tuple {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = -2772914227685036942L;

    protected NullTuple() {
      super(null);
    }
  }

  public static class Unit<A> extends Tuple implements UnitGetter<A> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = -249824566397478071L;

    @Nullable
    private final A element0;

    protected Unit(@Nullable A element0) {
      super(new Object[] { element0 });
      this.element0 = element0;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }
  }

  public static class Pair<A, B> extends Tuple implements PairGetter<A, B> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 6551261310327404242L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    protected Pair(@Nullable A element0, @Nullable B element1) {
      super(new Object[] { element0, element1 });
      this.element0 = element0;
      this.element1 = element1;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }
  }

  public static class Triplet<A, B, C> extends Tuple implements TripletGetter<A, B, C> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = -1418825261198087698L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    protected Triplet(@Nullable A element0, @Nullable B element1, @Nullable C element2) {
      super(new Object[] { element0, element1, element2 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }
  }

  public static class Quartet<A, B, C, D> extends Tuple implements QuartetGetter<A, B, C, D> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 2655487093396860786L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    protected Quartet(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3) {
      super(new Object[] { element0, element1, element2, element3 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }
  }

  public static class Quintet<A, B, C, D, E> extends Tuple implements QuintetGetter<A, B, C, D, E> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 803818565591283543L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    protected Quintet(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4) {
      super(new Object[] { element0, element1, element2, element3, element4 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }
  }

  public static class Sextet<A, B, C, D, E, F> extends Tuple implements SextetGetter<A, B, C, D, E, F> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = -6432602045072045468L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    @Nullable
    private final F element5;

    protected Sextet(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4, @Nullable F element5) {
      super(new Object[] { element0, element1, element2, element3, element4, element5 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
      this.element5 = element5;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }

    @Override
    @Nullable
    public F get5() {
      return this.element5;
    }
  }

  public static class Septet<A, B, C, D, E, F, G> extends Tuple implements SeptetGetter<A, B, C, D, E, F, G> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 4794838179937147807L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    @Nullable
    private final F element5;

    @Nullable
    private final G element6;

    protected Septet(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4, @Nullable F element5, @Nullable G element6) {
      super(new Object[] { element0, element1, element2, element3, element4, element5, element6 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
      this.element5 = element5;
      this.element6 = element6;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }

    @Override
    @Nullable
    public F get5() {
      return this.element5;
    }

    @Override
    @Nullable
    public G get6() {
      return this.element6;
    }
  }

  public static class Octet<A, B, C, D, E, F, G, H> extends Tuple implements OctetGetter<A, B, C, D, E, F, G, H> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 5136635401408367190L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    @Nullable
    private final F element5;

    @Nullable
    private final G element6;

    @Nullable
    private final H element7;

    protected Octet(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4, @Nullable F element5, @Nullable G element6, @Nullable H element7) {
      super(new Object[] { element0, element1, element2, element3, element4, element5, element6, element7 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
      this.element5 = element5;
      this.element6 = element6;
      this.element7 = element7;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }

    @Override
    @Nullable
    public F get5() {
      return this.element5;
    }

    @Override
    @Nullable
    public G get6() {
      return this.element6;
    }

    @Override
    @Nullable
    public H get7() {
      return this.element7;
    }
  }

  public static class Ennead<A, B, C, D, E, F, G, H, I> extends Tuple
      implements EnneadGetter<A, B, C, D, E, F, G, H, I> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = -3551223655511796372L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    @Nullable
    private final F element5;

    @Nullable
    private final G element6;

    @Nullable
    private final H element7;

    @Nullable
    private final I element8;

    protected Ennead(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4, @Nullable F element5, @Nullable G element6, @Nullable H element7, @Nullable I element8) {
      super(new Object[] { element0, element1, element2, element3, element4, element5, element6, element7, element8 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
      this.element5 = element5;
      this.element6 = element6;
      this.element7 = element7;
      this.element8 = element8;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }

    @Override
    @Nullable
    public F get5() {
      return this.element5;
    }

    @Override
    @Nullable
    public G get6() {
      return this.element6;
    }

    @Override
    @Nullable
    public H get7() {
      return this.element7;
    }

    @Override
    @Nullable
    public I get8() {
      return this.element8;
    }
  }

  public static class Decade<A, B, C, D, E, F, G, H, I, J> extends Tuple
      implements DecadeGetter<A, B, C, D, E, F, G, H, I, J> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 2979039931994928142L;

    @Nullable
    private final A element0;

    @Nullable
    private final B element1;

    @Nullable
    private final C element2;

    @Nullable
    private final D element3;

    @Nullable
    private final E element4;

    @Nullable
    private final F element5;

    @Nullable
    private final G element6;

    @Nullable
    private final H element7;

    @Nullable
    private final I element8;

    @Nullable
    private final J element9;

    protected Decade(@Nullable A element0, @Nullable B element1, @Nullable C element2, @Nullable D element3,
        @Nullable E element4, @Nullable F element5, @Nullable G element6, @Nullable H element7, @Nullable I element8,
        @Nullable J element9) {
      super(new Object[] { element0, element1, element2, element3, element4, element5, element6, element7, element8,
          element9 });
      this.element0 = element0;
      this.element1 = element1;
      this.element2 = element2;
      this.element3 = element3;
      this.element4 = element4;
      this.element5 = element5;
      this.element6 = element6;
      this.element7 = element7;
      this.element8 = element8;
      this.element9 = element9;
    }

    @Override
    @Nullable
    public A get0() {
      return this.element0;
    }

    @Override
    @Nullable
    public B get1() {
      return this.element1;
    }

    @Override
    @Nullable
    public C get2() {
      return this.element2;
    }

    @Override
    @Nullable
    public D get3() {
      return this.element3;
    }

    @Override
    @Nullable
    public E get4() {
      return this.element4;
    }

    @Override
    @Nullable
    public F get5() {
      return this.element5;
    }

    @Override
    @Nullable
    public G get6() {
      return this.element6;
    }

    @Override
    @Nullable
    public H get7() {
      return this.element7;
    }

    @Override
    @Nullable
    public I get8() {
      return this.element8;
    }

    @Override
    @Nullable
    public J get9() {
      return this.element9;
    }
  }
}
