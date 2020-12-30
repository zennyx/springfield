package zenny.toybox.springfield.util.keyvalue.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.util.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.keyvalue.KeyValues;
import zenny.toybox.springfield.util.keyvalue.KeyValuesFactory;
import zenny.toybox.springfield.util.keyvalue.support.DefaultKeyValuesFactory;

@Configuration
public class KeyValueSupportConfiguration {

  @Autowired
  @Bean
  @ConditionalOnMissingBean // TODO use more traditional way (e.g. ImportSelector) instead of spring-boot
  public KeyValues keyValues(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    KeyValuesFactory factory = new DefaultKeyValuesFactory();
    return factory.getKeyValues(loaders, holder);
  }
}
