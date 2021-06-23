package ab3.impl.KuparSiarheyeuIsmailov;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LzwCodecTest {

    @Test
    void encode() {
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
        System.out.println("test: " + toBits(test, 0b11111_11111));
        byte[] enc = LzwCodec.encodeBits(test, 10);
        System.out.println("enc: " + toBits(enc));
        int[] dec = LzwCodec.decodeBits(enc, 10);
        System.out.println("dec: " + toBits(dec, 0b11111_11111));
        assertArrayEquals(test, dec);
    }

    @Test
    void decodeBits10bit1234() {
        int[] test = {1, 2, 3, 4};
        byte[] enc = LzwCodec.encodeBits(test, 10);
        int[] dec = LzwCodec.decodeBits(enc, 10);
        assertArrayEquals(test, dec);
    }

    @Test
    void decode() {
    }

    String toBits(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for(byte b : array) {
            sb.append(Integer.toBinaryString(b & 0xFF));
            sb.append(' ');
        }
        return sb.toString();
    }
    String toBits(int[] array, int mask) {
        StringBuilder sb = new StringBuilder();
        for(int i : array) {
            sb.append(Integer.toBinaryString(i & mask));
            sb.append(' ');
        }
        return sb.toString();
    }
}