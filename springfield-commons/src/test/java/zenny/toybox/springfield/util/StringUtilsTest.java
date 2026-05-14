package zenny.toybox.springfield.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

/**
 * Tests for {@link StringUtils}, covering the {@link StringUtils#xorMask(String, char)} and {@link
 * StringUtils#xorMask(String, String)} methods with full branch coverage.
 *
 * <p>Branch coverage for {@link StringUtils#xorMask(String, char)}:
 *
 * <ul>
 *   <li>Null input → returns {@code null} (early-exit branch)
 *   <li>Empty string → returns empty string (loop body not entered)
 *   <li>Non-null, non-empty input → each character XORed with the secret (loop body executed)
 *   <li>Secret = 0 → identity (each character XORed with 0, unchanged)
 * </ul>
 *
 * <p>Branch coverage for {@link StringUtils#xorMask(String, String)}:
 *
 * <ul>
 *   <li>Null input → returns {@code null} (early-exit branch)
 *   <li>Empty string → returns empty string (loop body not entered)
 *   <li>Non-empty secret → each character XORed with the cycling secret key (loop body executed,
 *       key cycling via {@code i % key.length})
 *   <li>Empty secret → {@link IllegalArgumentException} (Assert.hasLength guard)
 * </ul>
 */
@DisplayName("StringUtils")
class StringUtilsTest {

  /**
   * Exercises all branches of {@link StringUtils#xorMask(String, char)}.
   *
   * <p>Covers:
   *
   * <ul>
   *   <li>{@code null} input → {@code null} return (early-exit branch)
   *   <li>Empty string → empty string (loop body not entered)
   *   <li>Non-null, non-empty input with non-zero secret → each character XORed (loop body
   *       executed)
   *   <li>Non-null input with zero secret → identity (each character XORed with 0, unchanged)
   * </ul>
   *
   * <p>The case-toggle secret (space = {@code 0x20}) is used for readability: {@code 'A' ^ ' ' =
   * 'a'}, {@code 'H' ^ ' ' = 'h'}, etc.
   *
   * @param input the string to mask; may be {@code null}
   * @param secret the character used as the XOR key
   * @param expected the expected XOR-masked result; may be {@code null}
   */
  @TableTest(
      """
    Scenario     | Value   | Secret | Expected
    Null input   |         | " "    |
    Empty string | ""      | " "    | ""
    Single char  | "A"     | " "    | "a"
    Multi char   | "Hello" | " "    | "hELLO"
    """)
  @DisplayName("xorMask(char).succeeds")
  void xorMaskCharSucceeds(String input, char secret, String expected) {
    assertEquals(expected, StringUtils.xorMask(input, secret));
  }

  /**
   * Verifies the identity branch of {@link StringUtils#xorMask(String, char)}: XORing with {@code
   * '\0'} leaves every character unchanged (A ^ 0 = A).
   *
   * <p>This case cannot be expressed as a TableTest row because the null character {@code '\0'}
   * cannot be represented as a cell value in the table.
   */
  @Test
  @DisplayName("xorMask(char).identity")
  void xorMaskCharIdentity() {
    assertEquals("hello", StringUtils.xorMask("hello", '\0'));
  }

  /**
   * Verifies the symmetric property of {@link StringUtils#xorMask(String, char)}: applying XOR
   * masking twice with the same secret restores the original string (A^B^B = A).
   *
   * <p>This complements {@link #xorMaskCharSucceeds} by exercising the full encrypt-decrypt
   * round-trip with a non-trivial secret, ensuring the loop body processes each character
   * independently and correctly.
   */
  @Test
  @DisplayName("xorMask(char).symmetry")
  void xorMaskCharSymmetry() {
    String original = "Hello, World!";
    char secret = 'K';
    String masked = StringUtils.xorMask(original, secret);
    // Masking must change the string (secret is non-zero)
    assertNotEquals(original, masked);
    // Masking again with the same secret must restore the original
    assertEquals(original, StringUtils.xorMask(masked, secret));
  }

  /**
   * Exercises all branches of {@link StringUtils#xorMask(String, String)}.
   *
   * <p>Covers:
   *
   * <ul>
   *   <li>{@code null} input → {@code null} return (early-exit branch)
   *   <li>Empty string → empty string (loop body not entered)
   *   <li>Single-char secret → equivalent to the char variant (cycling has no visible effect)
   *   <li>Multi-char secret with input longer than secret → key cycles via {@code i % key.length}
   *   <li>Secret longer than input → only the first {@code value.length()} characters of the key
   *       are used
   * </ul>
   *
   * <p>For the multi-char secret case, digits and uppercase letters are chosen so that XOR results
   * are printable ASCII: {@code '0'(0x30) ^ 'A'(0x41) = 'q'(0x71)}, {@code '2'(0x32) ^ 'B'(0x42) =
   * 'p'(0x70)}, etc.
   *
   * @param input the string to mask; may be {@code null}
   * @param secret the string used as the cyclic XOR key
   * @param expected the expected XOR-masked result; may be {@code null}
   */
  @TableTest(
      """
    Scenario                 | Value  | Secret | Expected
    Null input               |        | "key"  |
    Empty string             | ""     | "key"  | ""
    Single-char secret       | "0"    | "A"    | "q"
    Multi-char secret        | "0246" | "AB"   | "qput"
    Secret longer than input | "0"    | "AB"   | "q"
    Blank secret             | "A"    | " "    | "a"
    """)
  @DisplayName("xorMask(String).succeeds")
  void xorMaskStringSucceeds(String input, String secret, String expected) {
    assertEquals(expected, StringUtils.xorMask(input, secret));
  }

  /**
   * Verifies the symmetric property of {@link StringUtils#xorMask(String, String)}: applying XOR
   * masking twice with the same secret restores the original string (A^B^B = A).
   *
   * <p>This test exercises the key-cycling logic ({@code i % key.length}) by using a
   * multi-character secret that is shorter than the input, ensuring the modulo operation is
   * triggered and the cycling is correct across the full round-trip.
   */
  @Test
  @DisplayName("xorMask(String).symmetry")
  void xorMaskStringSymmetry() {
    String original = "Hello, World!";
    String secret = "secretKey";
    String masked = StringUtils.xorMask(original, secret);
    assertNotEquals(original, masked);
    assertEquals(original, StringUtils.xorMask(masked, secret));
  }

  /**
   * Verifies that {@link StringUtils#xorMask(String, String)} throws {@link
   * IllegalArgumentException} when the secret is empty.
   *
   * <p>Covers the {@code Assert.hasLength} guard branch:
   *
   * <ul>
   *   <li>Empty secret ({@code ""}) → rejected (would cause {@code i % 0} arithmetic error)
   * </ul>
   *
   * @param secret the invalid secret string
   */
  @TableTest(
      """
    Scenario     | Secret
    Empty secret | ""
    """)
  @DisplayName("xorMask(String).fails")
  void xorMaskStringFails(String secret) {
    assertThrows(IllegalArgumentException.class, () -> StringUtils.xorMask("test", secret));
  }
}
