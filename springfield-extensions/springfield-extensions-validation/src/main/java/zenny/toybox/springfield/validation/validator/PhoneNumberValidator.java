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
import zenny.toybox.springfield.validation.constraint.PhoneNumber;
import zenny.toybox.springfield.validation.constraint.PhoneNumber.Type;

@Lab
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, CharSequence> {

  private static final Map<Key, Rule<CharSequence>> RULES = new HashMap<>();

  private Locale locale;
  private Type type;
  private boolean bypass;

  static {
    RULES.put(new Key(Locale.CHINA, Type.LINE), new Rule<CharSequence>() {

      private final Pattern mode = Pattern.compile("^(0[0-9]{2,3}\\\\-)?([2-9][0-9]{6,7})+(\\\\-[0-9]{1,4})?$");

      @Override
      public boolean isValid(CharSequence value, ConstraintValidatorContext context, @Nullable Object... rest) {
        return this.mode.matcher(value).matches();
      }

    });

    RULES.put(new Key(Locale.CHINA, Type.MOBILE), new Rule<CharSequence>() {

      private final Pattern mode = Pattern
          .compile("^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$");

      @Override
      public boolean isValid(CharSequence value, ConstraintValidatorContext context, @Nullable Object... rest) {
        return this.mode.matcher(value).matches();
      }

    });
  }

  public static void addRule(Locale locale, Type type, Rule<CharSequence> rule) {
    Assert.notNull(locale, "The given locale must not be null");
    Assert.notNull(type, "The given type must not be null");
    Assert.notNull(rule, "The given rule must not be null");

    RULES.put(new Key(locale, type), rule);
  }

  @Override
  public void initialize(PhoneNumber constraintAnnotation) {
    Assert.hasText(constraintAnnotation.countryOrRegion(), "The given region must not be empty");
    Assert.notNull(constraintAnnotation.type(), "The given type must not be null");

    this.locale = Locale.forLanguageTag(constraintAnnotation.countryOrRegion());
    this.type = constraintAnnotation.type();
    this.bypass = constraintAnnotation.bypassIfNoRuleAvailable();
  }

  @Override
  public boolean isValid(@Nullable CharSequence value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    Rule<CharSequence> rule = RULES.get(new Key(this.locale, this.type));
    if (rule == null) {
      return this.bypass;
    }

    return rule.isValid(value, context);
  }

  private static final class Key {

    private final Locale locale;
    private final Type type;

    private Key(Locale locale, Type type) {
      this.locale = locale;
      this.type = type;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.locale.hashCode();
      result = prime * result + this.type.hashCode();

      return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (this == other) {
        return true;
      }

      if (!(other instanceof Key)) {
        return false;
      }

      Key otherKey = (Key) other;
      return this.locale.equals(otherKey.locale) && this.type == otherKey.type;
    }
  }
}
