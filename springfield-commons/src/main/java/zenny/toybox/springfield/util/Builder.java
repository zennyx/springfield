package zenny.toybox.springfield.util;

public interface Builder<O, B extends Builder<O, B>> {

  O build();

  default Builder<O, B> self() {
    return this;
  }
}
