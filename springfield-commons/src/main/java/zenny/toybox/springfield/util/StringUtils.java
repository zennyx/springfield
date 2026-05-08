package zenny.toybox.springfield.util;

import java.math.BigInteger;
import java.util.Map;

/**
 * Extended string utilities that build upon Spring's {@link org.springframework.util.StringUtils},
 * providing additional conversion and manipulation capabilities.
 *
 * <p>This class is not instantiable.
 */
public final class StringUtils extends org.springframework.util.StringUtils {

  /** Suppresses default constructor, ensuring non-instantiability. */
  private StringUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Utility class for Chinese numeral conversion, supporting parsing of Chinese numerals (e.g.
   * "一万二千三百四十五") into {@link BigInteger} values.
   *
   * <p>Supports coefficient words (零~九), decimal digit words (十, 百, 千), and myriad-scale digit
   * words (万, 亿, 兆, 京, 垓, 秭, 穰, 沟, 涧, 正, 载).
   *
   * <p>This class is not instantiable.
   */
  public static final class Chinese {

    /**
     * Coefficient words mapping Chinese numeral characters (零~九) to their integer values. Excludes
     * special words: 十 (ten), 半 (half), 两 (two-as-pair).
     */
    public static final Map<Character, Integer> NUMERAL_COEFFICIENT_WORDS =
        Map.of(
            '〇', 0,
            '一', 1,
            '二', 2,
            '三', 3,
            '四', 4,
            '五', 5,
            '六', 6,
            '七', 7,
            '八', 8,
            '九', 9);

    /**
     * Decimal digit words mapping Chinese place-value characters (十, 百, 千) to their integer values.
     * Excludes 万 (ten-thousand) which belongs to the myriad scale.
     */
    public static final Map<Character, Integer> NUMERAL_DIGIT_WORDS_DECIMAL =
        Map.of(
            '十', 10,
            '百', 100,
            '千', 1000);

    /**
     * Myriad-scale digit words mapping Chinese large-number characters (万, 亿, 兆, 京, 垓, 秭, 穰, 沟, 涧,
     * 正, 载) to their {@link BigInteger} values, each a power of ten.
     *
     * <p>The scale follows the traditional Chinese myriad system where each unit represents 10^4
     * times the previous: 万(10^4), 亿(10^8), 兆(10^12), etc.
     */
    public static final Map<Character, BigInteger> NUMERAL_DIGIT_WORDS_MYRIAD =
        Map.ofEntries(
            Map.entry('万', BigInteger.TEN.pow(4)),
            Map.entry('亿', BigInteger.TEN.pow(8)),
            Map.entry('兆', BigInteger.TEN.pow(12)),
            Map.entry('京', BigInteger.TEN.pow(16)),
            Map.entry('垓', BigInteger.TEN.pow(20)),
            Map.entry('秭', BigInteger.TEN.pow(24)),
            Map.entry('穰', BigInteger.TEN.pow(28)),
            Map.entry('沟', BigInteger.TEN.pow(32)),
            Map.entry('涧', BigInteger.TEN.pow(36)),
            Map.entry('正', BigInteger.TEN.pow(40)),
            Map.entry('载', BigInteger.TEN.pow(44)));

    /** Suppresses default constructor, ensuring non-instantiability. */
    private Chinese() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a Chinese numeral string to its {@link BigInteger} representation.
     *
     * <p>This method parses Chinese numerals that combine coefficient words, decimal digit words,
     * and myriad-scale digit words. For example, "一万二千三百四十五" is converted to 12345.
     *
     * <p>The algorithm processes characters left-to-right, accumulating values across three levels:
     * the current coefficient ({@code prev}), the decimal-level sum ({@code myriad}), and the
     * myriad-level sum ({@code total}). When a myriad-scale word is encountered, the accumulated
     * decimal-level sum is multiplied by the myriad value and added to the total.
     *
     * @param numerals the Chinese numeral string to convert (e.g. "一万二千三百四十五")
     * @return the converted numeric value as a {@link BigInteger}
     * @throws IllegalArgumentException if the input is blank or contains invalid characters
     * @see <a href="https://en.wikipedia.org/wiki/Chinese_numerals">Chinese Numerals</a>
     */
    public static BigInteger toUnsignedInteger(String numerals) {
      if (!StringUtils.hasText(numerals)) {
        throw new IllegalArgumentException("Invalid Chinese numerals format.");
      }

      // prev: the current coefficient value (defaults to 1 for implicit coefficients, e.g. "十"
      // means "一十")
      // myriad: the accumulated sum at the decimal level (十, 百, 千)
      // total: the accumulated sum at the myriad level (万, 亿, 兆, ...)
      BigInteger prev = BigInteger.ONE;
      BigInteger myriad = BigInteger.ZERO;
      BigInteger total = BigInteger.ZERO;

      for (int i = 0; i < numerals.length(); i++) {
        char numeral = numerals.charAt(i);

        // Coefficient word: store as the current coefficient for the next digit/myriad word
        if (NUMERAL_COEFFICIENT_WORDS.containsKey(numeral)) {
          prev = BigInteger.valueOf(NUMERAL_COEFFICIENT_WORDS.get(numeral));
          continue;
        }

        // Decimal digit word: multiply the coefficient by the place value and add to the
        // decimal-level sum
        if (NUMERAL_DIGIT_WORDS_DECIMAL.containsKey(numeral)) {
          myriad =
              myriad.add(
                  (prev.equals(BigInteger.ZERO) ? BigInteger.ONE : prev)
                      .multiply(BigInteger.valueOf(NUMERAL_DIGIT_WORDS_DECIMAL.get(numeral))));
          prev = BigInteger.ZERO;
          continue;
        }

        // Myriad digit word: add the decimal-level sum to the total, scaled by the myriad value
        if (NUMERAL_DIGIT_WORDS_MYRIAD.containsKey(numeral)) {
          myriad = myriad.add(prev);
          total =
              total.add(
                  (myriad.equals(BigInteger.ZERO) ? BigInteger.ONE : myriad)
                      .multiply(NUMERAL_DIGIT_WORDS_MYRIAD.get(numeral)));
          myriad = BigInteger.ZERO;
          prev = BigInteger.ZERO;
          continue;
        }

        throw new IllegalArgumentException("Invalid Chinese numerals format.");
      }

      // Combine all remaining accumulated values
      return total.add(myriad).add(prev);
    }
  }
}
