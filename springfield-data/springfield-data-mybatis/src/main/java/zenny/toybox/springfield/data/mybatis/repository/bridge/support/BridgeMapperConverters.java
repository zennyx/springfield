package zenny.toybox.springfield.data.mybatis.repository.bridge.support;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.BridgeConverter;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.ResolvedType;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConversionService;
import zenny.toybox.springfield.lang.Internal;
import zenny.toybox.springfield.util.Assert;

/**
 * Simple domain service to convert query parameters/results into a dedicated
 * type.
 *
 * @author Zenny Xu
 */
@Internal
public class BridgeMapperConverters {

  /**
   * Registers converters for type conversions of MyBatis bridge mapper.
   *
   * @param conversionService a service for type conversion, must not be
   * {@literal null}
   */
  public static void registerConvertersIn(TypeConversionService conversionService) {
    Assert.notNull(conversionService, "ConversionService must not be null");

    conversionService.addConverter(new PageableToRowBoundsConverter());
    conversionService.addConverter(new SliceToCollectionConverter());
    conversionService.addConverter(new SliceToIterableConverter());
  }

  /**
   * Registers converters for value conversions of MyBatis bridge mapper.
   *
   * @param conversionService a service for value conversion, must not be
   * {@literal null}
   */
  public static void registerConvertersIn(ConfigurableConversionService conversionService) {
    Assert.notNull(conversionService, "ConversionService must not be null");

    conversionService.addConverter(new PageableToRowBoundsConverter());
    conversionService.addConverter(new SliceToCollectionConverter());
    conversionService.addConverter(new SliceToIterableConverter());
  }

  /**
   * A {@link BridgeConverter} to support converting from {@link Pageable} to
   * {@link RowBounds}.
   */
  private static class PageableToRowBoundsConverter implements BridgeConverter<Pageable, RowBounds> {

    @Override
    public RowBounds convert(Pageable source) {
      Assert.notNull(source, "Source must not be null.");

      return new RowBounds(source.getPageNumber(), source.getPageSize());
    }

    @Override
    public ResolvedType<RowBounds> convert(ResolvedType<Pageable> source) {
      Assert.notNull(source, "Source must not be null.");

      return ResolvedType.forClass(RowBounds.class);
    }
  }

  /**
   * A {@link BridgeConverter} to support converting from {@link Slice} to
   * {@link Collection}.
   */
  private static class SliceToCollectionConverter implements BridgeConverter<Slice<?>, Collection<?>> {

    @Override
    public Collection<?> convert(Slice<?> source) {
      Assert.notNull(source, "Source must not be null");

      return source.getContent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResolvedType<Collection<?>> convert(ResolvedType<Slice<?>> source) {
      Assert.notNull(source, "Source must not be null");

      ResolvableType resolvableType = ResolvableType.forClass(source.getResolved());
      ResolvableType[] generics = resolvableType.getGenerics();
      ResolvableType targetType = ResolvableType.forClassWithGenerics(List.class, generics);

      return (ResolvedType<Collection<?>>) ResolvedType.forType(targetType.getType());
    }
  }

  /**
   * A {@link BridgeConverter} to support converting from {@link Slice} to
   * {@link Iterable}.
   */
  private static class SliceToIterableConverter implements BridgeConverter<Slice<?>, Iterable<?>> {

    @Override
    public Iterable<?> convert(Slice<?> source) {
      Assert.notNull(source, "Source must not be null");

      return source.getContent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResolvedType<Iterable<?>> convert(ResolvedType<Slice<?>> source) {
      Assert.notNull(source, "Source must not be null");

      ResolvableType resolvableType = ResolvableType.forClass(source.getResolved());
      ResolvableType[] generics = resolvableType.getGenerics();
      ResolvableType targetType = ResolvableType.forClassWithGenerics(List.class, generics);

      return (ResolvedType<Iterable<?>>) ResolvedType.forType(targetType.getType());
    }
  }
}
