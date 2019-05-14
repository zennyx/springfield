package zenny.toybox.springfield.validation.validator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.lang.Lab;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.validation.Rule;
import zenny.toybox.springfield.validation.constraint.ZipCode;

@Lab
public class ZipCodeValidator implements ConstraintValidator<ZipCode, CharSequence> {

  private static final Map<Locale, Rule<CharSequence>> RULES = new HashMap<>();

  private Locale locale;
  private boolean bypass;

  static {
    RULES.put(Locale.CHINA, new Rule<CharSequence>() {

      private final Pattern mode = Pattern.compile("^[1-9]\\\\d{5}$");

      @Override
      public boolean isValid(CharSequence value, ConstraintValidatorContext context, @Nullable Object... rest) {
        return this.mode.matcher(value).matches();
      }
    });
  }

  public static void addRule(Locale locale, Rule<CharSequence> rule) {
    Assert.notNull(locale, "The given locale must not be null");
    Assert.notNull(rule, "The given rule must not be null");

    RULES.put(locale, rule);
  }

  @Override
  public void initialize(ZipCode constraintAnnotation) {
    Assert.hasText(constraintAnnotation.countryOrRegion(), "The given region must not be empty");

    this.locale = Locale.forLanguageTag(constraintAnnotation.countryOrRegion());
    this.bypass = constraintAnnotation.bypassIfNoRuleAvailable();
  }

  @Override
  public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    Rule<CharSequence> rule = RULES.get(this.locale);
    if (rule == null) {
      return this.bypass;
    }

    return rule.isValid(value, context);
  }
}
