package zenny.toybox.springfield.util.id.support;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.id.IdGenerator;
import zenny.toybox.springfield.util.id.support.Configuration.Customizer;

public abstract class AbstractIdGeneratorFactoryBean<ID extends Serializable & Comparable<ID>>
    extends AbstractFactoryBean<IdGenerator<ID>> {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  @Nullable
  private final Configuration config;

  public AbstractIdGeneratorFactoryBean() {
    this(null);
  }

  public AbstractIdGeneratorFactoryBean(@Nullable Configuration config, @Nullable Customizer... customizers) {
    Assert.noNullElements(customizers, "Customizers must contain no null elements");

    if (config != null && customizers != null) {
      for (Customizer customizer : customizers) {
        customizer.customize(config);
      }
    }

    this.config = config;
  }

  @Override
  protected IdGenerator<ID> createInstance() throws Exception {
    IdGenerator<ID> generator = this.doCreateInstance();
    if (generator == null) {
      throw new IllegalStateException("IdGenerator must be created");
    }

    return generator;
  }

  abstract protected IdGenerator<ID> doCreateInstance() throws Exception;

  protected Configuration getConfig() {
    return this.config;
  }
}
