package zenny.toybox.springfield.util.keyvalue.support;

import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

public class SimpleJdbcBasedKeyValueLoader extends JdbcBasedKeyValueLoader<String, String> {

  public SimpleJdbcBasedKeyValueLoader(DataSource dataSource, String sql, String keyColumnName,
      String valueColumnName) {
    super(dataSource, sql, toIteratee(keyColumnName), toIteratee(valueColumnName));
  }

  public SimpleJdbcBasedKeyValueLoader(JdbcTemplate template, String sql, String keyColumnName,
      String valueColumnName) {
    super(template, sql, toIteratee(keyColumnName), toIteratee(valueColumnName));
  }

  private static Function<Map<String, Object>, String> toIteratee(String columnName) {
    Assert.hasLength(columnName, "ColumnName must not be empty");

    return (map) -> {
      Object columnValue = map.get(columnName);

      if (columnValue == null) {
        return null;
      }
      return columnValue.toString();
    };
  }
}
