package zenny.toybox.springfield.jackson.io.escaping;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

@SuppressWarnings("serial")
public class OwaspCharacterEscapes extends CharacterEscapes {

  private final int[] ESCAPES;

  public OwaspCharacterEscapes() {
    this.ESCAPES = standardAsciiEscapesForJSON();
    for (int i = 0; i < this.ESCAPES.length; i++) {
      if (!(Character.isAlphabetic(i) || Character.isDigit(i))) {
        this.ESCAPES[i] = CharacterEscapes.ESCAPE_CUSTOM;
      }
    }
  }

  @Override
  public int[] getEscapeCodesForAscii() {
    return this.ESCAPES;
  }

  @Override
  public SerializableString getEscapeSequence(int ch) {
    String unicode = String.format("\\u%04x", ch);
    return new SerializedString(unicode);
  }
}
