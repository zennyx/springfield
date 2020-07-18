package zenny.toybox.springfield.keyvalue;

public interface KeyValueManager {

  KeyValueHolder getHolder();

  void refresh(String name);
}
