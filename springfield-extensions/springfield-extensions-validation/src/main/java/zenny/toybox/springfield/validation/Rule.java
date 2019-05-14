package zenny.toybox.springfield.validation;

import javax.validation.ConstraintValidatorContext;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface Rule<T> {

  boolean isValid(@Nullable T value, ConstraintValidatorContext context, @Nullable Object... rest);
}
