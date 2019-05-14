package zenny.toybox.springfield.convert.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

/**
 * A converter converts a source object of type {@code S} to a target of type
 * {@code T}, and vice versa.
 *
 * @author Zenny Xu
 * @param <S> the source type
 * @param <T> the target type
 */
public interface ReversibleConverter<S, T> extends Converter<S, T> {

  /**
   * Reverses the target object of type {@code T} to source type {@code S}.
   *
   * @param target the target object to reverse, which must be an instance of
   * {@code T} (never {@code null})
   * @return the reversed object, which must be an instance of {@code S}
   * (potentially {@code null})
   * @throws IllegalArgumentException if the target cannot be reversed to the
   * desired source type
   */
  @Nullable
  S reverse(T target);
}
