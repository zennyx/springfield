package zenny.toybox.springfield.util.id.config;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.id.IdGenerator;
import zenny.toybox.springfield.util.id.config.Configuration.Customizer;
import zenny.toybox.springfield.util.id.support.SnowflakeIdGenerator;
import zenny.toybox.springfield.util.id.support.SnowflakeIdGenerator.IdentifierLookup;

public class SnowflakeIdGeneratorFactoryBean extends AbstractIdGeneratorFactoryBean<Long> {

  public static final String ATTR_NAME_EPOCH = "springfield.id.snowflake.epoch";
  public static final String ATTR_NAME_IDENTIFIER = "springfield.id.snowflake.identifier";
  public static final String ATTR_NAME_SUB_IDENTIFIER = "springfield.id.snowflake.subIdentifier";

  public SnowflakeIdGeneratorFactoryBean() {
    super();
  }

  public SnowflakeIdGeneratorFactoryBean(@Nullable Configuration config, @Nullable Customizer... customizers) {
    super(config, customizers);
  }

  @Override
  public Class<?> getObjectType() {
    return SnowflakeIdGenerator.class;
  }

  @Override
  protected IdGenerator<Long> doCreateInstance() throws Exception {

    Configuration config = this.getConfig();
    Long epoch = this.resolveEpochAttribute(config);
    IdentifierLookup lookuper = this.resolveIdentifierAttribute(config, ATTR_NAME_IDENTIFIER);
    IdentifierLookup subLookuper = this.resolveIdentifierAttribute(config, ATTR_NAME_SUB_IDENTIFIER);

    if (epoch == null) {
      epoch = System.currentTimeMillis();

      if (lookuper == null && subLookuper == null) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("You are using the default configuration to generate the snowflake ID generator, "
              + "please make sure that this is for testing purposes only, otherwise it is not recommended");
        }
      }
    }

    return new SnowflakeIdGenerator(epoch, lookuper, subLookuper);
  }

  @Nullable
  private Long resolveEpochAttribute(@Nullable Configuration config) {
    if (config != null) {
      Object epochObject = config.getAttribute(ATTR_NAME_EPOCH);

      if (epochObject != null) {
        if (epochObject instanceof Long) {
          return (Long) epochObject;
        }

        throw new IllegalArgumentException(
            "Fail to load 'epoch' attribute from the configuration. The type of the attribute is incorrect");
      }
    }

    return null;
  }

  @Nullable
  private IdentifierLookup resolveIdentifierAttribute(@Nullable Configuration config, String attrName) {
    if (config == null) {
      return null;
    }

    Object identifierObject = config.getAttribute(attrName);
    if (identifierObject == null) {
      return null;
    }

    if (identifierObject instanceof IdentifierLookup) {
      return (IdentifierLookup) identifierObject;
    }

    if (identifierObject instanceof Long) {
      return () -> (Long) identifierObject;
    }

    throw new IllegalArgumentException(
        "Fail to load 'identifier' or 'subIdentifier' attributes from the configuration. The type of the attribute is incorrect");
  }
}
