package zenny.toybox.springfield.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.tabletest.junit.TableTest;

/**
 * Tests for {@link ChineseUtils}, covering the {@link ChineseUtils.Numerals#toUppercase(String)},
 * {@link ChineseUtils.Numerals#toLowercase(String)}, {@link ChineseUtils.Numerals#parse(String)},
 * and {@link ChineseUtils.Numerals#format(BigInteger)} methods with full branch coverage.
 *
 * <p>Branch coverage for {@link ChineseUtils.Numerals#toUppercase}:
 *
 * <ul>
 *   <li>Null input → returns {@code null}
 *   <li>Character with uppercase equivalent in {@code UPPERCASE_CHARS} → replaced (e.g. 一→壹, 十→拾)
 *   <li>Character without uppercase equivalent → passed through unchanged (e.g. 万, 亿, 零)
 * </ul>
 *
 * <p>Branch coverage for {@link ChineseUtils.Numerals#toLowercase}:
 *
 * <ul>
 *   <li>Null input → returns {@code null}
 *   <li>Uppercase character in {@code UPPERCASE_CHARS} → replaced with lowercase equivalent (e.g.
 *       壹→一, 拾→十)
 *   <li>Non-uppercase character → passed through unchanged (e.g. 万, 亿, 零)
 * </ul>
 *
 * <p>Branch coverage for {@link ChineseUtils.Numerals#parse}:
 *
 * <ul>
 *   <li>Null/blank input → {@link IllegalArgumentException}
 *   <li>Invalid character → {@link IllegalArgumentException}
 *   <li>Digit (零~九), sub-myriad (十, 百, 千) with zero/non-zero coefficient, myriad-scale (万, 亿, ...)
 *       with normal/nested scales and zero/non-zero groupSum
 * </ul>
 *
 * <p>Branch coverage for {@link ChineseUtils.Numerals#format}:
 *
 * <ul>
 *   <li>Null/negative input → {@link IllegalArgumentException}
 *   <li>Zero → "零" (early exit)
 *   <li>posFromEnd == 0 (ones position) vs > 0 (sub-myriad/myriad positions)
 *   <li>subMyriadChar != null (pos%4 is 1,2,3) vs == null (pos%4 == 0)
 *   <li>myriadChar != null (known position ≤ 44) vs == null (beyond 载)
 *   <li>Trailing zero removal; zero cleanup rules (ZERO_PLACE_VALUE_PATTERN,
 *       CONSECUTIVE_ZEROS_PATTERN)
 *   <li>normalize=true: "一十" → "十"; normalize=false: "一十" preserved
 * </ul>
 */
@DisplayName("ChineseUtils")
class ChineseUtilsTest {

  /**
   * Tests for {@link ChineseUtils.Numerals}, covering the {@link
   * ChineseUtils.Numerals#toUppercase(String)}, {@link ChineseUtils.Numerals#toLowercase(String)},
   * {@link ChineseUtils.Numerals#parse(String)}, and {@link
   * ChineseUtils.Numerals#format(BigInteger)} methods with full branch coverage.
   *
   * <p>For {@link ChineseUtils.Numerals#toUppercase}: each lowercase character with an uppercase
   * equivalent in {@code UPPERCASE_CHARS} is replaced; other characters pass through unchanged.
   *
   * <p>For {@link ChineseUtils.Numerals#toLowercase}: each uppercase character in {@code
   * UPPERCASE_CHARS} is replaced with its lowercase equivalent; other characters pass through
   * unchanged.
   *
   * <p>For {@link ChineseUtils.Numerals#parse}: Chinese numeral strings are parsed into {@link
   * BigInteger} values via three-level accumulation; invalid inputs are rejected.
   *
   * <p>For {@link ChineseUtils.Numerals#format}: {@link BigInteger} values are formatted as Chinese
   * numeral strings via digit-first construction with post-hoc zero cleanup; the {@code normalize}
   * parameter controls whether "一十" is simplified to "十".
   */
  @Nested
  @DisplayName("Numerals")
  class NumeralsTest {

