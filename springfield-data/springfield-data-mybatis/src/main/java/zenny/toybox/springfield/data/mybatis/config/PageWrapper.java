package zenny.toybox.springfield.data.mybatis.config;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.springframework.lang.Nullable;

class PageWrapper implements ObjectWrapper {

  private final PageProxy<Object> page;

  public PageWrapper(PageProxy<Object> page) {
    this.page = page;
  }

  @Override
  public Object get(@Nullable PropertyTokenizer prop) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(@Nullable PropertyTokenizer prop, @Nullable Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String findProperty(@Nullable String name, boolean useCamelCaseMapping) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getGetterNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getSetterNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getSetterType(@Nullable String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getGetterType(@Nullable String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSetter(@Nullable String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasGetter(@Nullable String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetaObject instantiatePropertyValue(@Nullable String name, @Nullable PropertyTokenizer prop,
      @Nullable ObjectFactory objectFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCollection() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(@Nullable Object element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> void addAll(List<E> element) {
    this.page.fill(element);
  }
}
