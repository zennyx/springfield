package zenny.toybox.springfield.util.algorithm;

import java.util.Optional;

import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Hasher;

public enum Hashing implements Hasher<Object> {

  STANDARD {

    @Override
    public int smear(int hashCode) {
      return hashCode;
    }
  },

  HASHMAP {

    @Override
    protected int smear(int hashCode) {
      return hashCode >>> 16;
    }

  },

  MURMURHASH3 {

    private static final int C1 = 0xcc9e2d51;

    private static final int C2 = 0x1b873593;

    /**
     * This method was rewritten in Java from an intermediate step of the Murmur
     * hash function in
     * http://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp, which
     * contained the following header:
     * <p>
     * MurmurHash3 was written by Austin Appleby, and is placed in the public
     * domain. The author hereby disclaims copyright to this source code.
     *
     * @param hashCode
     * @return
     */
    @Override
    public int smear(int hashCode) {
      return C2 * Integer.rotateLeft(hashCode * C1, 15);
    }
  },

  ACC4 {

    @Override
    public int smear(int hashCode) {
      hashCode += ~(hashCode << 9);
      hashCode ^= hashCode >>> 14;
      hashCode += hashCode << 4;
      hashCode ^= hashCode >>> 10;

      return hashCode;
    }
  };

  @Override
  public int hash(@Nullable Object object) {
    return this.smear(Optional.ofNullable(object).hashCode());
  }

  protected abstract int smear(int hashCode);
}
