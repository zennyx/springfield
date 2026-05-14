package zenny.toybox.springfield.util;

import org.jspecify.annotations.Nullable;
import org.springframework.lang.Contract;

/**
 * Extension of Spring's {@link org.springframework.util.StringUtils}, providing additional
 * conversion and manipulation capabilities.
 *
 * <p>This class is not instantiable.
 *
 * @see org.springframework.util.StringUtils
 */
public final class StringUtils extends org.springframework.util.StringUtils {

  /** Suppresses default constructor, ensuring non-instantiability. */
  private StringUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Applies XOR masking to the given string using the specified secret character.
   *
   * <p>This method leverages the symmetric property of XOR (A^B^B = A): masking a string and then
   * masking the result again with the same secret restores the original string. This makes it
   * suitable for lightweight obfuscation where the same operation serves as both "encrypt" and
   * "decrypt".
   *
   * <p><b>Example:</b>
   *
   * <pre>
   * String masked = xorMask("hello", 'K');          // obfuscate
   * String original = xorMask(masked, 'K');         // restore → "hello"
   * </pre>
   *
   * <p><b>Warning:</b> XOR masking provides only minimal obfuscation and is <em>not</em> suitable
   * for security-sensitive scenarios. Since Java {@code char} is 16-bit, there are at most 65536
   * possible keys, which can be trivially reversed through frequency analysis or brute force.
   *
   * @param str the string to mask; if {@code null}, {@code null} is returned
   * @param secret the character used as the XOR key; can use {@code \\uXXXX} hex escapes for
   *     arbitrary 16-bit values (e.g. 'K' → '\u004B')
   * @return the XOR-masked string, or {@code null} if the input is {@code null}
   * @see #xorMask(String, String)
   */
  @Contract("null, _ -> null; !null, _ -> !null")
  public static @Nullable String xorMask(@Nullable String str, char secret) {
    if (str == null) {
      return null;
    }

    char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      chars[i] ^= secret;
    }
    return new String(chars);
  }

  /**
   * Applies XOR masking to the given string using the specified secret string, cycling through the
   * secret characters for each character in the input (Vigenère-style XOR).
   *
   * <p>Like the single-key variant, this method is symmetric: applying it twice with the same
   * secret restores the original string.
   *
   * <p><b>Example:</b>
   *
   * <pre>
   * String masked = xorMask("hello", "secret");   // obfuscate
   * String original = xorMask(masked, "secret");  // restore → "hello"
   * </pre>
   *
   * <p><b>Warning:</b> XOR masking provides only minimal obfuscation and is <em>not</em> suitable
   * for security-sensitive scenarios.
   *
   * @param str the string to mask; if {@code null}, {@code null} is returned
   * @param secret the string used as the cyclic XOR key; must not be empty
   * @return the XOR-masked string, or {@code null} if the input is {@code null}
   * @throws IllegalArgumentException if {@code secret} is empty
   * @see #xorMask(String, char)
   */
  @Contract("null, _ -> null; !null, _ -> !null")
  public static @Nullable String xorMask(@Nullable String str, String secret) {
    if (str == null) {
      return null;
    }

    Assert.hasLength(secret, "'secret' must not be empty");

    char[] chars = str.toCharArray();
    char[] key = secret.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      chars[i] ^= key[i % key.length];
    }
    return new String(chars);
  }
}
