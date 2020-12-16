package zenny.toybox.springfield.util.id;

import java.io.Serializable;

/**
 * Contract for generating universally unique identifiers.
 *
 * @author Zenny Xu
 * @param <ID> the type of the generated identifier
 */
@FunctionalInterface
public interface IdGenerator<ID extends Serializable & Comparable<ID>> {

  /**
   * Generate a new identifier.
   *
   * @return the generated identifier
   */
  ID nextId();
}
