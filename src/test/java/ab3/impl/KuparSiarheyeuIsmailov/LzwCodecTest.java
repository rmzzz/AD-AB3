package ab3.impl.KuparSiarheyeuIsmailov;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LzwCodecTest {

    @Test
    void encode() {
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
        System.out.println("out: " + toBits(out, 10));
        System.out.println("tst: " + toBits(test));
        byte[] enc = LzwCodec.encodeBits(out, 10);
        System.out.println(Arrays.toString(enc));
        System.out.println("enc: " + toBits(enc));
        assertArrayEquals(test, enc);
    }

    @Test
    void decodeBitsExampleFromAb3() {
        int[] out = {0b1100110011, 0b1010101010};
        byte[] test = {0b00110011, (byte)0b10101011, 0b00001010};
        System.out.println("out: " + toBits(out, 10));
        System.out.println("test: " + toBits(test));
        int[] dec = LzwCodec.decodeBits(test, 10);
        System.out.println("dec: " + toBits(dec, 10));
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
        System.out.println(Arrays.toString(test));
        byte[] encoded = LzwCodec.encodeBits(new int[]{0b01010_10101, 0b00000_11111}, 10);
        System.out.println(Arrays.toString(encoded));
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
        System.out.println("src: " + toBits(test, 10));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit1234() {
        int[] test = {1, 2, 3, 4};
        System.out.println("test: " + toBits(test, 10));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        System.out.println("dec: " + toBits(dec, 10));
        assertArrayEquals(test, dec);
    }

    @Test
    void decode() {
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

        StringBuilder sb = new StringBuilder();
        for(int i : array) {
            String s = Integer.toBinaryString(i & mask);
            int len = s.length();
            for(int j = len; j < bits; j++) {
                sb.append('0');
            }
            sb.append(s);
            sb.append(' ');
        }
        return sb.toString();
    }
}