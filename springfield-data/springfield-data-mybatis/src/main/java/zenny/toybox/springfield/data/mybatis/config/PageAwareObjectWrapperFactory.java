package zenny.toybox.springfield.data.mybatis.config;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.lang.Nullable;

public class PageAwareObjectWrapperFactory implements ObjectWrapperFactory {

  @Override
  public boolean hasWrapperFor(@Nullable Object object) {
    if (PageProxy.class.isInstance(object)) {
      return true;
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectWrapper getWrapperFor(@Nullable MetaObject metaObject, Object object) {
    return new PageWrapper((PageProxy<Object>) object);
  }
}
