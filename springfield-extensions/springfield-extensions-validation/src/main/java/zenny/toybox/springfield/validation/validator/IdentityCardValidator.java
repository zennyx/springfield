package zenny.toybox.springfield.validation.validator;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.validation.Rule;
import zenny.toybox.springfield.validation.constraint.IdentityCard;

public class IdentityCardValidator implements ConstraintValidator<IdentityCard, CharSequence> {

  private static final Map<Locale, Rule<CharSequence>> RULES = new HashMap<>();

  private Locale locale;
  private boolean bypass;

  static {
    RULES.put(Locale.CHINA, new Rule<CharSequence>() {

      @SuppressWarnings("serial")
      private final Map<Integer, String> regions = new HashMap<Integer, String>() {
        {
          this.put(11, "北京");
          this.put(12, "天津");
          this.put(13, "河北");
          this.put(14, "山西");
          this.put(15, "内蒙");
          this.put(21, "辽宁");
          this.put(22, "吉林");
          this.put(23, "黑龙");
          this.put(31, "上海");
          this.put(32, "江苏");
          this.put(33, "浙江");
          this.put(34, "安徽");
          this.put(35, "福建");
          this.put(36, "江西");
          this.put(37, "山东");
          this.put(41, "河南");
          this.put(42, "湖北");
          this.put(43, "湖南");
          this.put(44, "广东");
          this.put(45, "广西");
          this.put(46, "海南");
          this.put(50, "重庆");
          this.put(51, "四川");
          this.put(52, "贵州");
          this.put(53, "云南");
          this.put(54, "西藏");
          this.put(61, "陕西");
          this.put(62, "甘肃");
          this.put(63, "青海");
          this.put(64, "宁夏");
          this.put(65, "新疆");
          this.put(71, "台湾");
          this.put(81, "香港");
          this.put(82, "澳门");
          this.put(91, "国外");
        }
      };

      private final Pattern mode15 = Pattern.compile("^(\\d){15}$");

      private final Pattern mode18 = Pattern.compile("^\\d{17}(\\d|x)$");

      private final Pattern mode18Replacement = Pattern.compile("x$");

      private final int[] mode18Calculations = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

      private final char[] mode18Suffixes = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

      @Override
      public boolean isValid(CharSequence value, ConstraintValidatorContext context, @Nullable Object... rest) {
        String target = value.toString();
        if (target.length() == 15) {
          target = this.upgrade(target);
        }

        target = target.toLowerCase();
        if (!this.mode18.matcher(target).matches()) {
          return false;
        }

        if (!this.regions.containsKey(Integer.parseInt(target.substring(0, 2)))) {
          return false;
        }

        String birthday = new StringBuilder(target.substring(6, 10)).append('-').append(target.substring(10, 12))
            .append('-').append(target.substring(12, 14)).toString();

        try {
          DateTimeFormatter.ISO_LOCAL_DATE.parse(birthday);
        } catch (DateTimeParseException e) {
          return false;
        }

        int calcValue = 0;
        target = this.mode18Replacement.matcher(target).replaceAll("a");
        for (int count = 17; count >= 0; count--) {
          calcValue += Math.pow(2, count) % 11 * Integer.parseInt(String.valueOf(target.charAt(17 - count)), 11);
        }

        if (calcValue % 11 != 1) {
          return false;
        }

        return true;
      }

      private String upgrade(String input) {
        if (this.mode15.matcher(input).matches()) {
          int calcValue = 0;
          StringBuilder output = new StringBuilder(input.substring(0, 6)).append("19")
              .append(input.substring(6, input.length()));

          for (int count = 0; count < input.length(); count++) {
            calcValue += Integer.parseInt(input.substring(count, count + 1)) * this.mode18Calculations[count];
          }
          output.append(this.mode18Suffixes[calcValue % 11]);

          return output.toString();
        }

        return "#";
      }
    });
  }

  public static void addRule(Locale locale, Rule<CharSequence> rule) {
    Assert.notNull(locale, "The given locale must not be null");
    Assert.notNull(rule, "The given rule must not be null");

    RULES.put(locale, rule);
  }

  @Override
  public void initialize(IdentityCard constraintAnnotation) {
    Assert.hasText(constraintAnnotation.countryOrRegion(), "The given region must not be empty");

    this.locale = Locale.forLanguageTag(constraintAnnotation.countryOrRegion());
    this.bypass = constraintAnnotation.bypassIfNoRuleAvailable();
  }

  @Override
  public boolean isValid(@Nullable CharSequence value, ConstraintValidatorContext context) {
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
