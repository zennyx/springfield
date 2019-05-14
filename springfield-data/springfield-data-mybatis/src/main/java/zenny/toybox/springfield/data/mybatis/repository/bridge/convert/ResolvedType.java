package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import java.lang.reflect.Type;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;

public final class ResolvedType<T> {

  @Nullable
  private final Class<T> resolved;

  private ResolvedType(Class<T> resolved) {
    this.resolved = resolved;
  }

  public static <T> ResolvedType<T> forClass(Class<T> resolved) {
    Assert.notNull(resolved, "Class must not be null");

    return new ResolvedType<>(resolved);
  }

  public static ResolvedType<?> forType(Type toResolve) {
    Assert.notNull(toResolve, "Type must not be null");

    ResolvableType resolvableType = ResolvableType.forType(toResolve);
    return new ResolvedType<>(resolvableType.resolve());
  }

  @Nullable
  public Class<T> getResolved() {
    return this.resolved;
  }
}
