package zenny.toybox.springfield.keyvalue.annotation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValues;
import zenny.toybox.springfield.keyvalue.KeyValuesFactory;
import zenny.toybox.springfield.keyvalue.support.DefaultKeyValuesFactory;

@Configuration
public class KeyValueSupportConfiguration {

  @Autowired
  @Bean
  @ConditionalOnMissingBean
  public KeyValues keyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    KeyValuesFactory factory = new DefaultKeyValuesFactory();
    return factory.getKeyValues(loaders, holder);
  }
}
