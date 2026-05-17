package zenny.toybox.springfield.messaging.support;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zenny.toybox.springfield.messaging.DispatchStrategy;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageDispatcher;
import zenny.toybox.springfield.messaging.MessageHandler;
import zenny.toybox.springfield.messaging.MessageHandlerRegistry;

public class DefaultMessageDispatcher implements MessageDispatcher {

  protected final Log log = LogFactory.getLog(this.getClass());

  private final MessageHandlerRegistry registry;
  private final DispatchStrategy strategy;

  public DefaultMessageDispatcher(MessageHandlerRegistry registry, DispatchStrategy strategy) {
    this.registry = registry;
    this.strategy = strategy;
  }

  @Override
  public void dispatch(Message<?> message) {
    Map<String, List<MessageHandler>> groups =
        this.registry.getHandlerMap().get(message.getChannel());
    if (groups == null || groups.isEmpty()) {
      return;
    }
    for (Map.Entry<String, List<MessageHandler>> entry : groups.entrySet()) {
      List<MessageHandler> handlers = entry.getValue();
      List<MessageHandler> selected = this.strategy.select(handlers, message);
      for (MessageHandler handler : selected) {
        this.doDispatch(handler, message);
      }
    }
  }

  protected void doDispatch(MessageHandler handler, Message<?> message) {
    if (this.log.isDebugEnabled()) {
      this.log.debug(
          "dispatching message channel="
              + message.getChannel()
              + " group="
              + handler.getGroup()
              + " handler={}"
              + handler.getClass().getSimpleName());
    }
    handler.handle(message);
  }
}
