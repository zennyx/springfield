package zenny.toybox.springfield.data.mybatis.repository.bridge.convert.support;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.BridgeConverter;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.ResolvedType;
import zenny.toybox.springfield.util.Assert;

public class ChainingBridgeConverter implements BridgeConverter<Object, Object> {

  private final Class<?> targetType;
  private final BridgeConverter<Object, Object> delegate;

  private ChainingBridgeConverter(Class<?> targetType, BridgeConverter<Object, Object> delegate) {
    Assert.notNull(targetType, "Targettype must not be null");
    Assert.notNull(delegate, "Delegate must not be null");

    this.targetType = targetType;
    this.delegate = delegate;
  }

  public static ChainingBridgeConverter of(Class<?> targetType, BridgeConverter<Object, Object> delegate) {
    return new ChainingBridgeConverter(targetType, delegate);
  }

  public ChainingBridgeConverter and(final BridgeConverter<Object, Object> converter) {
    Assert.notNull(converter, "Converter must not be null.");

    return new ChainingBridgeConverter(this.targetType, new BridgeConverter<Object, Object>() {

      @Override
      @Nullable
      public Object convert(Object source) {
        Object intermediate = ChainingBridgeConverter.this.convert(source);
        return ChainingBridgeConverter.this.targetType.isInstance(intermediate) ? intermediate
            : converter.convert(intermediate);
      }

      @Override
      public ResolvedType<Object> convert(ResolvedType<Object> source) {
        ResolvedType<Object> intermediate = ChainingBridgeConverter.this.convert(source);
        return ChainingBridgeConverter.this.targetType.equals(intermediate.getResolved()) ? intermediate
            : converter.convert(intermediate);
      }
    });
  }

  @Override
  @Nullable
  public Object convert(Object source) {
    return this.delegate.convert(source);
  }

  @Override
  public ResolvedType<Object> convert(ResolvedType<Object> source) {
    return this.delegate.convert(source);
  }
}
