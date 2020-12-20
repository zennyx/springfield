package zenny.toybox.springfield.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import zenny.toybox.springfield.util.HierarchicalBuilder;

public abstract class Locks {

  /**
   * Suppresses default constructor, ensuring non-instantiability.
   */
  private Locks() {
    throw new Error("No instances");
  }

  public static Lock systemFair() {
    return system(true);
  }

  public static Lock systemUnfair() {
    return system(false);
  }

  public static Lock system(boolean fair) {
    return new ReentrantLock(fair);
  }

  public static ReadWriteLock systemFairRw() {
    return systemRw(true);
  }

  public static ReadWriteLock systemUnfairRw() {
    return systemRw(false);
  }

  public static ReadWriteLock systemRw(boolean fair) {
    return new ReentrantReadWriteLock(fair);
  }

  @FunctionalInterface
  public interface LockBuilder extends HierarchicalBuilder<Lock, LockBuilder> {
  }

  /**
   * A factory class for building {@code LockBuilder}s.
   * Not a good idea, just make do with it before a better solution is found. :(
   * <p>
   * Example usage:
   * <p>
   *
   * <pre>
   * public class RedisLockBuilder implements LockBuilder {
   *   private final RedissonClient client;
   *   private boolean isFair;
   *   private String name;
   *
   *   private RedisLockBuilder(RedissonClient client) {
   *     this.client = client;
   *   }
   *
   *   &#64;Override
   *   public RedisLockBuilder self() {
   *     return this;
   *   }
   *
   *   public RedisLockBuilder fair(boolean fair) {
   *     this.fair = fair;
   *     return this.self();
   *   }
   *   
   *   public RedisLockBuilder name(String name) {
   *     this.name = name;
   *     return this.self();
   *   }
   *   
   *   &#64;Override
   *   public Lock build() {
   *     if (this.fair) {
   *       return this.client.getFairLock(this.name);
   *     }
   *     return this.client.getLock(this.name);
   *   }
   * }
   *
   * public class RedisLockBuilderFactory implements LockBuilderFactory&ltRedisLockBuilder&gt {
   *   private RedissonClient client;
   *
   *   public RedisLockBuilderFactory(RedissonClient client) {
   *     this.client = client;
   *   }
   *
   *   &#64;Override
   *   public RedisLockBuilder get() {
   *     return new RedisLockBuilder(this.client);
   *   }
   *
   *   public void setClient(RedissonClient client) {
   *     this.client = client;
   *   }
   * }
   *
   * &#64;Service
   * public class SomeServiceImpl implements SomeService {
   *   private final RedisLockBuilderFactory factory;
   *
   *   &#64;Autowired
   *   public SomeServiceImpl(RedisLockBuilderFactory factory) {
   *     this.factory = factory;
   *   }
   *   
   *   &#64;Override
   *   public void doSomething() {
   *     Lock lock = this.factory.get().name("SOME:SERVICE:1").build();
   *     lock.lock();
   *     // ...
   *     lock.unlock();
   *   }
   * }
   * </pre>
   *
   * @param <B> the type of the returned {@code LockBuilder}
   */
  @FunctionalInterface
  public interface LockBuilderFactory<B extends LockBuilder> {
    B get();
  }
}
