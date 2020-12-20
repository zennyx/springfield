package zenny.toybox.springfield.util;

@FunctionalInterface
public interface HierarchicalBuilder<O, B extends HierarchicalBuilder<O, B>> {

  O build();

  default HierarchicalBuilder<O, B> self() {
    return this;
  }
}
