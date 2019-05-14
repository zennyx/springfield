package zenny.toybox.springfield.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collector;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.lang.Internal;

/**
 * Static utility methods pertaining to instances of {@link Throwable}.
 * <p>
 * Mainly for internal use within the framework; consider
 * <a href="https://github.com/google/guava/">Google Guava</a> for a more
 * comprehensive suite of {@code Throwable} utilities.
 *
 * @author Zenny Xu
 */
@Internal
public final class ThrowableUtils {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private ThrowableUtils() {
    throw new Error("No instances");
  }

  /**
   * Throws {@code throwable} if it is an instance of {@code declaredType}.
   * <p>
   * Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (BarException | RuntimeException | Error t) {
   *     failure = t;
   *   }
   * }
   * throwIfInstanceOf(failure, BarException.class);
   * throwIfUnchecked(failure);
   * </pre>
   *
   * @param throwable the error to throw
   * @param declaredType the expected error type
   * @throws X if {@code throwable} is an instance of {@code declaredType}
   */
  public static <X extends Throwable> void throwIfInstanceOf(@Nullable Throwable throwable, Class<X> declaredType)
      throws X {
    Assert.notNull(declaredType, "DeclaredType must not be null");

    if (throwable == null) {
      return;
    }

    if (declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
  }

  /**
   * Throws {@code throwable} if it is a {@link RuntimeException} or
   * {@link Error}.
   * <p>
   * Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (RuntimeException | Error t) {
   *     failure = t;
   *   }
   * }
   * throwIfUnchecked(failure);
   * </pre>
   *
   * @param throwable the error to throw
   */
  public static void throwIfUnchecked(@Nullable Throwable throwable) {
    if (throwable == null) {
      return;
    }

    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
  }

  /**
   * Throws {@code throwable} as an instance of {@link RuntimeException}.
   * <p>
   * Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (BarException | Error t) {
   *     failure = t;
   *   }
   * }
   * throwAsUnchecked(failure);
   * </pre>
   *
   * @param throwable the error to throw
   */
  public static <X extends RuntimeException> void throwAsUnchecked(@Nullable Throwable throwable) {
    throwAsUnchecked(throwable, RuntimeException.class);
  }

  /**
   * Throws {@code throwable} as an instance of {@code declaredType}, which is a
   * sub-class of {@link RuntimeException}.
   * <p>
   * Example usage:
   *
   * <pre>
   * for (Foo foo : foos) {
   *   try {
   *     foo.bar();
   *   } catch (BarException | Error t) {
   *     failure = t;
   *   }
   * }
   * throwAsUnchecked(failure, RuntimeException.class);
   * </pre>
   *
   * @param throwable the error to throw
   * @param declaredType the expected error type
   */
  public static <X extends RuntimeException> void throwAsUnchecked(@Nullable Throwable throwable,
      Class<X> declaredType) {
    Assert.notNull(declaredType, "DeclaredType must not be null");

    if (throwable == null) {
      return;
    }

    if (declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
    if (declaredType.equals(RuntimeException.class)) {
      throw new RuntimeException(throwable);
    }

    X ex;
    try {
      ex = declaredType.getConstructor(Throwable.class).newInstance(throwable);
    } catch (Exception e) {
      throw new RuntimeException(throwable);
    }
    throw ex;
  }

  /**
   * Returns the innermost cause of {@code throwable}. The first throwable in a
   * chain provides context from when the error or exception was initially
   * detected.
   * <p>
   * Example usage:
   *
   * <pre>
   * assertEquals("Unable to assign a customer id", ThrowableUtils.getRootCause(e).getMessage());
   * </pre>
   *
   * @param throwable the error to throw
   */
  @Nullable
  public static Throwable getRootCause(@Nullable Throwable throwable) {
    Throwable[] rootCause = new Throwable[1];
    mapCauses(throwable, ex -> rootCause[0] = ex);
    return rootCause[0];
  }

  /**
   * Gets a {@code Throwable} causal chain as a collection. The first entry in the
   * collection will be {@code throwable} followed by its cause hierarchy.
   * <p>
   * Example usage:
   *
   * <pre>
   * ThrowableUtils.flattenCauses(e, Collectors.&lt;Throwable&gt;toList());
   * </pre>
   *
   * @param throwable the error to throw
   * @param collector the operation to flatten the causal chain of
   * {@code throwable}
   * @return flattened causal chain
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <A, R extends Collection<Throwable>> R flattenCauses(@Nullable Throwable throwable,
      Collector<Throwable, A, R> collector) {
    Assert.notNull(collector, "Callback must not be null");

    if (throwable == null) {
      return null;
    }

    A container = collector.supplier().get();
    mapCauses(throwable, e -> collector.accumulator().accept(container, e));
    return collector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH) ? (R) container
        : collector.finisher().apply(container);
  }

  /**
   * Maps a {@code Throwable} causal chain and applies the given {@code callback}
   * for each cause.
   * <p>
   * Example usage:
   *
   * <pre>
   * ThrowableUtils.mapCauses(e, t -> {
   *   System.out.println(t);
   * });
   * </pre>
   *
   * @param throwable the error to throw
   * @param callback the operation to do with the cause
   */
  public static void mapCauses(@Nullable Throwable throwable, Consumer<Throwable> callback) {
    Assert.notNull(callback, "Callback must not be null");

    if (throwable == null) {
      return;
    }

    // Keep a second pointer that slowly walks the causal chain. If the fast pointer
    // ever catches
    // the slower pointer, then there's a loop.
    Throwable slowPointer = throwable;
    boolean advanceSlowPointer = false;

    Throwable cause;
    while ((cause = throwable.getCause()) != null) {
      throwable = cause;
      callback.accept(throwable);

      if (throwable == slowPointer) {
        break; // break if loop in causal chain detected
      }
      if (advanceSlowPointer) {
        slowPointer = slowPointer.getCause();
      }
      advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
    }
  }

  /**
   * Returns a string containing the result of {@link Throwable#toString()
   * toString()}, followed by the full, recursive stack trace of
   * {@code throwable}.
   *
   * @param throwable the error to throw
   * @return a string containing the full, recursive stack trace of
   * {@code throwable}
   */
  public static String getStackTraceAsString(@Nullable Throwable throwable) {
    if (throwable == null) {
      return null;
    }

    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}
