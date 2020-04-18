package com.onecoder.device;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void byteEx() {
        System.out.println("bigEndian v:" + byteArrayToLong(true, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00,
                (byte) 0x3c, (byte) 0x33,
                (byte) 0x60, (byte) 0x85));

        System.out.println("littleEndian v:" + byteArrayToLong(false,
                (byte) 0x85,
                (byte) 0x60,
                (byte) 0x33,
                (byte) 0x3c
        ));
    }

    /**
     * byte数组转为long类型
     *
     * @param bytes
     * @return
     */
    public static long byteArrayToLong(boolean bigEndian, byte... bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        //由高位到低位
        int length = bytes.length;
        int offsetByteCntMax = length - 1;
        long value = 0;

        for (int i = 0; i < length; i++) {
            value += (bytes[i] & 0xff) << ((bigEndian ? (offsetByteCntMax - i) : i) * 8);
        }
        return value;
    }

}