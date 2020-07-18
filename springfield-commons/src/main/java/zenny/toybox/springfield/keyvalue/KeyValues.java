package zenny.toybox.springfield.keyvalue;

import java.util.Map;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;

/**
 * Usages
 * <p>
 * 1. use loaders only:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public KeyValueLoader&lt;String, String&gt; genderLoader() {
 *     return MapBasedKeyValueLoader.of("1", "female").append("2", "male").build();
 *   }
 * }
 *
 * &#064;Service
 * public class AppService {
 *
 *   &#064;Autowired
 *   private KeyValueLoader&lt;String, String&gt; genderLoader;
 * }
 * </pre>
 * <p>
 * 2. use holder only:
 *
 * <pre class="code">
 * &#064;EnableKeyValueSupport
 * &#064;Configuration
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public KeyValueHolder keyValueHolder() {
 *     Map&lt;String, String&gt; genders = new HashMap&lt;&gt;() {{
 *       this.put("1", "female");
 *       this.put("2", "male");
 *     }}
 *
 *     InMemoryKeyValueHolder holder = new InMemoryKeyValueHolder();
 *     holder.put("genders", genders);
 *
 *     return holder;
 *   }
 * }
 *
 * &#064;Service
 * public class AppService {
 *
 *   &#064;Autowired
 *   private KeyValues keyValues;
 *
 *   public void genderService() {
 *     Map&lt;String, String&gt; genders = this.keyValues.get("genders");
 *     // other logic...
 *   }
 * }
 * </pre>
 * <p>
 * 3. use both:
 *
 * <pre class="code">
 * &#064;EnableKeyValueSupport
 * &#064;Configuration
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public KeyValueLoader&lt;String, String&gt; genderLoader() {
 *     return MapBasedKeyValueLoader.of("1", "female").append("2", "male").build();
 *   }
 *
 *   &#064;Bean
 *   public KeyValueHolder keyValueHolder() {
 *     return new InMemoryKeyValueHolder();
 *   }
 * }
 *
 * &#064;Service
 * public class AppService {
 *
 *   &#064;Autowired
 *   private KeyValues keyValues;
 *
 *   public void genderService() {
 *     Map&lt;String, String&gt; genders = this.keyValues.get("genders");
 *     // other logic...
 *   }
 * }
 * </pre>
 */
public final class KeyValues {

  private final KeyValueManager manager;

  public KeyValues(KeyValueManager manager) {
    Assert.notNull(manager, "KeyValueManager must not be null");

    this.manager = manager;
  }

  @Nullable
  public Map<String, String> get(String name) {
    return this.get(name, String.class, String.class);
  }

  @Nullable
  public <K, V> Map<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
    return this.manager.getValue(name, keyType, valueType);
  }

  public void refresh(String name) {
    this.manager.refresh(name);
  }
}
