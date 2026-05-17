package zenny.toybox.springfield.messaging;

public interface MessageHandler {

  String getChannel();

  String getGroup();

  void handle(Message<?> message);
}
