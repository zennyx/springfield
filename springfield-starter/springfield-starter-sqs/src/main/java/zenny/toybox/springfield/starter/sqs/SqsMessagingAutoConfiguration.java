package zenny.toybox.springfield.starter.sqs;

import java.net.URI;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import zenny.toybox.springfield.messaging.MessagePublisher;
import zenny.toybox.springfield.sqs.SqsMessageReceiver;
import zenny.toybox.springfield.sqs.SqsMessageSender;

@AutoConfiguration
@ConditionalOnClass(SqsClient.class)
@ConditionalOnProperty(
    prefix = "springfield.messaging.sqs",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(SqsMessagingProperties.class)
public class SqsMessagingAutoConfiguration {

  @Bean
  public SqsClient sqsClient(SqsMessagingProperties properties) {
    var builder = SqsClient.builder().region(Region.of(properties.getRegion()));
    if (properties.getAccessKey() != null && properties.getSecretKey() != null) {
      builder.credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
    } else {
      builder.credentialsProvider(DefaultCredentialsProvider.create());
    }
    if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(properties.getEndpoint()));
    }
    return builder.build();
  }

  @Bean
  public SqsMessageSender sqsMessageSender(SqsClient sqsClient, SqsMessagingProperties properties) {
    return new SqsMessageSender(sqsClient, properties);
  }

  @Bean
  public SqsMessageReceiver sqsMessageReceiver(
      SqsClient sqsClient, SqsMessagingProperties properties, MessagePublisher messagePublisher) {
    return new SqsMessageReceiver(sqsClient, properties, messagePublisher);
  }
}
