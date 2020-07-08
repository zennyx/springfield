package zenny.toybox.springfield.keyvalue.annotation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.keyvalue.KeyValueHolder;
import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.keyvalue.KeyValueManager;
import zenny.toybox.springfield.keyvalue.KeyValues;
import zenny.toybox.springfield.keyvalue.support.DefaultKeyValueManager;
import zenny.toybox.springfield.keyvalue.support.InMemoryKeyValueHolder;

@Configuration
public class KeyValueSupportConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public KeyValueHolder keyValueHolder() {
    return new InMemoryKeyValueHolder();
  }

  @Autowired
  @Bean
  @ConditionalOnMissingBean
  public KeyValueManager keyValueManager(@Nullable Map<String, KeyValueLoader<?, ?>> loaders, KeyValueHolder holder) {
    return new DefaultKeyValueManager(loaders, holder);
  }

  @Autowired
  @Bean
  @ConditionalOnMissingBean
  public KeyValues keyValues(KeyValueManager keyValueManager) {
    return new KeyValues(keyValueManager);
  }
}
