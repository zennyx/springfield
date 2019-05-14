package zenny.toybox.springfield.validation.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import zenny.toybox.springfield.lang.Lab;
import zenny.toybox.springfield.validation.constraint.ZipCode.List;
import zenny.toybox.springfield.validation.validator.ZipCodeValidator;

@Documented
@Retention(RUNTIME)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Repeatable(List.class)
@Constraint(validatedBy = ZipCodeValidator.class)
@Lab
public @interface ZipCode {

  String countryOrRegion() default "zh_CN";

  boolean bypassIfNoRuleAvailable() default true;

  String message() default "{zenny.toybox.springfield.validation.constraint.ZipCode.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
  @Retention(RUNTIME)
  @Documented
  public @interface List {
    ZipCode[] value();
  }
}
