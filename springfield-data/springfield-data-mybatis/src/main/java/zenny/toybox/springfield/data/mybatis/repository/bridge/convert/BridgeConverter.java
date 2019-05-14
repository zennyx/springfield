package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import org.springframework.core.convert.converter.Converter;

public interface BridgeConverter<S, T> extends Converter<S, T>, TypeConverter<S, T> {

}
