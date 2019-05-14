package zenny.toybox.springfield.data.mybatis.repository.config;

import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

class MyBatisRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
   */
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    // TODO Auto-generated method stub
    return EnableMyBatisRepositories.class;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.config.
   * RepositoryBeanDefinitionRegistrarSupport#getExtension()
   */
  @Override
  protected RepositoryConfigurationExtension getExtension() {
    // TODO Auto-generated method stub
    return new MyBatisRepositoryConfigExtension();
  }
}
