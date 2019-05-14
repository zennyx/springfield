package zenny.toybox.springfield.data.mybatis.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
    RowBounds.class, ResultHandler.class }) })
public class PageAwareInterceptor implements Interceptor {

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    InvocationMetadata meta = new InvocationMetadata(invocation);
    Pageable pageable = meta.getPageable();
    if (pageable == null) {
      return invocation.proceed();
    }

    List<?> result = (List<?>) invocation.proceed();
    return new PagingList<>(result, pageable);
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(@Nullable Properties properties) {
    // Do nothing.
  }

  private static class InvocationMetadata {

    private static final Map<Key, Object> CACHE = new ConcurrentHashMap<>();

    private final Object target;
    private final Method method;
    private final Object[] arguments;

    public InvocationMetadata(Invocation invocation) {
      this.target = invocation.getTarget();
      this.method = invocation.getMethod();
      this.arguments = invocation.getArgs();
    }

    @Nullable
    public Pageable getPageable() {
      Object queryArgs = this.arguments[1];
      Object rowBounds = this.arguments[2];

      if (!Map.class.isInstance(queryArgs) || rowBounds != null) {
        return null;
      }

      Map<?, ?> paramMap = (Map<?, ?>) queryArgs;
      Key key = new Key(this.target, this.method);
      Object pageableName = null;
      Object pageableValue = null;
      if (!CACHE.containsKey(key)) {
        for (Entry<?, ?> entry : paramMap.entrySet()) {
          if (Pageable.class.isInstance(entry.getValue())) {
            pageableName = entry.getKey();
            pageableValue = entry.getValue();

            break;
          }
        }

        if (pageableValue == null) {
          return null;
        }

        CACHE.put(key, pageableName);
      } else {
        pageableName = CACHE.get(key);
        pageableValue = paramMap.get(pageableName);
      }

      paramMap.remove(pageableName);
      Pageable pageable = (Pageable) pageableValue;
      this.arguments[2] = new RowBounds(new Long(pageable.getOffset()).intValue(), pageable.getPageSize());

      return pageable;
    }
  }

  private static class Key {
    private final Object target;
    private final Method method;

    public Key(Object target, Method method) {
      this.target = target;
      this.method = method;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.method.hashCode();
      result = prime * result + this.target.hashCode();

      return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof Key)) {
        return false;
      }

      Key otherKey = (Key) other;
      return this.target.equals(otherKey.target) && this.method.equals(otherKey.method);
    }
  }
}
