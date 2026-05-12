package zenny.toybox.springfield.util;

import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;

/**
 * Utility class for Chinese-language operations.
 *
 * <p>This class is not instantiable.
 */
public final class ChineseUtils {

  /** Suppresses default constructor, ensuring non-instantiability. */
  private ChineseUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Utility class for Chinese numeral conversion, supporting parsing of Chinese numerals (e.g.
   * "一万二千三百四十五") into {@link BigInteger} values and formatting {@link BigInteger} values into
   * Chinese numeral strings.
   *
   * <p>Supports digits (零~九), sub-myriad place values (十, 百, 千), and myriad-scale place values (万,
   * 亿, 兆, 京, 垓, 秭, 穰, 沟, 涧, 正, 载). Uppercase/variant forms (e.g. 壹, 贰, 拾, 佰, 仟, 〇) are
   * automatically normalized to their standard equivalents before parsing.
   *
   * <p>This class is not instantiable.
   */
  public static final class Numerals {

    /** Zero character. */
    public static final String ZERO = "零";

    /** Ten character. */
    public static final String TEN = "十";

    /** Ten character (variant). */
    public static final String TEN_VARIANT = "一十";

    /**
     * Chinese numeral digits mapping numeral characters (零~九) to their integer values. Excludes
     * special words: 半 (half), 两 (two-as-pair). The character 零 serves as both the digit zero and
     * the positional zero placeholder.
     */
    private static final BidiMap<Character, Integer> DIGIT_VALUES =
        new DualHashBidiMap<>(
            Map.ofEntries(
                Map.entry('零', 0),
                Map.entry('一', 1),
                Map.entry('二', 2),
                Map.entry('三', 3),
                Map.entry('四', 4),
                Map.entry('五', 5),
                Map.entry('六', 6),
                Map.entry('七', 7),
                Map.entry('八', 8),
                Map.entry('九', 9)));

    /**
     * Sub-myriad place values mapping Chinese place-value characters (十, 百, 千) to their
     * power-of-ten exponents. These represent positional multipliers within each myriad group
     * (0~9999). Excludes 万 (ten-thousand) which belongs to the myriad scale.
     *
     * <p>For example, 十→1 (10¹), 百→2 (10²), 千→3 (10³).
     */
    private static final BidiMap<Character, Integer> SUB_MYRIAD_PLACE_VALUES =
        new DualHashBidiMap<>(
            Map.ofEntries(Map.entry('十', 1), Map.entry('百', 2), Map.entry('千', 3)));

    /**
     * Myriad-scale place values mapping Chinese large-number characters (万, 亿, 兆, 京, 垓, 秭, 穰, 沟, 涧,
     * 正, 载) to their power-of-ten exponents.
     *
     * <p>The scale follows the traditional Chinese myriad system where each unit represents 10^4
     * times the previous: 万(10⁴), 亿(10⁸), 兆(10¹²), etc.
     */
    private static final BidiMap<Character, Integer> MYRIAD_SCALE_VALUES =
        new DualHashBidiMap<>(
            Map.ofEntries(
                Map.entry('万', 4),
                Map.entry('亿', 8),
                Map.entry('兆', 12),
                Map.entry('京', 16),
                Map.entry('垓', 20),
                Map.entry('秭', 24),
                Map.entry('穰', 28),
                Map.entry('沟', 32),
                Map.entry('涧', 36),
                Map.entry('正', 40),
                Map.entry('载', 44)));

    /**
     * Bidirectional mapping between uppercase (financial) and lowercase (standard) Chinese numeral
     * characters. The forward direction maps uppercase to lowercase; the inverse maps lowercase to
     * uppercase.
     *
     * <p>Covers digits (零~九) and place values (十, 百, 千, 万, 亿):
     *
     * <ul>
     *   <li>Uppercase digits: 壹↔一, 贰↔二, 叁↔三, 肆↔四, 伍↔五, 陆↔六, 柒↔七, 捌↔八, 玖↔九
     *   <li>Uppercase place values: 拾↔十, 佰↔百, 仟←千
     * </ul>
     *
     * <p>Note: 零 has no uppercase equivalent in standard financial notation (〇 is a variant, not an
     * uppercase form). 万 and 亿 have traditional forms (萬, 億) which are handled separately in {@link
     * #VARIANT_DIGIT_CHARS}.
     */
    private static final BidiMap<Character, Character> UPPERCASE_CHARS =
        new DualHashBidiMap<>(
            Map.ofEntries(
                Map.entry('壹', '一'),
                Map.entry('贰', '二'),
                Map.entry('叁', '三'),
                Map.entry('肆', '四'),
                Map.entry('伍', '五'),
                Map.entry('陆', '六'),
                Map.entry('柒', '七'),
                Map.entry('捌', '八'),
                Map.entry('玖', '九'),
                Map.entry('拾', '十'),
                Map.entry('佰', '百'),
                Map.entry('仟', '千')));

