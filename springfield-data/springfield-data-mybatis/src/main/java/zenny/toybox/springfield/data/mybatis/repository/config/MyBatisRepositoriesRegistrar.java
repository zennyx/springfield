package zenny.toybox.springfield.data.mybatis.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * {@link ImportBeanDefinitionRegistrar} to enable
 * {@link EnableMyBatisRepositories} annotation.
 *
 * @author Zenny Xu
 */
class MyBatisRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableMyBatisRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new MyBatisRepositoryConfigExtension();
  }
}