    /**
     * Exercises all branches of {@link ChineseUtils.Numerals#toUppercase(String)}:
     *
     * <ul>
     *   <li>{@code null} input → {@code null} return (early-exit branch)
     *   <li>Empty string → empty string (loop body not entered)
     *   <li>Lowercase digit with uppercase equivalent (一→壹, ..., 九→玖; ternary true-branch)
     *   <li>Lowercase place value with uppercase equivalent (十→拾, 百→佰, 千→仟; ternary true-branch)
     *   <li>Character without uppercase equivalent (万, 亿, 零 → passed through; ternary false-branch)
     *   <li>Mixed input combining both branches of the ternary within a single string
     * </ul>
     *
     * @param input the Chinese numeral string to convert; may be {@code null}
     * @param expected the expected uppercase result; may be {@code null}
     */
    @TableTest(
        """
      Scenario                    | Input              | Expected
      Null input                  |                    |
      Empty string                | ""                 | ""
      Single lowercase digit      | 一                 | 壹
      All lowercase digits        | 一二三四五六七八九 | 壹贰叁肆伍陆柒捌玖
      Lowercase place values      | 十百千             | 拾佰仟
      Mixed with no-upper chars   | 一万二千三百四十五 | 壹万贰仟叁佰肆拾伍
      No uppercase equivalent     | 万亿零             | 万亿零
      Uppercase input passthrough | 壹                 | 壹
      """)
    @DisplayName("toUppercase.succeeds")
    void toUppercaseSucceeds(String input, String expected) {
      // toUppercase maps lowercase chars to their uppercase equivalents via UPPERCASE_CHARS;
      // chars without uppercase mappings (万, 亿, 零) pass through unchanged.
      assertEquals(expected, ChineseUtils.Numerals.toUppercase(input));
    }

    /**
     * Exercises all branches of {@link ChineseUtils.Numerals#toLowercase(String)}:
     *
     * <ul>
     *   <li>{@code null} input → {@code null} return (early-exit branch)
     *   <li>Empty string → empty string (loop body not entered)
     *   <li>Uppercase digit with lowercase equivalent (壹→一, ..., 玖→九; ternary true-branch)
     *   <li>Uppercase place value with lowercase equivalent (拾→十, 佰→百, 仟→千; ternary true-branch)
     *   <li>Character without lowercase equivalent (万, 亿, 零 → passed through; ternary false-branch)
     *   <li>Mixed input combining both branches of the ternary within a single string
     * </ul>
     *
     * @param input the Chinese numeral string to convert; may be {@code null}
     * @param expected the expected lowercase result; may be {@code null}
     */
    @TableTest(
        """
      Scenario                    | Input              | Expected
      Null input                  |                    |
      Empty string                | ""                 | ""
      Single uppercase digit      | 壹                 | 一
      All uppercase digits        | 壹贰叁肆伍陆柒捌玖 | 一二三四五六七八九
      Uppercase place values      | 拾佰仟             | 十百千
      Mixed with no-lower chars   | 壹万贰仟叁佰肆拾伍 | 一万二千三百四十五
      No lowercase equivalent     | 万亿零             | 万亿零
      Lowercase input passthrough | 一                 | 一
      """)
    @DisplayName("toLowercase.succeeds")
    void toLowercaseSucceeds(String input, String expected) {
      // toLowercase maps uppercase chars to their lowercase equivalents via UPPERCASE_CHARS;
      // chars without lowercase mappings (万, 亿, 零) pass through unchanged.
      assertEquals(expected, ChineseUtils.Numerals.toLowercase(input));
    }

