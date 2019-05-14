package zenny.toybox.springfield.util;

import java.io.Serializable;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.lang.Lab;

@SuppressWarnings("serial")
@Lab
public class ResolvableTypeToken<T> implements Serializable, ResolvableTypeProvider { // TODO add api

  private final Class<? extends T> type;

  @Nullable
  private final ResolvableType resolvableType;

  private ResolvableTypeToken(T instance) {
    this(resolveClass(instance));
  }

  private ResolvableTypeToken(Class<? extends T> type) {
    Assert.notNull(type, "Type must not be null");

    this.type = type;
    this.resolvableType = resolveType(type);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<? extends T> resolveClass(T instance) {
    Assert.notNull(instance, "Instance must not be null");

    return (Class<? extends T>) instance.getClass();
  }

  private static <T> ResolvableType resolveType(Class<? extends T> type) {
    ResolvableType resolvableType = ResolvableType.forClass(type);

    if (type.isAnonymousClass() || type.isLocalClass()) {
      return resolvableType.as(type.getSuperclass());
    }
    return resolvableType;
  }

  public static <T> ResolvableTypeToken<T> forInstance(T instance) {
    return new ResolvableTypeToken<>(instance);
  }

  public static <T> ResolvableTypeToken<T> forClass(Class<? extends T> type) {
    return new ResolvableTypeToken<>(type);
  }

  @Override
  public ResolvableType getResolvableType() {
    return this.resolvableType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.type.hashCode();
    result = prime * result + this.resolvableType.hashCode();

    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ResolvableTypeToken)) {
      return false;
    }

    ResolvableTypeToken<?> otherType = (ResolvableTypeToken<?>) other;
    return this.type.equals(otherType.type) && this.resolvableType.equals(otherType.resolvableType);
  }

  @Override
  public String toString() {
    return this.resolvableType.toString();
  }
}
