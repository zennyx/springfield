package zenny.toybox.springfield.keyvalue.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.PropertiesPersister;

import zenny.toybox.springfield.keyvalue.KeyValueLoader;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;
import zenny.toybox.springfield.util.HierarchicalBuilder;

public class ResourceBasedKeyValueLoader<K, V> implements KeyValueLoader<K, V> {

  private final ResourceBundleMessageSourceAgent agent;

  private final Function<String, K> keyIteratee;

  private final Function<String, V> valueIteratee;

  private ResourceBasedKeyValueLoader(ResourceBundleMessageSourceAgent agent, Function<String, K> keyIteratee,
      Function<String, V> valueIteratee) {
    Assert.notNull(agent, "Agent must not be null");
    Assert.notNull(keyIteratee, "KeyIteratee must not be null");
    Assert.notNull(valueIteratee, "ValueIteratee must not be null");

    this.agent = agent;
    this.keyIteratee = keyIteratee;
    this.valueIteratee = valueIteratee;
  }

  public static <K, V> ResourceBasedKeyValueLoaderBuilder<K, V> includes(@Nullable String... basenames) {
    return new ResourceBasedKeyValueLoaderBuilder<>(basenames);
  }

  @Override
  public Map<K, V> load() {
    Locale currentLocale = LocaleContextHolder.getLocale();
    Set<String> keys = this.agent.getKeySet(currentLocale);
    if (CollectionUtils.isEmpty(keys)) {
      return null;
    }

    Map<K, V> result = new HashMap<>();
    keys.forEach(key -> {
      result.put(this.keyIteratee.apply(key),
          this.valueIteratee.apply(this.agent.getMessage(key, null, currentLocale)));
    });

    return result;
  }

  private static class ResourceBundleMessageSourceAgent extends ReloadableResourceBundleMessageSource {

    private Set<String> getKeySet(Locale locale) {
      return this.getMergedProperties(locale).getProperties().stringPropertyNames();
    }
  }

  public static class ResourceBasedKeyValueLoaderBuilder<K, V>
      implements HierarchicalBuilder<KeyValueLoader<K, V>, ResourceBasedKeyValueLoaderBuilder<K, V>> {

    @Nullable
    private String[] baseNames = null;

    private long cacheMillis = -1;

    private int cacheSeconds = -1;

    private boolean concurrentRefresh = true;

    @Nullable
    private String defaultEncoding = null;

    private boolean fallbackToSystemLocale = true;

    @Nullable
    private Properties fileEncodings = null;

    @Nullable
    private PropertiesPersister propertiesPersister = null;

    @Nullable
    private ResourceLoader resourceLoader = null;

    @Nullable
    private Function<String, K> keyIteratee = null;

    @Nullable
    private Function<String, V> valueIteratee = null;

    private ResourceBasedKeyValueLoaderBuilder(@Nullable String... basenames) {
      this.baseNames = basenames;
    }

    @Override
    public ResourceBasedKeyValueLoaderBuilder<K, V> self() {
      return this;
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> cacheMillis(long cacheMillis) {
      this.cacheMillis = cacheMillis;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> cacheSeconds(int cacheSeconds) {
      this.cacheSeconds = cacheSeconds;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> concurrentRefresh(boolean concurrentRefresh) {
      this.concurrentRefresh = concurrentRefresh;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> defaultEncoding(@Nullable String defaultEncoding) {
      this.defaultEncoding = defaultEncoding;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> fallbackToSystemLocale(boolean fallbackToSystemLocale) {
      this.fallbackToSystemLocale = fallbackToSystemLocale;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> propertiesPersister(
        @Nullable PropertiesPersister propertiesPersister) {
      this.propertiesPersister = propertiesPersister;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> resourceLoader(@Nullable ResourceLoader resourceLoader) {
      this.resourceLoader = resourceLoader;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> keyIteratee(@Nullable Function<String, K> keyIteratee) {
      this.keyIteratee = keyIteratee;

      return this.self();
    }

    public ResourceBasedKeyValueLoaderBuilder<K, V> valueIteratee(@Nullable Function<String, V> valueIteratee) {
      this.valueIteratee = valueIteratee;

      return this.self();
    }

    @Override
    public KeyValueLoader<K, V> build() {
      ResourceBundleMessageSourceAgent agent = new ResourceBundleMessageSourceAgent();
      agent.setBasenames(this.baseNames);
      agent.setCacheMillis(this.cacheMillis);
      agent.setCacheSeconds(this.cacheSeconds);
      agent.setConcurrentRefresh(this.concurrentRefresh);
      agent.setDefaultEncoding(this.defaultEncoding);
      agent.setFallbackToSystemLocale(this.fallbackToSystemLocale);
      agent.setFileEncodings(this.fileEncodings);
      if (this.propertiesPersister != null) {
        agent.setPropertiesPersister(this.propertiesPersister);
      }
      if (this.resourceLoader != null) {
        agent.setResourceLoader(this.resourceLoader);
      }
      if (this.keyIteratee == null) {
        this.keyIteratee = (key) -> {
          return null;
        };
      }
      if (this.valueIteratee == null) {
        this.valueIteratee = (value) -> {
          return null;
        };
      }

      return new ResourceBasedKeyValueLoader<>(agent, this.keyIteratee, this.valueIteratee);
    }
  }
}
