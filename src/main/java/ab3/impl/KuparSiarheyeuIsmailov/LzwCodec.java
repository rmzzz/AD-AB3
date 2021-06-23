package ab3.impl.KuparSiarheyeuIsmailov;

// this task is allowed to use java.util.* library

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * LZW Codec
 */
public class LzwCodec {
    static final boolean DEBUG = true;

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
    static byte[] encodeBits(int[] out, int outLength, int bits) {
        int totalBits = outLength * bits;
        int totalBytes = (totalBits + 7) / 8;
        byte[] encoded = new byte[totalBytes];
        int encIdx = 0;
        int bitsOffset = 0;
        int code = 0;
        for (int i = 0; i < outLength; i++) {
            code |= out[i] << bitsOffset;

            while (bitsOffset + 8 <= bits) {
                encoded[encIdx++] = (byte)(code & 0xFF);
                code >>= 8;
                bitsOffset += 8;
            }
            bitsOffset = bits - bitsOffset;
        }
        if(bitsOffset > 0) {
            encoded[encIdx] = (byte)(code & 0xFF);
        }
        if(DEBUG)
            System.out.println("debug: encoded: " + Arrays.toString(encoded));

        return encoded;
    }

    static int[] decodeBits(byte[] data, int bits) {
        int max = ((1 << bits) - 1);
        int[] input = new int[data.length * bits/8];
        int inputSize = 0;
        int code = 0;
        int bitsOffset = 0;
        for(byte b : data) {
            if(bitsOffset >= bits) {
                input[inputSize++] = code & max;
                code >>= bits;
                bitsOffset -= bits;
            }
            code |= (b & 0xFF) << bitsOffset;
            bitsOffset += 8;
        }
        if(bitsOffset > 0) {
            input[inputSize++] = code & max;
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

        int max = ((1 << bits) - 1);

        int[] input = decodeBits(data, bits);

        Map<Integer, int[]> tTab = new HashMap<>(data.length);
        int nextCode = 257;
        byte[] out = new byte[data.length * 4];
        int outIdx = 0;
        int oldCode = input[0];
        out[outIdx++] = (byte)oldCode;
        for(int i = 1; i < input.length; i++) {
            int newCode = input[i];
            if(newCode == 256) {
                tTab.clear();
                nextCode = 257;
                continue;
            }
            int[] psp = tTab.get(newCode);
            if(psp == null) { // not in tTab
                tTab.put(nextCode++, new int[]{oldCode, oldCode});
            } else {
                tTab.put(nextCode++, new int[]{oldCode, psp[0]});
            }
            out[outIdx++] = (byte) newCode;
            oldCode = newCode;
        }

        out = Arrays.copyOf(out, outIdx);
        return out;
    }
}
