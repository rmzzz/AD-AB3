package ab3.impl.KuparSiarheyeuIsmailov;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LzwCodecTest {

    @Test
    public void testLzwCompressed16bit()
    {
        byte[] input = "ababcbababaaaaad".getBytes();
        //System.out.println("inp: " + toBits(input));
        byte[] encoded = LzwCodec.encode(input, 16);
        //System.out.println("enc: " + toBits(encoded));
        byte[] decoded = LzwCodec.decode(encoded, 16);
        //System.out.println("dec: " + toBits(decoded));
        assertArrayEquals(input, decoded);
    }

    /**
     * An example: Assume codeword 1100110011 is emitted first, and
	 * then codeword 1010101010 is output afterwards, (codeword length
	 * 10bit, so 20bits total). Represented as a bitstring (lowest value
	 * bit on the right), this would yield 10101010101100110011. To get
	 * full bytes, several leading zeros must be added ("padding"). This
	 * would result in 000010101010101100110011, exactly 24bit. Converting
	 * this into a byte array, we would have byte 0 = [00110011], byte 1 =
     * [10101011], byte 2 = [00001010] (byte 0 thus contains the eight
	 * lowest value bits, byte 1 the next eight, and so on).
     */
    @Test
    void encodeBitsExampleFromAb3() {
        int[] out = {0b1100110011, 0b1010101010};
        byte[] test = {0b00110011, (byte)0b10101011, 0b00001010};
        //System.out.println("out: " + toBits(out, 10));
        //System.out.println("tst: " + toBits(test));
        byte[] enc = LzwCodec.encodeBits(out, 10);
        //System.out.println(Arrays.toString(enc));
        //System.out.println("enc: " + toBits(enc));
        assertArrayEquals(test, enc);
    }

    @Test
    void decodeBitsExampleFromAb3() {
        int[] out = {0b1100110011, 0b1010101010};
        byte[] test = {0b00110011, (byte)0b10101011, 0b00001010};
        //System.out.println("out: " + toBits(out, 10));
        //System.out.println("test: " + toBits(test));
        int[] dec = LzwCodec.decodeBits(test, 10);
        //System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(out, dec);

    }

    @Test
    void encodeBits16bit() {
        assertArrayEquals(
                new byte[]{1, 1},
                LzwCodec.encodeBits(new int[]{0x101}, 16));
    }

    @Test
    void encodeBits16bit2x() {
        assertArrayEquals(
                new byte[]{2, 1, 4, 3},
                LzwCodec.encodeBits(new int[]{0x102, 0x304}, 16));
    }

    @Test
    void encodeBits10bit() {
        assertArrayEquals(
                new byte[]{0b010_10101, 0b01},
                LzwCodec.encodeBits(new int[]{0b01010_10101}, 10));
    }

    @Test
    void encodeBits10bit2x() {
        byte[] test = {0b010_10101, 0b0_11111_01, 0};
        //System.out.println(Arrays.toString(test));
        byte[] encoded = LzwCodec.encodeBits(new int[]{0b01010_10101, 0b00000_11111}, 10);
        //System.out.println(Arrays.toString(encoded));
        assertArrayEquals(
                // 01|010_10101 00000_11111
                test, encoded);
    }

    @Test
    void decodeBits16bit() {
        int[] test = {16, 64};
        byte[] enc = LzwCodec.encodeBits(test, 16);
        int[] dec = LzwCodec.decodeBits(enc, 16);
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit() {
        int[] test = {0b01010_10101, 0b00000_11111};
        byte[] enc = LzwCodec.encodeBits(test, 10);
        int[] dec = LzwCodec.decodeBits(enc, 10);
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit12() {
        int[] test = {1, 2};
        byte[] enc = LzwCodec.encodeBits(test, 10);
        int[] dec = LzwCodec.decodeBits(enc, 10);
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit123() {
        int[] test = {1, 2, 3};
        //System.out.println("src: " + toBits(test, 10));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        //System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        //System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit1234() {
        int[] test = {1, 2, 3, 4};
        //System.out.println("test: " + toBits(test, 10));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        //System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        //System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit12345() {
        int[] test = {0, 1, 2, 3, 4, 5};
        //System.out.println("test: " + toBits(test, 10));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        //System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        //System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(test, dec);
    }

    @Test
    void decode() {

        // yields 10 codewords as output (97 98 257 99 258 261 97 263 263 100).
        byte[] exp = "ababcbababaaaaad".getBytes();
        byte[] enc = { 97, 0, 98, 0, 1, 1, 99, 0, 2, 1, 5, 1, 97, 0, 7, 1, 7, 1, 100, 0 };
        byte[] dec = LzwCodec.decode(enc, 16);
        assertArrayEquals(exp, dec);
    }

    @Disabled("for local use only! too heavy for automation")
    @Test
    void encodeDecodeWithResetCodeTable() {
        int bits = 9;
        int beyondMax = (1 << bits);
        byte[] src = new byte[beyondMax];
        for(int i = 0; i < src.length; i++) {
            src[i] = (byte)(0xFF & i);
        }
        //Arrays.setAll(src, i -> i);//fill(src, (byte)'a');
        //src[1] = 'b';
        //src[2] = 'c';
        //System.out.println("src: " + toBits(src));

        byte[] enc = LzwCodec.encode(src, bits);
        //System.out.println("enc: " + toBits(enc));

        byte[] dec = LzwCodec.decode(enc, bits);
        //System.out.println("dec: " + toBits(dec));
        //System.out.println("src: " + new String(src));
        //System.out.println("dec: " + new String(dec));
        assertArrayEquals(src, dec);
    }

    @Test
    @Disabled("for local use only! too heavy for automation")
    void encodeDecodeRandomCharactersAllBitsRange() {
        int minBits = 10; ///< 8 bits makes no sense
        int maxBits = 16; ///< 17 bits can overflow 32bit int!
        int minArrayLength = 0;
        int maxArrayLength = 4096;

        Random rnd = new Random(System.currentTimeMillis());

        for(int bits = minBits; bits <= maxBits; bits++) {

            for(int size = minArrayLength; size <= maxArrayLength; size++){
                byte[] src = new byte[size];
                for(int i = 0; i < size; i++) {
                    src[i] = (byte)('a' + rnd.nextInt(26));
                }

                try {
                    byte[] enc = LzwCodec.encode(src, bits);
//                    //System.out.println("bits: " + bits + "; size: " + size);
//                    //System.out.println("src: " + toBits(src));
//                    //System.out.println("enc: " + toBits(enc));

                    byte[] dec = LzwCodec.decode(enc, bits);
//                    //System.out.println("dec: " + toBits(dec));

                    if (!Arrays.equals(src, dec)) {
                        //System.out.println("bits: " + bits + "; size: " + size);
                        //System.out.println("src: " + toBits(src));
                        //System.out.println("enc: " + toBits(enc));
                        //System.out.println("dec: " + toBits(dec));
                        assertArrayEquals(src, dec,
                                "with bits=" + bits + " and size=" + size + " decoded array not equal to source!");
                    }
                } catch (RuntimeException ex) {
                    fail("Error with bits=" + bits + " and size=" + size , ex);
                }
            }
        }
    }

    String toBits(byte[] array) {
        int[] a = new int[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = 0xFF & array[i];
        }
        return toBits(a, 8);
    }
    String toBits(int[] array, int bits) {
        int mask = (1 << bits) - 1;

        StringBuilder sb = new StringBuilder((1+array.length)*bits);
        sb.append('[').append(array.length).append(']');
        // print positions
        sb.append('\n');
        for( int i = 0; i < array.length; i++) {
            String s = Integer.toString(i);
            sb.append(' ');
            sb.append(s);
            for(int j = s.length(); j < bits; j++) {
                sb.append(' ');
            }
        }
        // print values
        sb.append('\n');
        for(int i : array) {
            sb.append(' ');
            String s = Integer.toBinaryString(i & mask);
            for(int j = s.length(); j < bits; j++) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }
}