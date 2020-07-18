package zenny.toybox.springfield.data.mybatis.repository.config;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import zenny.toybox.springfield.data.mybatis.repository.support.MyBatisRepositoryFactoryBean;

/**
 * Annotation to enable MyBatis repositories. Will scan the package of the
 * annotated configuration class for Spring Data repositories by default.
 *
 * @author Zenny Xu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(MyBatisRepositoriesRegistrar.class)
public @interface EnableMyBatisRepositories {

  /**
   * Alias for the {@link #basePackages()} attribute. Allows for more concise
   * annotation declarations e.g.: {@code @EnableJpaRepositories("org.my.pkg")}
   * instead of {@code @EnableJpaRepositories(basePackages="org.my.pkg")}.
   *
   * @return base package names
   */
  String[] value() default {};

  /**
   * Base packages to scan for annotated components. {@link #value()} is an alias
   * for (and mutually exclusive with) this attribute. Use
   * {@link #basePackageClasses()} for a type-safe alternative to String-based
   * package names.
   *
   * @return base package names
   * @see AnnotationRepositoryConfigurationSource
   */
  String[] basePackages() default {};

  /**
   * Type-safe alternative to {@link #basePackages()} for specifying the packages
   * to scan for annotated components. The package of each class specified will be
   * scanned. Consider creating a special no-op marker class or interface in each
   * package that serves no purpose other than being referenced by this attribute.
   *
   * @return classes that indicate base package for scanning
   * @see AnnotationRepositoryConfigurationSource
   */
  Class<?>[] basePackageClasses() default {};

  /**
   * Specifies which types are eligible for component scanning. Further narrows
   * the set of candidate components from everything in {@link #basePackages()} to
   * everything in the base packages that matches the given filter or filters.
   *
   * @return filters to include
   * @see AnnotationRepositoryConfigurationSource
   */
  Filter[] includeFilters() default {};

  /**
   * Specifies which types are not eligible for component scanning.
   *
   * @return filters to exclude
   * @see AnnotationRepositoryConfigurationSource
   */
  Filter[] excludeFilters() default {};

  /**
   * Returns the postfix to be used when looking up custom repository
   * implementations. Defaults to {@literal Impl}. So for a repository named
   * {@code PersonRepository} the corresponding implementation class will be
   * looked up scanning for {@code PersonRepositoryImpl}.
   *
   * @return the postfix
   * @see AnnotationRepositoryConfigurationSource
   */
  String repositoryImplementationPostfix() default "Impl";

  /**
   * Configures the location of where to find the Spring Data named queries
   * properties file. Will default to
   * {@code META-INF/jpa-named-queries.properties}.
   *
   * @return the location properties file
   * @see AnnotationRepositoryConfigurationSource
   */
  String namedQueriesLocation() default ""; // TODO

  /**
   * Returns the key of the {@link QueryLookupStrategy} to be used for lookup
   * queries for query methods. Defaults to {@link Key#CREATE_IF_NOT_FOUND}.
   *
   * @return the key of the {@link QueryLookupStrategy}
   * @see AnnotationRepositoryConfigurationSource
   */
  Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND; // TODO

  /**
   * Returns the {@link FactoryBean} class to be used for each repository
   * instance. Defaults to {@link MyBatisRepositoryFactoryBean}.
   *
   * @return the {@link FactoryBean} class
   * @see AnnotationRepositoryConfigurationSource
   */
  Class<?> repositoryFactoryBeanClass() default MyBatisRepositoryFactoryBean.class;

  /**
   * Configure the repository base class to be used to create repository proxies
   * for this particular configuration.
   *
   * @return the repository base class
   * @see AnnotationRepositoryConfigurationSource
   */
  Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

  /**
   * Configures whether nested repository-interfaces (e.g. defined as inner
   * classes) should be discovered by the repositories infrastructure.
   *
   * @see AnnotationRepositoryConfigurationSource
   */
  boolean considerNestedRepositories() default false; // TODO not implemented yet

  /**
   * Configures when the repositories are initialized in the bootstrap lifecycle.
   * {@link BootstrapMode#DEFAULT} (default) means eager initialization except all
   * repository interfaces annotated with {@link Lazy}, {@link BootstrapMode#LAZY}
   * means lazy by default including injection of lazy-initialization proxies into
   * client beans so that those can be instantiated but will only trigger the
   * initialization upon first repository usage (i.e a method invocation on it).
   * This means repositories can still be uninitialized when the application
   * context has completed its bootstrap. {@link BootstrapMode#DEFERRED} is
   * fundamentally the same as {@link BootstrapMode#LAZY}, but triggers repository
   * initialization when the application context finishes its bootstrap.
   *
   * @return the bootstrap mode
   * @see AnnotationRepositoryConfigurationSource
   */
  BootstrapMode bootstrapMode() default BootstrapMode.DEFAULT;

  // MyBatis specific configuration

  /**
   * The {@link BeanNameGenerator} class to be used for naming bridged mappers
   * within the Spring container.
   *
   * @return the class of {@link BeanNameGenerator}
   * @see org.mybatis.spring.annotation.MapperScan#nameGenerator()
   */
  Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

  /**
   * This property specifies the annotation that the scanner will search for.The
   * scanner will register all interfaces in the base package that also have he
   * specified annotation.
   * <p>
   * Note this can be combined with markerInterface.
   *
   * @return the annotation that the scanner will search for
   * @see org.mybatis.spring.annotation.MapperScan#annotationClass() @
   */
  Class<? extends Annotation> annotationClass() default Annotation.class; // TODO delete (in/exclude-filter)

  /**
   * This property specifies the parent that the scanner will search for. The
   * scanner will register all interfaces in the base package that also have the
   * specified interface class as a parent.
   * <p>
   * Note this can be combined with annotationClass.
   *
   * @return the parent that the scanner will search for
   * @see org.mybatis.spring.annotation.MapperScan#markerInterface()
   */
  Class<?> markerInterface() default Class.class; // TODO delete (in/exclude-filter)

  /**
   * Specifies which {@code SqlSessionTemplate} to use in the case that there is
   * more than one in the spring context. Usually this is only needed when you
   * have more than one datasource.
   *
   * @return the bean name of {@code SqlSessionTemplate}
   * @see org.mybatis.spring.annotation.MapperScan#sqlSessionTemplateRef()
   */
  String sqlSessionTemplateRef() default "";

  /**
   * Specifies which {@code SqlSessionFactory} to use in the case that there is
   * more than one in the spring context. Usually this is only needed when you
   * have more than one datasource.
   *
   * @return the bean name of {@code SqlSessionFactory}
   * @see org.mybatis.spring.annotation.MapperScan#sqlSessionFactoryRef()
   */
  String sqlSessionFactoryRef() default "";

  /**
   * Specifies a custom MapperFactoryBean to return a mybatis proxy as spring
   * bean.
   *
   * @return the class of {@code MapperFactoryBean}
   * @see org.mybatis.spring.annotation.MapperScan#factoryBean()
   */
  @SuppressWarnings("rawtypes")
  Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class; // TODO
}
