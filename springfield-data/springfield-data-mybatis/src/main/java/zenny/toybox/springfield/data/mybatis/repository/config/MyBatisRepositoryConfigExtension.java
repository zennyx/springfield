package zenny.toybox.springfield.data.mybatis.repository.config;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;

import zenny.toybox.springfield.data.mybatis.repository.MyBatisRepository;
import zenny.toybox.springfield.data.mybatis.repository.support.MyBatisRepositoryFactoryBean;

public class MyBatisRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

  private static final String MODULE_NAME = "MyBatis";

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#getModuleName()
   */
  @Override
  public String getModuleName() {
    return MODULE_NAME;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.config.RepositoryConfigurationExtension#
   * getRepositoryFactoryBeanClassName()
   */
  @Override
  public String getRepositoryFactoryBeanClassName() {
    return MyBatisRepositoryFactoryBean.class.getName();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#getModulePrefix()
   */
  @Override
  protected String getModulePrefix() {
    return this.getModuleName().toLowerCase(Locale.US);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
   */
  @Override
  protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
    return Collections.emptySet(); // TODO not sure yet
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
   */
  @Override
  protected Collection<Class<?>> getIdentifyingTypes() {
    return Collections.<Class<?>>singleton(MyBatisRepository.class);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans
   * .factory.support.BeanDefinitionBuilder,
   * org.springframework.data.repository.config.RepositoryConfigurationSource)
   */
  @Override
  public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans
   * .factory.support.BeanDefinitionBuilder,
   * org.springframework.data.repository.config.
   * AnnotationRepositoryConfigurationSource)
   */
  @Override
  public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans
   * .factory.support.BeanDefinitionBuilder,
   * org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
   */
  @Override
  public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#registerBeansForRoot(org.
   * springframework.beans.factory.support.BeanDefinitionRegistry,
   * org.springframework.data.repository.config.RepositoryConfigurationSource)
   */
  @Override
  public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
    // TODO
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryConfigurationExtensionSupport#getConfigurationInspectionClassLoader
   * (org.springframework.core.io.ResourceLoader)
   */
  @Override
  protected ClassLoader getConfigurationInspectionClassLoader(ResourceLoader loader) {
    return loader.getClassLoader(); // TODO InspectionClassLoader ?
  }
}
