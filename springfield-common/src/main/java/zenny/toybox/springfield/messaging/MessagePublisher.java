package zenny.toybox.springfield.messaging;

public interface MessagePublisher {

  void publish(Message<?> message);
}
