package zenny.toybox.springfield.util;

import java.util.stream.Stream;

import org.springframework.lang.Nullable;

/**
 * A helper interface to optimize tail recursion in Java. Since Java has not yet
 * optimized for tail recursion, it is usually converted to a loop to avoid the
 * risk of stack overflow when recursive calls are invoked, but this also
 * undermine the developers writing habits. This interface is designed to
 * optimize the performance of tail recursive calls without changing those
 * habits.
 * <p>
 * Example usage:
 *
 * <pre>
 * public static TailRecursion&lt;Integer&gt; accumulate(int number, int total) {
 *   if (number == 1) {
 *     return () -&gt; endsWith(total);
 *   }
 *   return () -&gt; accumulate(number - 1, total + number);
 * }
 *
 * accumulate(5, 1).collect(); // =&gt; 15
 * </pre>
 *
 * The above exmaple is equivalent to the standard recursion shown below (,
 * except for performance):
 *
 * <pre>
 * public static int stdAccumulate(int number, int total) {
 *   if (number == 1) {
 *     return total;
 *   }
 *   return stdAccumulate(number - 1, total + number);
 * }
 *
 * stdAccumulate(5, 1); // =&gt; 15
 * </pre>
 *
 * @author Zenny Xu
 * @param <O> type of the output
 */
public interface TailRecursion<O> {

  /**
   * @return
   */
  TailRecursion<O> invoke();

  /**
   * @return
   */
  default boolean done() {
    return false;
  }

  /**
   * @return
   */
  @Nullable
  default O get() {
    throw new IllegalStateException("Unable to get value because the tail recursive call has not completed yet");
  }

  /**
   * @return
   */
  @Nullable
  default O collect() {
    return Stream.iterate(this, TailRecursion::invoke).filter(TailRecursion::done).findFirst().get().get();
  }

  /**
   * @param output
   * @return
   */
  static <O> TailRecursion<O> endsWith(@Nullable O output) {
    return new LastStackFrame<>(output);
  }

  /**
   * @author Zenny Xu
   * @param <O> type of the output
   */
  static final class LastStackFrame<O> implements TailRecursion<O> {

    /**
     *
     */
    @Nullable
    private final O output;

    /**
     * @param output
     */
    private LastStackFrame(@Nullable O output) {
      this.output = output;
    }

    /**
     *
     */
    @Override
    public TailRecursion<O> invoke() {
      throw new IllegalStateException("The tail recursion has been executed and cannot continue");
    }

    /**
     *
     */
    @Override
    public boolean done() {
      return true;
    }

    /**
     *
     */
    @Override
    @Nullable
    public O get() {
      return output;
    }
  }
}
