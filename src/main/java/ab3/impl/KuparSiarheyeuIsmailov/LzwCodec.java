package ab3.impl.KuparSiarheyeuIsmailov;

// this task is allowed to use java.util.* library

import java.util.HashMap;
import java.util.Map;

/**
 * LZW Codec
 */
public class LzwCodec {
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

        int totalBits = outIdx * bits;
        int totalBytes = (totalBits + 7) / 8;
        byte[] encoded = new byte[totalBytes];
        int encIdx = 0;
        int bitsOffset = 0;
        int code = 0;
        for (int i = 0; i < outIdx; i++) {
            code |= out[i] << bitsOffset;

            while (bitsOffset < bits) {
                encoded[encIdx++] = (byte)(code & 0xFF);
                code >>= 8;
                bitsOffset += 8;
            }
            bitsOffset %= bits;
        }
        return encoded;
    }

    public static byte[] decode(byte[] data, int bits) {
        if(data.length == 0)
            return new byte[0];

        int max = ((1 << bits) - 1);
        Map<PrefixSuffixPair, Integer> table = new HashMap<>(data.length);
        byte[] out = new byte[];
        for(int i = 0; i < data.length; i++) {
            int code = data[i];
            if(code < 256) {

            }
        }

        return out;
    }
}
