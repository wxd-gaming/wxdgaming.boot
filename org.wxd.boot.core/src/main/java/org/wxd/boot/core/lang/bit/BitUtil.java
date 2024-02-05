package org.wxd.boot.core.lang.bit;


import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class BitUtil {

    /**
     * 将int转为低字节在前，高字节在后的byte数组
     *
     * @param bytes
     * @return
     */
    public static byte[] reverseBytes(byte[] bytes) {
        int len = bytes.length;
        byte[] b = new byte[len];
        for (int i = len - 1; i >= 0; i--) {
            b[len - i - 1] = bytes[i];
        }
        return b;
    }

    /** 端序换算 */
    public static short get2Bytes(byte[] from, int fromIndex) {
        int high = from[fromIndex] & 0xff;
        int low = from[fromIndex + 1] & 0xff;
        return (short) (high << 8 + low);
    }

    /** 端序换算 */
    public static byte[] short2Bytes(short x) {
        return short2Bytes(x, ByteOrder.BIG_ENDIAN);
    }

    /** 端序换算 */
    public static byte[] short2Bytes(short x, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(byteOrder);
        buffer.putShort(x);
        return buffer.array();
    }

    /** 端序换算 */
    public static byte[] int2Bytes(int x, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(byteOrder);
        buffer.putInt(x);
        return buffer.array();
    }

    /** 端序换算 */
    public static byte[] long2Bytes(long x, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(byteOrder);
        buffer.putLong(x);
        return buffer.array();
    }

    /** 端序换算 */
    public static short bytes2Short(byte[] src, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.wrap(src);
        buffer.put(src);
        buffer.order(byteOrder);
        return buffer.getShort();
    }

    /** 端序换算 */
    public static int bytes2Int(byte[] src, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.wrap(src);
        buffer.put(src);
        buffer.order(byteOrder);
        return buffer.getInt();
    }

    /** 端序换算 */
    public static long bytes2Long(byte[] src, ByteOrder byteOrder) {
        ByteBuffer buffer = ByteBuffer.wrap(src);
        buffer.order(byteOrder);
        return buffer.getLong();
    }

    /** 转化字符串 */
    public static String bytesToStr(byte[] target) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < target.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(target[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public static int merge32(short head, short tail) {
        return leftShift(head, 16) | tail;
    }

    public static int merge32(int head, int tail) {
        return merge32((short) head, (short) tail);
    }

    public static long merge64(int head, int tail) {
        return (long) tail | (long) head << 32;
    }

    public static long merge64(int head, long tail) {
        if (head > 920000) throw new RuntimeException("head 超过最大值 922337");
        if (tail > 2036854775807L) throw new RuntimeException("tail 超过最大值 2036854775807L");
        return head * 10000000000000L + tail;
    }

    public static int leftShift(short v, int shift) {
        Preconditions.checkArgument(shift > 0);
        Preconditions.checkArgument(shift < 32);
        return v << shift;
    }

    public static int leftShift1(int shift) {
        Preconditions.checkArgument(shift >= 0);
        Preconditions.checkArgument(shift < 32);
        return 1 << shift;
    }

    public static short high(int v) {
        return (short) (v >> 16 & '\uffff');
    }

    public static short low(int v) {
        return (short) (v & '\uffff');
    }

    public static int value(int v, int min, int max) {
        return v > max ? max : (Math.max(v, min));
    }

    public static void main(String[] args) {
        System.out.println(merge64(300001, Integer.MAX_VALUE * 1L));
    }

}
