package zenny.toybox.springfield.data.mybatis.repository.bridge.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;

import org.springframework.data.repository.core.RepositoryInformation;

import zenny.toybox.springfield.data.mybatis.repository.MyBatisRepository;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.BridgeConverter;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.TypeConversionService;
import zenny.toybox.springfield.data.mybatis.repository.bridge.convert.support.DefaultTypeConversionService;
import zenny.toybox.springfield.util.CollectionUtils;

public class SimpleBridgeMapperFactory extends BridgeMapperFactorySupport {

  private final TypeConversionService conversionService;

  public SimpleBridgeMapperFactory() {
    this(null);
  }

  public SimpleBridgeMapperFactory(Collection<BridgeConverter<?, ?>> converters) {
    TypeConversionService conversionService = this.getConversionService();

    BridgeMapperConverters.registerConvertersIn(conversionService);

    if (!CollectionUtils.isEmpty(converters)) {
      for (BridgeConverter<?, ?> converter : converters) {
        if (Objects.isNull(converter)) {
          continue;
        }
        conversionService.addConverter(converter);
      }
    }

    this.conversionService = conversionService;
  }

  /**
   * Returns a {@link TypeConversionService} to support converting {@link Type}s
   * of parameters and returnTypes. Will default to a
   * {@link DefaultTypeConversionService} instance. This may be useful for example
   * to allow custom {@link BridgeConverter}s to be registered and then insert
   * default ones through this method.
   *
   * @return a {@code TypeConversionService} to convert {@code Type}s
   */
  protected TypeConversionService getConversionService() {
    return new DefaultTypeConversionService();
  }

  /*
   * (non-Javadoc)
   * @see zenny.toybox.springfield.data.mybatis.repository.bridge.support.
   * BridgeMapperFactorySupport#isValid(org.springframework.data.repository.core.
   * RepositoryInformation)
   */
  @Override
  protected boolean isValid(RepositoryInformation information) {
    return MyBatisRepository.class.isAssignableFrom(information.getRepositoryInterface()); // TODO need confirm
  }

  /*
   * (non-Javadoc)
   * @see zenny.toybox.springfield.data.mybatis.repository.bridge.support.
   * BridgeMapperFactorySupport#getMapperPostfix()
   */
  @Override
  protected String getMapperPostfix() {
    return DEFAULT_BRIDGE_MAPPER_POSTFIX;
  }

  /*
   * (non-Javadoc)
   * @see zenny.toybox.springfield.data.mybatis.repository.bridge.support.
   * BridgeMapperFactorySupport#isCandidateMethod(java.lang.reflect.Method,
   * org.springframework.data.repository.core.RepositoryInformation)
   */
  @Override
  protected boolean isCandidateMethod(Method method, RepositoryInformation information) {
    return !information.isCustomMethod(method);
  }

  /*
   * (non-Javadoc)
   * @see zenny.toybox.springfield.data.mybatis.repository.bridge.support.
   * BridgeMapperFactorySupport#processType(java.lang.reflect.Type,
   * java.lang.reflect.Method,
   * org.springframework.data.repository.core.RepositoryInformation)
   */
  @Override
  protected Type processType(Type type, Method method, RepositoryInformation information) {
    return this.conversionService.canConvert(type) ? this.conversionService.convert(type) : type;
  }

  /*
   * (non-Javadoc)
   * @see zenny.toybox.springfield.data.mybatis.repository.bridge.support.
   * BridgeMapperFactorySupport#processMethodAnnotations(java.lang.annotation.
   * Annotation[], org.springframework.data.repository.core.RepositoryInformation)
   */
  @Override
  protected Annotation[] processMethodAnnotations(Annotation[] annotations, RepositoryInformation information) {
    return annotations; // TODO add annotations according to CRUD
  }
}
