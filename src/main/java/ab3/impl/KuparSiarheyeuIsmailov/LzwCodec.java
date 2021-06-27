package ab3.impl.KuparSiarheyeuIsmailov;

// this task is allowed to use java.util.* libraries

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LZW Codec using LSB first packing order
 */
public class LzwCodec {
    static final boolean DEBUG = false;
    static final boolean TRACE = false;

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
        static final int FIRST_CODE = RESET + 1;
        int nextCode;
        int[][] table;

        TranslationTable(int initialSize) {
            table = new int[initialSize][];
            reset();
        }

        public int decode(int code, byte[] output, int offset) {
            if (code < RESET) {
                output[offset] = (byte) code;
                return 1;
            }

            int count = 0;
            int[] word = table[code - FIRST_CODE];
            for (int c : word) {
                count += decode(c, output, offset + count);
            }
            return count;
        }

        public void decode(int code, ByteArrayBuffer out) {
            if (code < RESET) {
                out.write(code);
            } else {
                assert (code > RESET); ///< RESET should never land here
                int[] word = table[code - FIRST_CODE];
                for (int c : word) {
                    decode(c, out);
                }
            }
        }

        public byte getFirst(int code) {
            if (code < RESET)
                return (byte) code;
            return getFirst(table[code - FIRST_CODE][0]);
        }

        public int add(int prefix, int suffix) {
            table[nextCode - FIRST_CODE] = new int[]{prefix, suffix};
            return nextCode++;
        }

        public boolean contains(int code) {
            return code < nextCode;
        }

