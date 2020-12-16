package zenny.toybox.springfield.util.id.support;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.id.IdGenerator;
import zenny.toybox.springfield.util.id.support.Configuration.Customizer;
import zenny.toybox.springfield.util.id.support.SnowflakeIdGenerator.IdentifierLookup;

public class SnowflakeIdGeneratorFactoryBean extends AbstractIdGeneratorFactoryBean<Long> {

  public static final String EPOCH_NAME = "springfield.id.snowflake.epoch";
  public static final String IDENTIFIER_NAME = "springfield.id.snowflake.identifier";
  public static final String SUB_IDENTIFIER_NAME = "springfield.id.snowflake.subIdentifier";

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
    IdentifierLookup idLookup = this.resolveIdentifierAttribute(config, IDENTIFIER_NAME);
    IdentifierLookup subIdLookup = this.resolveIdentifierAttribute(config, SUB_IDENTIFIER_NAME);

    if (epoch == null) {
      epoch = System.currentTimeMillis();

      if (idLookup == null && subIdLookup == null) {
        if (this.logger.isWarnEnabled()) {
          this.logger.warn("You are using the default configuration to generate the snowflake ID generator, "
              + "please make sure that this is for testing purposes only, otherwise it is not recommended");
        }
      }
    }

    return new SnowflakeIdGenerator(epoch, idLookup, subIdLookup);
  }

  @Nullable
  private Long resolveEpochAttribute(@Nullable Configuration config) {
    if (config != null) {
      Object epochObject = config.getAttribute(EPOCH_NAME);

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
