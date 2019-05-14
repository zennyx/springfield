package zenny.toybox.springfield.data.mybatis.config;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.springframework.data.domain.Page;

public class PageAwareObjectFactory extends DefaultObjectFactory {

  private static final long serialVersionUID = 1592731121984079354L;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <T> T create(Class<T> type) {
    if (Page.class.isAssignableFrom(type)) {
      return (T) new PageProxy();
    }

    return super.create(type);
  }
}
