package ab3.impl.KuparSiarheyeuIsmailov;

// this task is allowed to use java.util.* library

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * LZW Codec using LSB first packing order
 */
public class LzwCodec {
    static final boolean DEBUG = false;

    protected static class PrefixSuffixPair {
        public final int prefix;
        public final int suffix;

        public PrefixSuffixPair(int prefix, int suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PrefixSuffixPair that = (PrefixSuffixPair) o;
            return (prefix == that.prefix
                    && suffix == that.suffix);
        }

        @Override
        public int hashCode() {
            return prefix * 31 + suffix;
        }
    }

    static class TranslationTable {
        static final int RESET = 256;
        int nextCode;
        int[][] table;

        TranslationTable(int size) {
            table = new int[size][];
            reset();
        }

        public int decode(int code, byte[] output, int offset) {
            if(code < RESET) {
                output[offset] = (byte) code;
                return 1;
            }
            int count = 0;
            for (int c : table[code - RESET]) {
                count += decode(c, output, offset + count);
            }
            return count;
        }

        public byte getFirst(int code) {
            if(code < RESET)
                return (byte) code;
            return getFirst(table[code - RESET][0]);
        }

        public int add(int prefix, int suffix) {
            table[nextCode - RESET] = new int[]{prefix, suffix};
            return nextCode++;
        }

        public boolean contains(int code) {
            return code < nextCode;
        }

        public void reset() {
            nextCode = RESET + 1;
        }
    }

    public static byte[] encode(byte[] data, int bits) {
        if(data.length == 0)
            return new byte[0];

        int max = ((1 << bits) - 1);
        Map<PrefixSuffixPair, Integer> table = new HashMap<>(data.length);
        int nextCode = 257;
        int[] out = new int[data.length];
        int outIdx = 0;
        // 1
        int pref = data[0];
        for (int i = 1; i < data.length; i++) {
            // 2
            int sfx = data[i];
            // 4
            PrefixSuffixPair key = new PrefixSuffixPair(pref, sfx);
            Integer code = table.get(key);
            if(code == null) {
                if(nextCode >= max) {
                    out[outIdx++] = 256;
                    table.clear();
                    nextCode = 257;
                }
                table.put(key, nextCode++);
                out[outIdx++] = pref;
                pref = sfx;
            } else {
                pref = code;
            }
        }
        out[outIdx++] = pref;
        if(DEBUG)
            System.out.println("debug: out: " + Arrays.toString(out));

        return encodeBits(out, outIdx, bits);
    }

    static byte[] encodeBits(int[] out, int bits) {
        return encodeBits(out, out.length, bits);
    }

    /**
     * e.g.
     *  bits = 10
     *  out = {1100110011, 1010101010}
     *  returns {00110011, 10101011, 00001010}
     */
    static byte[] encodeBits(int[] out, int outLength, int bits) {
        int totalBits = outLength * bits;
        int totalBytes = (totalBits + 7) / 8;
        byte[] encoded = new byte[totalBytes];
        int encIdx = 0;
        int bitsOffset = 0;
        int code = 0;
        for (int i = 0; i < outLength; i++) {
            code |= out[i] << bitsOffset;

            if(DEBUG) {
                System.out.printf("%d) out=%s -> code=%s (bitsOffset=%d)\n",
                        i, Integer.toBinaryString(out[i]), Integer.toBinaryString(code), bitsOffset);
            }

            bitsOffset += bits; ///< mark read bits

            while (bitsOffset >= 8) { ///< while enough bits to write a byte
                encoded[encIdx++] = (byte)(code & 0xFF);
                code >>= 8;  ///< shift written bits away
                bitsOffset -= 8; ///< mark written bits
                if(DEBUG) {
                    System.out.printf(" => wrote: %s -> code=%s (bitsOffset=%d)\n",
                            Integer.toBinaryString(0xFF & encoded[encIdx-1]),
                            Integer.toBinaryString(code), bitsOffset);
                }
            }
            //bitsOffset = (bitsOffset + bits) % 8;
        }
        while(bitsOffset > 0) {
            encoded[encIdx++] = (byte)(code & 0xFF);
            code >>= 8;
            bitsOffset -= 8;
            if(DEBUG) {
                System.out.printf(" => wrote: %s -> code=%s \n",
                        Integer.toBinaryString(0xFF & encoded[encIdx-1]),
                        Integer.toBinaryString(code));
            }
        }
        if(DEBUG)
            System.out.println("debug: encoded: " + Arrays.toString(encoded));

        return encoded;
    }

    static int[] decodeBits(byte[] data, int bits) {
        int mask = ((1 << bits) - 1);
        int[] input = new int[data.length * bits/8];
        int inputSize = 0;
        int code = 0;
        int bitsOffset = 0;
        for(byte b : data) {
            if(bitsOffset >= bits) {
                input[inputSize++] = code & mask;
                code >>= bits;
                bitsOffset -= bits;
            }
            code |= (b & 0xFF) << bitsOffset;
            bitsOffset += 8;
        }
        if(bitsOffset > 0) {
            input[inputSize++] = code & mask;
        }
        input = Arrays.copyOf(input, inputSize);
        if(DEBUG) {
            System.out.println("debug: decoded: " + Arrays.toString(input));
        }

        return input;
    }


    public static byte[] decode(byte[] data, int bits) {
        if(data.length == 0)
            return new byte[0];

        int[] input = decodeBits(data, bits);

        // see VO 11-74
        TranslationTable tTab = new TranslationTable(input.length); ///< should not have mode codes than input length
        byte[] out = new byte[input.length * 4];
        int outIdx = 0;
        int oldCode = input[0];
        out[outIdx++] = (byte)oldCode;
        for(int i = 1; i < input.length; i++) {
            int newCode = input[i];
            if(newCode == TranslationTable.RESET) {
                tTab.reset();
                continue;
            }
            // optimized "if NewCode not in TTab then ... else ... endif"
            tTab.add(oldCode, tTab.getFirst(tTab.contains(newCode) ? newCode : oldCode));
            // output decoded newCode into out
            outIdx += tTab.decode(newCode, out, outIdx);
            oldCode = newCode;
        }

        out = Arrays.copyOf(out, outIdx);
        return out;
    }
}
