package zenny.toybox.springfield.data.mybatis.config;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class MyBatisWebSupportConfiguration {

  @Autowired
  private Collection<Interceptor> plugins;

  @Autowired
  public MyBatisWebSupportConfiguration(@Nullable SqlSessionFactoryBean sqlSessionFactory) {
    if (sqlSessionFactory == null) {
      return;
    }

    sqlSessionFactory.setObjectFactory(new PageAwareObjectFactory());
    sqlSessionFactory.setObjectWrapperFactory(new PageAwareObjectWrapperFactory());
    sqlSessionFactory.setPlugins(this.resolvePlugins());
  }

  private Interceptor[] resolvePlugins() {
    List<Interceptor> plugins = new LinkedList<>();
    plugins.add(new PageAwareInterceptor());
    plugins.addAll(this.plugins);

    return plugins.toArray(new Interceptor[] {});
  }
}
