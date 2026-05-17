package zenny.toybox.springfield.messaging.config;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;
import zenny.toybox.springfield.messaging.DispatchStrategy;
import zenny.toybox.springfield.messaging.MessageDispatcher;
import zenny.toybox.springfield.messaging.MessageHandlerRegistry;
import zenny.toybox.springfield.messaging.MessageListenerFactory;
import zenny.toybox.springfield.messaging.MessagePublisher;
import zenny.toybox.springfield.messaging.support.DefaultMessageDispatcher;
import zenny.toybox.springfield.messaging.support.DefaultMessageHandlerRegistry;
import zenny.toybox.springfield.messaging.support.DefaultMessageListenerFactory;
import zenny.toybox.springfield.messaging.support.DefaultMessagePublisher;
import zenny.toybox.springfield.messaging.support.MessageListenerMethodProcessor;
import zenny.toybox.springfield.messaging.support.RandomDispatchStrategy;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingConfiguration {

  @Bean
  public MessageListenerFactory messageListenerFactory(ApplicationContext applicationContext) {
    return new DefaultMessageListenerFactory(applicationContext);
  }

  @Bean
  public MessageHandlerRegistry messageHandlerRegistry() {
    return new DefaultMessageHandlerRegistry();
  }

  @Bean
  public MessageListenerMethodProcessor messageListenerMethodProcessor(
      ApplicationContext applicationContext,
      List<MessageListenerFactory> factories,
      MessageHandlerRegistry registry) {
    return new MessageListenerMethodProcessor(applicationContext, factories, registry);
  }

  @Bean
  @ConditionalOnMissingBean(DispatchStrategy.class)
  public DispatchStrategy dispatchStrategy() {
    return new RandomDispatchStrategy();
  }

  @Bean
  @ConditionalOnMissingBean(MessageDispatcher.class)
  public MessageDispatcher messageDispatcher(
      MessageHandlerRegistry registry, DispatchStrategy strategy) {
    return new DefaultMessageDispatcher(registry, strategy);
  }

  @Bean
  @ConditionalOnMissingBean(name = "messagingBusinessExecutor")
  public ThreadPoolExecutor messagingBusinessExecutor(MessagingProperties props) {
    return new ThreadPoolExecutor(
        props.getCorePoolSize(),
        props.getMaxPoolSize(),
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(props.getQueueCapacity()),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  @Bean
  @ConditionalOnMissingBean(MessagePublisher.class)
  public MessagePublisher messagePublisher(
      MessageDispatcher dispatcher,
      ThreadPoolExecutor messagingBusinessExecutor,
      @Nullable ErrorHandler errorHandler) {
    return new DefaultMessagePublisher(dispatcher, messagingBusinessExecutor, errorHandler);
  }
}
