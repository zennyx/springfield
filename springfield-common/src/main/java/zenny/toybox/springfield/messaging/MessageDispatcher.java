package zenny.toybox.springfield.messaging;

public interface MessageDispatcher {

  void dispatch(Message<?> message);
}
