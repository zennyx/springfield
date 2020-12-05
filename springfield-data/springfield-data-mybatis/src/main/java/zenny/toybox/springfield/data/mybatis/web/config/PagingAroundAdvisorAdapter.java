package zenny.toybox.springfield.data.mybatis.web.config;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Pageable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.function.Promise;

/**
 * Adapter to simplify the paging-operations in MyBatis.
 * <p>
 * Example usage:
 *
 * <pre>
 * &#64;EnableAspectJAutoProxy
 * &#64;EnableSpringDataWebSupport
 * &#64;Configuration
 * public class MyConfiguration {
 *   &#64;Bean
 *   public PagingAroundAdvisorAdapter pagingAdvisor() {
 *     return new PagingAroundAdvisorAdapter(p -> {
 *       // Store the page-info in a threadlocal in order to be used in DAL
 *       PageHelper.startPage(p.getPageSize(), p.getPageNumber());
 *     }, () -> {
 *       // Clear the threadlocal
 *       PageHelper.clear();
 *     });
 *   }
 * }
 *
 * &#64;RestController
 * public class MyController {
 *   &#64;GetMapping("hello")
 *   public String hello(&#64;RequestParam("param") String param, &#64;Paging Pageable pageable) {
 *     // ...
 *   }
 * }
 * </pre>
 *
 * @author Zenny Xu
 * @see zenny.toybox.springfield.data.mybatis.web.config.Paging
 */
@Aspect
@SuppressWarnings("serial")
public class PagingAroundAdvisorAdapter implements Serializable {

  private static final BiFunction<Object, Pageable, Object> DEFAULT_AFTER_RETURNING_ADVICE = (o, p) -> {
    return o;
  };

  private final Consumer<Pageable> before;

  /**
   * Page-info can be merged into the proceeded result at this step.
   * <p>
   * Some clients (such as DataTable) may require the page-info which them sent before
   * to synchronize requests.
   * Do nothing except for returning the proceeded result by default.
   */
  private final BiFunction<Object, Pageable, Object> afterReturning;

  private final Promise after;

  public PagingAroundAdvisorAdapter(Consumer<Pageable> before, Promise after) {
    this(before, DEFAULT_AFTER_RETURNING_ADVICE, after);
  }

  public PagingAroundAdvisorAdapter(Consumer<Pageable> before, BiFunction<Object, Pageable, Object> afterReturning,
      Promise after) {
    Assert.notNull(before, "Before-advice must not be null");
    Assert.notNull(afterReturning, "AfterReturning-advice must not be null");
    Assert.notNull(after, "After-advice must not be null");

    this.before = before;
    this.afterReturning = afterReturning;
    this.after = after;
  }

  @Around("execution(public * *(.., @Paging (org.springframework.data.domain.Pageable+), ..))"
      + "&& (@within(org.springframework.stereotype.Controller) "
          + "|| @within(org.springframework.web.bind.annotation.RestController))"
      + "&& (@annotation(org.springframework.web.bind.annotation.RequestMapping) "
          + "|| @annotation(org.springframework.web.bind.annotation.GetMapping) "
          + "|| @annotation(org.springframework.web.bind.annotation.PostMapping) "
          + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
          + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) "
          + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
  protected Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    try {
      Pageable pageable = this.resolveArguments(joinPoint.getArgs());
      this.before.accept(pageable);

      return this.afterReturning.apply(joinPoint.proceed(), pageable);
    } finally {
      this.after.fulfill();
    }
  }

  protected Pageable resolveArguments(Object[] args) {
    for (Object arg : args) {
      if (arg instanceof Pageable) {
        return (Pageable) arg;
      }
    }

    throw new IllegalStateException("Unable to resolve pageable argument at this moment");
  }

  public Consumer<Pageable> getBefore() {
    return this.before;
  }

  public BiFunction<Object, Pageable, Object> getAfterReturning() {
    return this.afterReturning;
  }

  public Promise getAfter() {
    return this.after;
  }
}