    /**
     * Mapping from variant Chinese numeral characters to their standard equivalents. These are
     * character substitutions that do not have a bidirectional uppercase/lowercase relationship.
     *
     * <p>Mappings include:
     *
     * <ul>
     *   <li>〇 → 零 (circle zero to standard zero)
     *   <li>萬 → 万 (traditional myriad to simplified)
     *   <li>億 → 亿 (traditional hundred-million to simplified)
     * </ul>
     */
    private static final Map<Character, Character> VARIANT_DIGIT_CHARS =
        Map.ofEntries(Map.entry('〇', '零'), Map.entry('萬', '万'), Map.entry('億', '亿'));

    /**
     * Pre-compiled pattern matching a zero digit (零) followed by any place-value character. Used
     * during formatting to remove spurious zeros before place-value markers.
     *
     * <p>For example, "零千" → "零", "零百" → "零".
     */
    public static final Pattern ZERO_PLACE_VALUE_PATTERN =
        Pattern.compile(
            ZERO
                + "["
                + SUB_MYRIAD_PLACE_VALUES.keySet().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining())
                + MYRIAD_SCALE_VALUES.keySet().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining())
                + "]");

    /**
     * Pre-compiled pattern matching consecutive zero digits (零), used during formatting to merge
     * multiple zeros into a single zero.
     *
     * <p>For example, "零零" → "零".
     */
    private static final Pattern CONSECUTIVE_ZEROS_PATTERN = Pattern.compile(ZERO + "+");

    /** Suppresses default constructor, ensuring non-instantiability. */
    private Numerals() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a Chinese numeral string to uppercase (financial) form.
     *
     * <p>Each lowercase character that has an uppercase equivalent in {@link #UPPERCASE_CHARS} is
     * replaced; other characters pass through unchanged. For example, "一万二千三百四十五" is converted to
     * "壹万贰千叁佰肆拾伍".
     *
     * @param numerals the Chinese numeral string in lowercase/standard form
     * @return the uppercase form of the numeral string
     */
    @Contract("null -> null; !null -> !null")
    public static @Nullable String toUppercase(@Nullable String numerals) {
      if (numerals == null) {
        return null;
      }

      StringBuilder sb = new StringBuilder(numerals.length());
      for (int i = 0; i < numerals.length(); i++) {
        char c = numerals.charAt(i);
        Character upper = UPPERCASE_CHARS.inverseBidiMap().get(c);
        sb.append(upper != null ? upper : c);
      }
      return sb.toString();
    }

    /**
     * Converts a Chinese numeral string to lowercase (standard) form.
     *
     * <p>Each uppercase character in {@link #UPPERCASE_CHARS} is replaced with its lowercase
     * equivalent; other characters pass through unchanged. For example, "壹万贰千叁佰肆拾伍" is converted to
     * "一万二千三百四十五".
     *
     * <p>Note: variant characters (〇, 萬, 億) are not handled by this method; use {@link #normalize}
     * for full normalization.
     *
     * @param numerals the Chinese numeral string possibly containing uppercase characters
     * @return the lowercase form of the numeral string
     */
    @Contract("null -> null; !null -> !null")
    public static @Nullable String toLowercase(@Nullable String numerals) {
      if (numerals == null) {
        return null;
      }

      StringBuilder sb = new StringBuilder(numerals.length());
      for (int i = 0; i < numerals.length(); i++) {
        char c = numerals.charAt(i);
        Character lower = UPPERCASE_CHARS.get(c);
        sb.append(lower != null ? lower : c);
      }
      return sb.toString();
    }

    /**
     * Parses a Chinese numeral string into its {@link BigInteger} representation.
     *
     * <p>This method parses Chinese numerals that combine digits, sub-myriad place values, and
     * myriad-scale place values. For example, "一万二千三百四十五" is parsed as 12345. Variant and uppercase
     * forms are automatically normalized before parsing (see {@link #normalize}).
     *
     * <p><b>Algorithm: Three-Level Accumulation</b>
     *
     * <p>The algorithm processes characters left-to-right, accumulating values across three
     * hierarchical levels that mirror the structure of Chinese numerals:
     *
     * <ol>
     *   <li><b>coefficient</b> — the current digit value (0~9), awaiting multiplication by a
     *       subsequent place-value character. Defaults to 1 to support implicit coefficients: "十"
     *       means "一十" (10), "万亿" means "一万亿" (10¹²). When a zero digit (零) is encountered,
     *       coefficient is set to 0, which signals an implicit "one" for the next place value
     *       (since "零百" is not a valid construct in practice, the zero-to-one fallback handles edge
     *       cases gracefully).
     *   <li><b>groupSum</b> — the accumulated sum within the current sub-myriad group (十, 百, 千).
     *       Each sub-myriad place value multiplies the current coefficient (or 1 if coefficient is
     *       zero) and adds the product to groupSum. For example, "二千三百" produces groupSum = 2×1000
     *       + 3×100 = 2300.
     *   <li><b>total</b> — the accumulated sum at the myriad level. When a myriad-scale character
     *       is encountered, groupSum (or 1 if groupSum is zero, for implicit coefficients like
     *       "万亿") is multiplied by the myriad scale value and added to total.
     * </ol>
     *
     * <p><b>Nested Myriad Scales</b>
     *
     * <p>When a new myriad scale exceeds the previous one (tracked via {@code lastMyriadScale}),
     * the existing total is combined with groupSum and then <em>multiplied</em> by the new scale,
     * rather than simply adding the product. This correctly handles constructs like "一万亿" where
     * 亿(10⁸) > 万(10⁴): total = (10000 + 0) × 10⁸ = 10¹².
     *
     * <p><b>Processing Rules by Character Type</b>
     *
     * <ul>
     *   <li><b>Digit (零~九)</b>: store as coefficient for the next place-value character.
     *   <li><b>Sub-myriad place value (十, 百, 千)</b>: multiply coefficient (or 1 if zero) by the
     *       place value and add to groupSum; reset coefficient to zero.
     *   <li><b>Myriad-scale place value (万, 亿, ...)</b>: add coefficient to groupSum, then multiply
     *       groupSum (or 1 if zero) by the scale value and add to total. If this scale exceeds the
     *       previous one, multiply (total + groupSum) by the scale instead. Reset groupSum and
     *       coefficient; update lastMyriadScale.
     * </ul>
     *
     * <p>After processing all characters, the result is {@code total + groupSum + coefficient},
     * combining any remaining values at all three levels.
     *
     * <p><b>Example Traces</b>
     *
     * <p>"一万二千三百四十五" → 12345:
     *
     * <pre>
     *   一  → coefficient=1
     *   万  → groupSum=0+1=1, total=0+1×10⁴=10000, reset, lastMyriadScale=10⁴
     *   二  → coefficient=2
     *   千  → groupSum=0+2×1000=2000, coefficient=0
     *   三  → coefficient=3
     *   百  → groupSum=2000+3×100=2300, coefficient=0
     *   四  → coefficient=4
     *   十  → groupSum=2300+4×10=2340, coefficient=0
     *   五  → coefficient=5
     *   Result: 10000 + 2340 + 5 = 12345
     * </pre>
     *
     * <p>"一万亿" → 10¹²:
     *
     * <pre>
     *   一  → coefficient=1
     *   万  → groupSum=0+1=1, total=0+1×10⁴=10000, reset, lastMyriadScale=10⁴
     *   亿  → groupSum=0+0=0, 10⁸ > lastMyriadScale=10⁴ → total=(10000+0)×10⁸=10¹²
     *   Result: 10¹² + 0 + 0 = 10¹²
     * </pre>
     *
     * @param numerals the Chinese numeral string to parse (e.g. "一万二千三百四十五"); uppercase and variant
     *     forms are accepted (e.g. "壹万贰千叁佰肆拾伍")
     * @return the parsed numeric value as a {@link BigInteger}
     * @throws IllegalArgumentException if the input is blank or contains invalid characters
     * @see #format(BigInteger)
     * @see <a href="https://en.wikipedia.org/wiki/Chinese_numerals">Chinese Numerals</a>
     */
    public static BigInteger parse(String numerals) {
      Assert.hasText(numerals, "'numerals' must not be empty.");

      // Normalize variant/uppercase characters to standard forms before parsing.
      // For example, "壹万贰千叁佰肆拾伍" → "一万二千三百四十五", "〇" → "零".
      numerals = normalize(numerals);

      // --- Three-level accumulation state ---
      // coefficient: the current digit value (0~9), waiting to be multiplied by the next
      // place-value character. Defaults to 1 so that implicit coefficients work:
      // "十" is interpreted as "一十" (10), "万亿" as "一万亿" (10¹²).
      BigInteger coefficient = BigInteger.ONE;
      // groupSum: the accumulated sum within the current sub-myriad group (十/百/千 level).
      // For example, "二千三百" yields groupSum = 2×1000 + 3×100 = 2300.
      BigInteger groupSum = BigInteger.ZERO;
      // lastMyriadScale: the value of the most recent myriad-scale character encountered.
      // Used to detect nested scales (e.g. "一万亿" where 亿 > 万), which require
      // multiplication rather than addition.
      BigInteger lastMyriadScale = BigInteger.ZERO;
      // total: the accumulated sum at the myriad level (万/亿/兆/...).
      // Each time a myriad-scale character is encountered, groupSum × scale is added here.
      BigInteger total = BigInteger.ZERO;

      for (int i = 0; i < numerals.length(); i++) {
        char numeral = numerals.charAt(i);

        // --- Case 1: Digit character (零~九) ---
        // Store the digit value as the current coefficient. It will be consumed by the next
        // sub-myriad or myriad-scale character. For example, in "二千", '二' sets coefficient=2,
        // then '千' consumes it as 2×1000.
        if (DIGIT_VALUES.containsKey(numeral)) {
          coefficient = BigInteger.valueOf(DIGIT_VALUES.get(numeral));
          continue;
        }

        // --- Case 2: Sub-myriad place value (十, 百, 千) ---
        // Multiply the current coefficient by this place value and add to groupSum.
        // If coefficient is zero (from '零'), treat it as 1 for implicit coefficients:
        // "十" alone means 10 (implicit 一十), so a zero coefficient before 十 should yield 1×10.
        // After consuming, reset coefficient to zero to avoid double-counting.
        if (SUB_MYRIAD_PLACE_VALUES.containsKey(numeral)) {
          groupSum =
              groupSum.add(
                  (coefficient.equals(BigInteger.ZERO) ? BigInteger.ONE : coefficient)
                      .multiply(BigInteger.TEN.pow(SUB_MYRIAD_PLACE_VALUES.get(numeral))));
          coefficient = BigInteger.ZERO;
          continue;
        }

        // --- Case 3: Myriad-scale place value (万, 亿, 兆, ...) ---
        // This is the most complex case. Two sub-cases:
        //
        // (a) Normal case (same or smaller scale than previous):
        //     Add coefficient to groupSum first (e.g. "三万" → groupSum was 0, now 0+3=3),
        //     then multiply groupSum by the scale value and add to total.
        //     If groupSum is zero, use 1 as the implicit coefficient (e.g. "万亿" → 1×10⁴×10⁸).
        //
        // (b) Nested case (larger scale than previous):
        //     The current scale exceeds lastMyriadScale, meaning the previous total should be
        //     multiplied by this scale, not simply added. For example, "一万亿":
        //       - After '万': total = 1×10⁴ = 10000, lastMyriadScale = 10⁴
        //       - At '亿': 10⁸ > 10⁴, so total = (10000 + 0) × 10⁸ = 10¹²
        //     This correctly represents "一万亿" = 10000 × 10⁸ = 10¹².
        if (MYRIAD_SCALE_VALUES.containsKey(numeral)) {
          // Fold the current coefficient into groupSum before processing the scale.
          // For example, "三万" has coefficient=3 at this point; groupSum becomes 0+3=3.
          groupSum = groupSum.add(coefficient);
          BigInteger myriadValue = BigInteger.TEN.pow(MYRIAD_SCALE_VALUES.get(numeral));

          // Nested scale: the previous total is a sub-multiple of this larger scale.
          // Combine total with groupSum, then multiply by the new scale.
          // Example: "一万亿" → after 万, total=10000, lastMyriadScale=10⁴;
          // at 亿 (10⁸ > 10⁴), total = (10000+0)×10⁸ = 10¹².
          if (lastMyriadScale.compareTo(BigInteger.ZERO) > 0
              && myriadValue.compareTo(lastMyriadScale) > 0) {
            total = total.add(groupSum).multiply(myriadValue);

            // Normal scale: groupSum × scale is added to total as an independent term.
            // If groupSum is zero, use 1 as the implicit coefficient.
            // Example: "三亿五万" → after 亿, total=3×10⁸, lastMyriadScale=10⁸;
            // at 万 (10⁴ < 10⁸), total = 3×10⁸ + 5×10⁴.
          } else {
            total =
                total.add(
                    (groupSum.equals(BigInteger.ZERO) ? BigInteger.ONE : groupSum)
                        .multiply(myriadValue));
          }

          // Update tracking state for the next iteration.
          lastMyriadScale = myriadValue;
          groupSum = BigInteger.ZERO;
          coefficient = BigInteger.ZERO;
          continue;
        }

        // If the character doesn't match any known category, the input is invalid.
        throw new IllegalArgumentException("'numerals' format must be valid.");
      }

      // After the loop, combine all three levels into the final result.
      // - total: fully accumulated myriad-level sum
      // - groupSum: any remaining sub-myriad sum (e.g. "三百" at the end without a following
      //   myriad-scale character)
      // - coefficient: any trailing digit without a place value (e.g. "五" at the end)
      return total.add(groupSum).add(coefficient);
    }

    /**
     * Formats a {@link BigInteger} value as a Chinese numeral string, with normalization enabled.
     *
     * <p>Equivalent to {@code format(numerals, true)}.
     *
     * @param numerals the non-negative numeric value to format
     * @return the Chinese numeral string representation
     * @throws IllegalArgumentException if the value is null or negative
     * @see #format(BigInteger, boolean)
     * @see #parse(String)
     */
    public static String format(BigInteger numerals) {
      return format(numerals, true);
    }

    /**
     * Formats a {@link BigInteger} value as a Chinese numeral string.
     *
     * <p>This method is the inverse of {@link #parse(String)}, converting a numeric value into its
     * Chinese numeral representation. For example, 12345 is formatted as "一万二千三百四十五".
     *
     * <p><b>Algorithm: Digit-First Construction with Post-Hoc Cleanup</b>
     *
     * <p>The algorithm works in three passes:
     *
     * <ol>
     *   <li><b>Digit replacement</b>: Convert the decimal string digit-by-digit into Chinese
     *       numeral characters (零~九).
     *   <li><b>Place-value insertion</b>: For each position (from least significant to most),
     *       append the appropriate place-value suffix:
     *       <ul>
     *         <li>Sub-myriad positions: 十 (×10), 百 (×100), 千 (×1000), cycling every 4 digits.
     *         <li>Myriad-scale markers: 万 (at position 4), 亿 (at position 8), 兆 (at position 12),
     *             etc., appended after the sub-myriad suffix when both apply.
     *       </ul>
     *   <li><b>Zero cleanup</b>: Three rules applied in order:
     *       <ul>
     *         <li>Replace "零 + place-value" with "零" (e.g. "零千" → "零").
     *         <li>Merge consecutive "零" into a single "零".
     *         <li>Remove trailing "零".
     *       </ul>
     * </ol>
     *
     * <p><b>Special case: "一十" → "十"</b>
     *
     * <p>When the number starts with "一十" (e.g. 15 → "一十五") and {@code normalize} is {@code true},
     * the leading "一" is conventionally omitted, producing "十五" instead. If {@code normalize} is
     * {@code false}, the "一" is preserved.
     *
     * <p><b>Example Trace</b>
     *
     * <p>12345 → "一万二千三百四十五":
     *
     * <pre>
     *   Pass 1: "一二三四五"
     *   Pass 2: "一万二千三百四十*五"  (* = no suffix for ones position)
     *   Pass 3: no zeros to clean → "一万二千三百四十五"
     * </pre>
     *
     * <p>10005 → "一万零五":
     *
     * <pre>
     *   Pass 1: "一零零零五"
     *   Pass 2: "一万零千零百零十五"
     *   Pass 3: "零千"→"零", "零百"→"零", "零十"→"零" → "一万零零零五"
     *          merge consecutive zeros → "一万零五"
     * </pre>
     *
     * @param numerals the non-negative numeric value to format
     * @param normalize if {@code true}, omit the leading "一" in "一十" (e.g. 15 → "十五"); if {@code
     *     false}, preserve it (e.g. 15 → "一十五")
     * @return the Chinese numeral string representation
     * @throws IllegalArgumentException if the value is null or negative
     * @see #parse(String)
     */
    public static String format(BigInteger numerals, boolean normalize) {
      Assert.notNull(numerals, "'numerals' must not be null.");
      Assert.isTrue(numerals.compareTo(BigInteger.ZERO) >= 0, "'numerals' must be non-negative.");

      if (numerals.equals(BigInteger.ZERO)) {
        return ZERO;
      }

      String digits = numerals.toString();
      int len = digits.length();

      // --- Pass 1 & 2: Replace digits and insert place-value suffixes ---
      // Process from most significant to least significant.
      // Position index: 0 = ones, 1 = tens, 2 = hundreds, 3 = thousands, 4 = ten-thousands, ...
      StringBuilder raw = new StringBuilder();
      for (int i = 0; i < len; i++) {
        int digit = digits.charAt(i) - '0';
        int posFromEnd = len - 1 - i;

        // Append the Chinese digit character.
        raw.append(DIGIT_VALUES.inverseBidiMap().get(digit));

        // Append place-value suffix if not the ones position.
        if (posFromEnd > 0) {
          int subMyriadPos = posFromEnd % 4;
          Character subMyriadChar = SUB_MYRIAD_PLACE_VALUES.inverseBidiMap().get(subMyriadPos);
          if (subMyriadChar != null) {
            raw.append(subMyriadChar);
          }
          // Myriad-scale marker: 万 at pos 4, 亿 at pos 8, 兆 at pos 12, etc.
          if (posFromEnd % 4 == 0) {
            Character myriadChar = MYRIAD_SCALE_VALUES.inverseBidiMap().get(posFromEnd);
            if (myriadChar != null) {
              raw.append(myriadChar);
            }
          }
        }
      }

      // --- Pass 3: Zero cleanup ---
      // Rule 1: Replace "零" followed by any place-value character with "零".
      //         Place-value characters are: 十百千万亿兆京垓秭穰沟涧正载.
      // Rule 2: Merge consecutive "零" into a single "零".
      String result = raw.toString();
      result = ZERO_PLACE_VALUE_PATTERN.matcher(result).replaceAll(ZERO);
      result = CONSECUTIVE_ZEROS_PATTERN.matcher(result).replaceAll(ZERO);

      // Rule 3: Remove trailing "零".
      if (result.endsWith(ZERO)) {
        result = result.substring(0, result.length() - 1);
      }

      // Special case: "一十" at the beginning → "十".
      if (normalize && result.startsWith(TEN_VARIANT)) {
        result = result.substring(1);
      }

      return result;
    }

    /**
     * Normalizes a Chinese numeral string by replacing uppercase and variant characters with their
     * standard equivalents.
     *
     * <p>This preprocessing step first converts all uppercase characters to lowercase via {@link
     * #toLowercase}, then replaces variant characters (〇→零, 萬→万, 億→亿) using {@link
     * #VARIANT_DIGIT_CHARS}. This allows the parser to handle diverse input forms uniformly.
     *
     * <p>For example, "壹万贰千叁佰肆拾伍" is normalized to "一万二千三百四十五", and "〇" is normalized to "零".
     *
     * @param numerals the raw Chinese numeral string possibly containing variant characters
     * @return the normalized string with all variant characters replaced by their standard forms
     */
    private static String normalize(String numerals) {
      String lowered = toLowercase(numerals);
      StringBuilder sb = new StringBuilder(lowered.length());
      for (int i = 0; i < lowered.length(); i++) {
        char c = lowered.charAt(i);
        sb.append(VARIANT_DIGIT_CHARS.getOrDefault(c, c));
      }
      return sb.toString();
    }
  }
}
