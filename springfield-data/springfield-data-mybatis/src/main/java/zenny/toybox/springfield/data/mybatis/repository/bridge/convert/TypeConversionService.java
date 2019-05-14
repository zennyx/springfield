package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

public interface TypeConversionService {

  boolean canConvert(@Nullable Type source);

  Type convert(Type source);

  void addConverter(@Nullable TypeConverter<?, ?> converter);
}
