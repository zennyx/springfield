package zenny.toybox.springfield.util.keyvalue.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.keyvalue.KeyValueLoader;

public class JdbcBasedKeyValueLoader<K, V> implements KeyValueLoader<K, V> {

  private final JdbcTemplate template;

  private final String sql;

  private final Function<Map<String, Object>, K> keyIteratee;

  private final Function<Map<String, Object>, V> valueIteratee;

  public JdbcBasedKeyValueLoader(DataSource dataSource, String sql, Function<Map<String, Object>, K> keyIteratee,
      Function<Map<String, Object>, V> valueIteratee) {
    this(new JdbcTemplate(dataSource), sql, keyIteratee, valueIteratee);
  }

  public JdbcBasedKeyValueLoader(JdbcTemplate template, String sql, Function<Map<String, Object>, K> keyIteratee,
      Function<Map<String, Object>, V> valueIteratee) {
    Assert.notNull(template, "JdbcTemplate must not be null");
    Assert.notNull(template.getDataSource(), "DataSource must not be null");
    Assert.hasText(sql, "Sql must contain valid text content");
    Assert.notNull(keyIteratee, "KeyIteratee must not be null");
    Assert.notNull(valueIteratee, "ValueIteratee must not be null");

    this.template = template;
    this.sql = sql;
    this.keyIteratee = keyIteratee;
    this.valueIteratee = valueIteratee;
  }

  @Override
  @Nullable
  public Map<K, V> load() {
    List<Map<String, Object>> queryResult = this.template.queryForList(this.sql);
    if (queryResult.isEmpty()) {
      return null;
    }

    Map<K, V> source = new HashMap<>();
    queryResult.forEach(row -> {
      source.put(this.keyIteratee.apply(row), this.valueIteratee.apply(row));
    });

    return source;
  }
}