    /**
     * Exercises all branches of {@link ChineseUtils.Numerals#parse(String)}:
     *
     * <p><b>Branch structure within {@code parse}:</b>
     *
     * <ul>
     *   <li>Blank input → {@link IllegalArgumentException} (Assert.hasText guard)
     *   <li>Invalid character → {@link IllegalArgumentException} (fall-through at end of loop)
     *   <li>Case 1 — Digit (零~九): sets coefficient to digit value
     *   <li>Case 2 — Sub-myriad (十, 百, 千): two sub-branches based on coefficient:
     *       <ul>
     *         <li>coefficient == 0 (from 零 or implicit) → use 1 as multiplier (e.g. "十" → 10)
     *         <li>coefficient != 0 → use coefficient directly (e.g. "二十" → 20)
     *       </ul>
     *   <li>Case 3 — Myriad-scale (万, 亿, ...): two main sub-branches:
     *       <ul>
     *         <li>Nested: lastMyriadScale > 0 && myriadValue > lastMyriadScale → total = (total +
     *             groupSum) × myriadValue (e.g. "一万亿" → 10¹²)
     *         <li>Normal: else → two further sub-branches:
     *             <ul>
     *               <li>groupSum == 0 → use 1 as implicit coefficient (e.g. "万亿" → 10¹²)
     *               <li>groupSum != 0 → use groupSum directly (e.g. "三万" → 30000)
     *             </ul>
     *       </ul>
     *   <li>Final combination: total + groupSum + coefficient (various remaining-state scenarios)
     * </ul>
     *
     * <p>Each table row is annotated with the specific branch(es) it exercises.
     *
     * @param input the Chinese numeral string to parse
     * @param expected the expected {@link BigInteger} result
     */
    @TableTest(
        """
      Scenario                             | Input              | Expected
      // Case 1: Digit only — coefficient remains, returned via final combination
      Single digit                         | 五                 | 5
      Zero digit                           | 零                 | 0
      // Case 2: Sub-myriad with non-zero coefficient (ternary false-branch)
      Sub-myriad with explicit coefficient | 二十               | 20
      Full sub-myriad group                | 二千三百四十五     | 2345
      // Case 2: Sub-myriad with zero coefficient (ternary true-branch, implicit 一)
      Sub-myriad with implicit coefficient | 十                 | 10
      // Case 3: Myriad-scale, normal, groupSum != 0 (ternary false-branch)
      Myriad with explicit coefficient     | 三万               | 30000
      Standard mixed numeral               | 一万二千三百四十五 | 12345
      // Case 3: Myriad-scale, normal, groupSum == 0 (ternary true-branch, implicit 1)
      Myriad with implicit coefficient     | 万                 | 10000
      // Case 3: Myriad-scale, nested (lastMyriadScale > 0 && myriadValue > lastMyriadScale)
      Nested myriad scales                 | 一万亿             | 1000000000000
      // Descending myriad scales (normal, not nested)
      Descending myriad scales             | 三亿五万           | 300050000
      // Uppercase input (normalized via toLowercase before parsing)
      Uppercase input                      | 壹万贰千叁佰肆拾伍 | 12345
      // Variant characters (〇→零, 萬→万, 億→亿, normalized before parsing)
      Variant circle zero                  | 〇                 | 0
      Variant traditional myriad           | 一萬               | 10000
      Variant traditional hundred-million  | 一億               | 100000000
      // Final combination: trailing coefficient without place value
      Trailing digit after myriad          | 一万五             | 10005
      // Final combination: trailing groupSum without myriad-scale
      Trailing sub-myriad after myriad     | 一万三百           | 10300
      """)
    @DisplayName("parse.succeeds")
    void parseSucceeds(String input, BigInteger expected) {
      // parse normalizes input, then walks characters left-to-right accumulating across
      // three levels (coefficient, groupSum, total). Each row targets a specific branch.
      assertEquals(expected, ChineseUtils.Numerals.parse(input));
    }

    /**
     * Verifies that {@link ChineseUtils.Numerals#parse(String)} throws {@link
     * IllegalArgumentException} for invalid inputs.
     *
     * <p>Covers three exception branches:
     *
     * <ul>
     *   <li>Null input → {@code Assert.hasText} guard
     *   <li>Blank input → {@code Assert.hasText} guard
     *   <li>Invalid character → fall-through at the end of the character-dispatch loop
     * </ul>
     *
     * @param input the invalid Chinese numeral string; may be {@code null}
     */
    @TableTest(
        """
      Scenario           | Input
      Null input         |
      Blank input        | ""
      Invalid characters | abc
      """)
    @DisplayName("parse.fails")
    void parseFails(String input) {
      assertThrows(IllegalArgumentException.class, () -> ChineseUtils.Numerals.parse(input));
    }

