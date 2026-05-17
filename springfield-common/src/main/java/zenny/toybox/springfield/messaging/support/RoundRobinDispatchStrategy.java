package zenny.toybox.springfield.messaging.support;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import zenny.toybox.springfield.messaging.DispatchStrategy;
import zenny.toybox.springfield.messaging.Message;
import zenny.toybox.springfield.messaging.MessageHandler;

public class RoundRobinDispatchStrategy implements DispatchStrategy {

  private final ConcurrentHashMap<Integer, AtomicInteger> counters = new ConcurrentHashMap<>();

  @Override
  public List<MessageHandler> select(List<MessageHandler> handlers, Message<?> message) {
    if (handlers.isEmpty()) {
      return List.of();
    }
    if (handlers.size() == 1) {
      return handlers;
    }
    int key = System.identityHashCode(handlers);
    int index =
        Math.floorMod(
            this.counters.computeIfAbsent(key, k -> new AtomicInteger(0)).getAndIncrement(),
            handlers.size());
    return List.of(handlers.get(index));
  }
}
