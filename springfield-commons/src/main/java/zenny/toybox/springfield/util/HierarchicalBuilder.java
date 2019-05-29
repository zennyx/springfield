package zenny.toybox.springfield.util;

public interface HierarchicalBuilder<O, B extends HierarchicalBuilder<O, B>> {

  O build();

  default HierarchicalBuilder<O, B> self() {
    return this;
  }
}