    /**
     * Exercises all branches of {@link ChineseUtils.Numerals#format(BigInteger)} (which delegates
     * to {@code format(numerals, true)}) with diverse number patterns.
     *
     * <p><b>Branch structure within {@code format}:</b>
     *
     * <ul>
     *   <li>Zero → "零" (early exit)
     *   <li>posFromEnd == 0 (ones position) → no suffix appended (e.g. 5 → "五")
     *   <li>posFromEnd > 0 && subMyriadChar != null → append sub-myriad suffix (e.g. 10 → "十", 100
     *       → "一百", 1000 → "一千")
     *   <li>posFromEnd > 0 && subMyriadChar == null (pos%4 == 0) → skip sub-myriad (e.g. 10000 →
     *       "一万")
     *   <li>posFromEnd % 4 == 0 && myriadChar != null → append myriad marker (e.g. 10000 → "一万",
     *       100000000 → "一亿", 1000000000000 → "一兆")
     *   <li>Zero cleanup: ZERO_PLACE_VALUE_PATTERN ("零" + place-value → "零"),
     *       CONSECUTIVE_ZEROS_PATTERN (merge consecutive "零"), trailing "零" removal
     *   <li>normalize=true && startsWith("一十") → remove leading "一" (e.g. 10 → "十")
     *   <li>normalize=true && !startsWith("一十") → no change (e.g. 20 → "二十")
     * </ul>
     *
     * <p>Each table row is annotated with the specific branch(es) it exercises.
     *
     * @param input the numeric value to format
     * @param expected the expected Chinese numeral string
     */
    @TableTest(
        """
      Scenario               | Input         | Expected
      // Zero early exit
      Zero                   | 0             | 零
      // posFromEnd == 0: single digit, no suffix
      Single digit           | 5             | 五
      // subMyriad (十) + trailing zero removal + normalize "一十" → "十"
      Ten                    | 10            | 十
      // subMyriad (十) + trailing zero removal, no "一十" prefix
      Twenty                 | 20            | 二十
      // subMyriad (百) + zero cleanup (ZERO_PLACE_VALUE_PATTERN + CONSECUTIVE_ZEROS_PATTERN)
      Hundred                | 100           | 一百
      // subMyriad (千) + zero cleanup
      Thousand               | 1000          | 一千
      // subMyriadChar == null (pos%4==0) + myriadChar != null (万)
      Ten thousand           | 10000         | 一万
      // Full mixed numeral, no zero cleanup needed
      Standard mixed numeral | 12345         | 一万二千三百四十五
      // Zero cleanup: internal zeros with ZERO_PLACE_VALUE_PATTERN + CONSECUTIVE_ZEROS_PATTERN
      Internal zeros         | 10005         | 一万零五
      // myriadChar != null (亿)
      Hundred million        | 100000000     | 一亿
      // myriadChar != null (兆)
      Trillion               | 1000000000000 | 一兆
      """)
    @DisplayName("format.succeeds")
    void formatSucceeds(BigInteger input, String expected) {
      // format(BigInteger) delegates to format(numerals, true), exercising all formatting
      // branches except normalize=false and myriadChar==null.
      assertEquals(expected, ChineseUtils.Numerals.format(input));
    }

    /**
     * Exercises the {@code normalize} parameter branches of {@link
     * ChineseUtils.Numerals#format(BigInteger, boolean)}.
     *
     * <p>Covers two sub-branches of the normalize logic:
     *
     * <ul>
     *   <li>{@code normalize=true && startsWith("一十")} → remove leading "一" (e.g. 10 → "十", 15 →
     *       "十五")
     *   <li>{@code normalize=false && startsWith("一十")} → preserve "一十" (e.g. 10 → "一十", 15 →
     *       "一十五")
     *   <li>{@code normalize=true/false && !startsWith("一十")} → no change (e.g. 20 → "二十"
     *       regardless of normalize)
     * </ul>
     *
     * @param input the numeric value to format
     * @param normalize whether to normalize the leading "一十"
     * @param expected the expected Chinese numeral string
     */
    @TableTest(
        """
      Scenario                     | Input | Normalize | Expected
      // normalize=true, startsWith("一十") → "一" removed
      Ten with normalize true      | 10    | true      | 十
      Fifteen with normalize true  | 15    | true      | 十五
      // normalize=false, startsWith("一十") → "一十" preserved
      Ten with normalize false     | 10    | false     | 一十
      Fifteen with normalize false | 15    | false     | 一十五
      // !startsWith("一十") → normalize has no effect
      Twenty with normalize true   | 20    | true      | 二十
      Twenty with normalize false  | 20    | false     | 二十
      """)
    @DisplayName("format.normalize")
    void formatWithNormalizeSucceeds(BigInteger input, boolean normalize, String expected) {
      // format(BigInteger, boolean) with normalize=false preserves the leading "一" in "一十";
      // normalize=true removes it. For numbers not starting with "一十", both produce the same
      // result.
      assertEquals(expected, ChineseUtils.Numerals.format(input, normalize));
    }

    /**
     * Verifies that {@link ChineseUtils.Numerals#format(BigInteger)} throws {@link
     * IllegalArgumentException} for invalid inputs.
     *
     * <p>Covers three exception branches:
     *
     * <ul>
     *   <li>Null input → {@code Assert.notNull} guard
     *   <li>Negative input → {@code Assert.isTrue} guard
     *   <li>Value exceeding 10⁴⁵ - 1 → {@code myriadChar == null} guard (position beyond 载)
     * </ul>
     *
     * @param input the invalid numeric value; may be {@code null}
     */
    @TableTest(
        """
      Scenario                    | Input
      Null input                  |
      Negative input              | -1
      Value exceeding max (10^45) | 1000000000000000000000000000000000000000000000
      """)
    @DisplayName("format.fails")
    void formatFails(BigInteger input) {
      assertThrows(IllegalArgumentException.class, () -> ChineseUtils.Numerals.format(input));
    }
  }
}
