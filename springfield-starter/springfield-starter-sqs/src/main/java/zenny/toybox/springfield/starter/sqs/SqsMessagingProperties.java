package zenny.toybox.springfield.starter.sqs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import zenny.toybox.springfield.sqs.SqsMessagingConfig;

@ConfigurationProperties(prefix = "springfield.messaging.sqs")
public class SqsMessagingProperties extends SqsMessagingConfig {}
