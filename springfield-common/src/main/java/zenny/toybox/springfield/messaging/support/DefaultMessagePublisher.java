package zenny.toybox.springfield.messaging.support;

import java.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ErrorHandler;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageDispatcher;
import zenny.toybox.springfield.messaging.MessagePublisher;

public class DefaultMessagePublisher implements MessagePublisher {

  protected final Log log = LogFactory.getLog(this.getClass());

  private final MessageDispatcher dispatcher;
  private final @Nullable Executor executor;
  private final @Nullable ErrorHandler errorHandler;

  public DefaultMessagePublisher(
      MessageDispatcher dispatcher,
      @Nullable Executor executor,
      @Nullable ErrorHandler errorHandler) {
    this.dispatcher = dispatcher;
    this.executor = executor;
    this.errorHandler = errorHandler;
  }

  @Override
  public void publish(Message<?> message) {
    if (this.executor != null) {
      this.executor.execute(() -> this.invokeDispatcher(message));
    } else {
      this.invokeDispatcher(message);
    }
  }

  protected void invokeDispatcher(Message<?> message) {
    if (this.log.isDebugEnabled()) {
      this.log.debug(
          "publishing message channel="
              + message.getChannel()
              + " payloadType="
              + message.getPayload().getClass().getSimpleName());
    }
    try {
      this.dispatcher.dispatch(message);
      message.acknowledge();
    } catch (Exception ex) {
      this.invokeErrorHandler(message, ex);
    }
  }

  protected void invokeErrorHandler(Message<?> message, Throwable ex) {
    if (this.errorHandler != null) {
      this.errorHandler.handleError(ex);
    } else {
      this.log.error("failed to dispatch message channel=" + message.getChannel(), ex);
    }
  }
}