        public void reset() {
            nextCode = FIRST_CODE;
        }
    }

    static class IntArrayBuffer {
        int[] buffer;
        int size;

        IntArrayBuffer(int initialSize) {
            buffer = new int[initialSize];
            size = 0;
        }

        public void write(int value) {
            if (size >= buffer.length) {
                buffer = Arrays.copyOf(buffer, size * 2);
            }
            buffer[size++] = value;
        }

        public int[] toArray() {
            return Arrays.copyOf(buffer, size);
        }

        public String toString() {
            return Arrays.stream(buffer).limit(size)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }

    static class ByteArrayBuffer {
        byte[] buffer;
        int size;

        ByteArrayBuffer(int initialSize) {
            buffer = new byte[initialSize];
            size = 0;
        }

        public void write(int value) {
            write((byte) (0xFF & value));
        }

        public void write(byte value) {
            if (size >= buffer.length) {
                buffer = Arrays.copyOf(buffer, size * 2);
            }
            buffer[size++] = value;
        }

        public byte[] toArray() {
            return Arrays.copyOf(buffer, size);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(size * 4);
            for (int i = 0; i < size; i++) {
                sb.append(sb.length() == 0 ? "[" : ", ");
                sb.append(0xFF & buffer[i]);
            }
            sb.append(']');
            return sb.toString();
        }
    }


    public static byte[] encode(byte[] data, int bits) {
        if (data.length == 0)
            return new byte[0];

        int max = ((1 << bits) - 1);

        trace("max=%d", max);

        // see VO 11-30
        Map<PrefixSuffixPair, Integer> table = new HashMap<>(data.length);
        int nextCode = TranslationTable.FIRST_CODE;
        IntArrayBuffer out = new IntArrayBuffer(data.length);
        // 1
        int pref = 0xFF & data[0];
        for (int i = 1; i < data.length; i++) {
            // 2
            int sfx = 0xFF & data[i];
            // 4
            PrefixSuffixPair pair = new PrefixSuffixPair(pref, sfx);
            Integer index = table.get(pair);
            if (index != null) {
                pref = index;
            } else {
                if (nextCode > max) {
                    out.write(TranslationTable.RESET);
                    table.clear();
                    debug("reset at i=%d; nextCode=%d\n", i, nextCode);
                    nextCode = TranslationTable.FIRST_CODE;
                    // reset cached pref as well: sfx is read, but pref can refer to reset index
                } else {
                    table.put(pair, nextCode++);
                }
                out.write(pref);
                pref = sfx;
            }
        }
        // 3
        out.write(pref);

        debug("out: %s", out);

        return encodeBits(out.toArray(), bits);
    }

    /**
     * e.g.
     * bits = 10
     * out = {1100110011, 1010101010}
     * returns {00110011, 10101011, 00001010}
     */
    static byte[] encodeBits(int[] out, int bits) {
        int totalBits = out.length * bits;
        int totalBytes = (totalBits + 7) / 8;
        byte[] encoded = new byte[totalBytes];
        int encIdx = 0;
        int bitsOffset = 0;
        int code = 0;
        for (int i = 0; i < out.length; i++) {
            code |= out[i] << bitsOffset;

            if (TRACE) {
                trace("%d) out=%s -> code=%s (bitsOffset=%d)\n",
                        i, Integer.toBinaryString(out[i]), Integer.toBinaryString(code), bitsOffset);
            }

            bitsOffset += bits; ///< mark read bits

            while (bitsOffset >= 8) { ///< while enough bits to write a byte
                encoded[encIdx++] = (byte) (code & 0xFF);
                code >>= 8;  ///< shift written bits away
                bitsOffset -= 8; ///< mark written bits
                if (TRACE) {
                    trace(" => wrote: %s -> code=%s (bitsOffset=%d)\n",
                            Integer.toBinaryString(0xFF & encoded[encIdx - 1]),
                            Integer.toBinaryString(code), bitsOffset);
                }
            }
        }

        while (bitsOffset > 0) {
            encoded[encIdx++] = (byte) (code & 0xFF);
            code >>= 8;
            bitsOffset -= 8;
            if (TRACE) {
                trace(" => wrote: %s -> code=%s \n",
                        Integer.toBinaryString(0xFF & encoded[encIdx - 1]),
                        Integer.toBinaryString(code));
            }
        }

        if(DEBUG) {
            debug("encoded bits: %s", Arrays.toString(encoded));
        }

        return encoded;
    }

    static int[] decodeBits(byte[] data, int bits) {
        int mask = ((1 << bits) - 1);
        int[] input = new int[data.length * bits / 8];
        int inputSize = 0;
        int code = 0;
        int bitsOffset = 0;
        for (byte b : data) {
            if (bitsOffset >= bits) {
                input[inputSize++] = code & mask;
                code >>= bits;
                bitsOffset -= bits;
            }
            code |= (b & 0xFF) << bitsOffset;
            bitsOffset += 8;
        }
        if (bitsOffset > 0) {
            input[inputSize++] = code & mask;
        }
        input = Arrays.copyOf(input, inputSize);

        debug("debug: decoded bits: %s", input);

        return input;
    }


    public static byte[] decode(byte[] data, int bits) {
        if (data.length == 0)
            return new byte[0];

        int[] input = decodeBits(data, bits);

        // see VO 11-74
        TranslationTable tTab = new TranslationTable(input.length); ///< should not have more codes than input length
        ByteArrayBuffer out = new ByteArrayBuffer(input.length);
        int oldCode = input[0];
        out.write(oldCode);
        for (int i = 1; i < input.length; i++) {
            int newCode = input[i];
            if (newCode == TranslationTable.RESET) {

                debug("decode: reset at %d, nextCode=%d\n", i, tTab.nextCode);

                tTab.reset();
                // reset cached codes as well
                oldCode = input[++i];
                out.write(oldCode);
                continue;
            }
            // optimized "if NewCode not in TTab then ... else ... endif"
            tTab.add(oldCode, tTab.getFirst(tTab.contains(newCode) ? newCode : oldCode));
            // output decoded newCode into out
            tTab.decode(newCode, out);
            oldCode = newCode;
        }

        return out.toArray();
    }

    private static void debug(String msg, Object... params) {
        if (DEBUG) {
            //System.out.printf("debug: " + msg + "\n", params);
        }
    }

    private static void trace(String msg, Object... params) {
        if (TRACE && DEBUG) {
            //System.out.printf("trace: " + msg + "\n", params);
        }
    }
}
