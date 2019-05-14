package zenny.toybox.springfield.data.mybatis.repository.bridge.convert.support;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.DecoratingProxy;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;

import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.ResolvedType;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConversionFailedException;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConversionService;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConverter;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConverterNotFoundException;
import zenny.toybox.springfield.util.Assert;

public class DefaultTypeConversionService implements TypeConversionService {

  private final List<TypeConverterProxy> converters = new ArrayList<>();
  private final Map<ConverterCacheKey, TypeConverterProxy> converterCache = new ConcurrentReferenceHashMap<>(8);

  @Override
  public boolean canConvert(@Nullable Type source) {
    if (source == null) {
      return false;
    }

    TypeConverterProxy converter = this.getConverter(source);
    return converter != null;
  }

  @Override
  public Type convert(Type source) {
    Assert.notNull(source, "Source type must not be null");

    TypeConverterProxy converter = this.getConverter(source);
    if (converter == null) {
      throw new TypeConverterNotFoundException(source);
    }

    Type result = null;
    try {
      result = converter.convert(source);
    } catch (RuntimeException cause) {
      throw new TypeConversionFailedException(source, cause);
    }

    if (result == null) {
      throw new TypeConversionFailedException(source);
    }

    return result;
  }

  private TypeConverterProxy getConverter(Type source) {
    ConverterCacheKey key = new ConverterCacheKey(source);
    TypeConverterProxy converter = this.converterCache.get(key);
    if (converter != null) {
      return converter;
    }

    converter = this.findConverter(source);
    if (converter != null) {
      this.converterCache.put(key, converter);
    }

    return converter;
  }

  private TypeConverterProxy findConverter(Type source) {
    ResolvableType sourceType = ResolvableType.forType(source);

    for (TypeConverterProxy converter : this.converters) {
      if (converter.matches(sourceType)) {
        return converter;
      }
    }

    return null;
  }

  @Override
  public void addConverter(@Nullable TypeConverter<?, ?> converter) {
    if (converter == null) {
      return;
    }

    TypeConverterProxy proxy = new TypeConverterProxy(converter);
    if (this.converters.contains(proxy)) {
      return;
    }

    this.converters.add(proxy);
    this.invalidateCache();
  }

  private void invalidateCache() {
    this.converterCache.clear();
  }

  @Override
  public String toString() {
    return this.converters.toString();
  }

  /**
   * A Proxy to a {@link TypeConverter}.
   */
  @SuppressWarnings("unchecked")
  private static class TypeConverterProxy {

    private final TypeConverter<Object, Object> delegate;
    private final Class<?> sourceClass;

    public TypeConverterProxy(TypeConverter<?, ?> delegate) {
      Class<?> typeInfo = getSourceTypeInfo(delegate.getClass(), TypeConverter.class);
      if (typeInfo == null && delegate instanceof DecoratingProxy) {
        typeInfo = getSourceTypeInfo(((DecoratingProxy) delegate).getDecoratedClass(), TypeConverter.class);
      }
      if (typeInfo == null) {
        throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your "
            + "Converter [" + delegate.getClass().getName() + "]; does the class parameterize those types?");
      }

      this.delegate = (TypeConverter<Object, Object>) delegate;
      this.sourceClass = typeInfo;
    }

    private static Class<?> getSourceTypeInfo(Class<?> converterClass, Class<?> genericIfc) {
      ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
      ResolvableType[] generics = resolvableType.getGenerics();
      if (generics.length < 2) {
        return null;
      }

      Class<?> sourceType = generics[0].resolve();
      Class<?> targetType = generics[1].resolve();
      if (sourceType == null || targetType == null) {
        return null;
      }

      return generics[0].resolve();
    }

    public boolean matches(ResolvableType actuallySourceType) {
      if (actuallySourceType == ResolvableType.NONE || actuallySourceType.hasUnresolvableGenerics()) {
        return false;
      }

      return this.sourceClass.isAssignableFrom(actuallySourceType.getRawClass());
    }

    public Type convert(Type source) {
      ResolvedType<Object> sourceType = (ResolvedType<Object>) ResolvedType.forType(source);
      ResolvedType<Object> targetType = this.delegate.convert(sourceType);

      return ResolvableType.forClass(targetType.getResolved()).getType();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.delegate.hashCode();

      return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof TypeConverterProxy)) {
        return false;
      }

      TypeConverterProxy otherProxy = (TypeConverterProxy) other;
      return this.delegate.equals(otherProxy.delegate);
    }

    @Override
    public String toString() {
      return "ConditionalTypeConverter [delegate=" + this.delegate + "]";
    }
  }

  /**
   * Key for use with the converter cache.
   */
  private static class ConverterCacheKey implements Comparable<ConverterCacheKey> {

    private final Type type;

    public ConverterCacheKey(Type type) {
      this.type = type;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.type.hashCode();

      return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof ConverterCacheKey)) {
        return false;
      }

      ConverterCacheKey otherKey = (ConverterCacheKey) other;
      return this.type.equals(otherKey.type);
    }

    @Override
    public String toString() {
      return "ConverterCacheKey [type=" + this.type + "]";
    }

    @Override
    public int compareTo(ConverterCacheKey other) {
      if (this.equals(other)) {
        return 0;
      }

      return this.getType().toString().compareTo(other.getType().toString());
    }

    public Type getType() {
      return this.type;
    }
  }
}
