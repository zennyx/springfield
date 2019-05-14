package zenny.toybox.springfield.data.mybatis.repository.config;

import java.util.Locale;

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

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

}
