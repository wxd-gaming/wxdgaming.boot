package wxdgaming.boot.core.lang.bit;

import lombok.Getter;
import wxdgaming.boot.core.str.StringUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 位标记
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-11 12:08
 **/
@Getter
public class BitFlag implements Serializable {

    /*
    & 真真的真，两个都是1就是1，
    | 有一个真就是真，
    ~ 位取反 0变1 1变0
    ^ 一真一假得假，0和1得1，
     */
    private long[] longs = null;

    public BitFlag() {

    }

    /**
     * 构建flag的长度
     *
     * @param sizeLong 你需要几个long来存储
     */
    public BitFlag(int sizeLong) {
        checkBounds(sizeLong - 1);
    }

    public BitFlag(long[] longs) {
        this.longs = longs;
    }

    /**
     * @param sizeLong 你需要几个long来存储
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-06-13 15:20
     */
    protected void checkBounds(int sizeLong) {
        sizeLong++;
        if (longs == null) {
            longs = new long[sizeLong];
        } else if (longs.length < sizeLong) {
            longs = Arrays.copyOf(this.longs, sizeLong);
        }
    }

    /** 增加多个标记位 */
    public BitFlag addFlags(int... indexes) {
        for (int index : indexes) {
            addFlag(index);
        }
        return this;
    }

    /**
     * @param index 位，从1开始，计算的时候会-1
     * @return
     */
    public BitFlag addFlag(int index) {
        if (index < 1) {
            throw new UnsupportedOperationException("index 大于 0");
        }
        final int flag1 = (index - 1) % 64;
        addFlag(index, 1L << flag1);
        return this;
    }

    /** 追加覆盖 */
    public BitFlag addFlag(int index, long value) {
        if (index < 1) {
            throw new UnsupportedOperationException("index 大于 0");
        }
        index--;
        final int flag0 = index / 64;
        checkBounds(flag0);
        longs[flag0] = longs[flag0] | value;
        return this;
    }

    /**
     * 标记一个范围
     *
     * @param start
     * @param end
     * @return
     */
    public BitFlag addFlagRange(int start, int end) {
        if (end < start || start < 0)
            throw new UnsupportedOperationException("参数不合法 start = " + start + ", end = " + end);
        for (int i = start; i <= end; i++) {
            addFlag(i);
        }
        return this;
    }

    /**
     * 分组标记位, 会先移除分组内标记，然后重新添加新的标记
     *
     * @param groupStart
     * @param groupEnd
     * @param index
     * @return
     */
    public BitFlag addFlagRange(int groupStart, int groupEnd, int index) {
        if (groupEnd < groupStart || groupStart < 0)
            throw new UnsupportedOperationException("参数不合法 groupStart = " + groupStart + ", groupEnd = " + groupEnd);
        removeFlagRange(groupStart, groupEnd);
        addFlag(index);
        return this;
    }


    /** 移除分组标记，通常是连续位 */
    public BitFlag removeFlagRange(int groupStart, int groupEnd) {
        if (groupEnd < groupStart || groupStart < 0)
            throw new UnsupportedOperationException("参数不合法 groupStart = " + groupStart + ", groupEnd = " + groupEnd);
        for (int i = groupStart; i < groupEnd; i++) {
            removeFlag(i);
        }
        return this;
    }

    /** 移除多个标记 */
    public BitFlag removeFlags(int... indexes) {
        for (int index : indexes) {
            removeFlag(index);
        }
        return this;
    }

    /** 移除一个标记 */
    public BitFlag removeFlag(int index) {
        if (index < 1) {
            throw new UnsupportedOperationException("index 大于 0");
        }
        final int flag1 = (index - 1) % 64;
        removeFlag(index, 1L << flag1);
        return this;
    }

    /**
     * 移除一个标记
     *
     * @param index
     * @param value 会对参数进行取反操作
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-06-13 14:09
     */
    public BitFlag removeFlag(int index, long value) {
        if (index < 1) {
            throw new UnsupportedOperationException("index 大于 0");
        }
        index--;
        final int flag0 = index / 64;
        checkBounds(flag0);
        longs[flag0] = longs[flag0] & ~value;
        return this;
    }


    /** 分组范围内，全部为真 */
    public boolean hasFlags(int... indexs) {
        for (int i : indexs) {
            if (!hasFlag(i)) {
                return false;
            }
        }
        return true;
    }

    /** 分组范围内，有一个是真 即为真 */
    public boolean hasAnyFlags(int... indexs) {
        for (int i : indexs) {
            if (hasFlag(i)) {
                return true;
            }
        }
        return false;
    }

    /** 分组范围内，有一个是真 即为真 */
    public boolean hasFlagRange(int groupStart, int groupEnd) {
        if (groupEnd < groupStart || groupStart < 0)
            throw new UnsupportedOperationException("参数不合法 groupStart = " + groupStart + ", groupEnd = " + groupEnd);
        for (int i = groupEnd; i <= groupStart; i++) {
            if (hasFlag(i)) {
                return true;
            }
        }
        return false;
    }

    /** 是否包含一个标记 */
    public boolean hasFlag(int index) {
        if (index < 1) {
            throw new UnsupportedOperationException("index 大于 0");
        }
        index--;
        final int flag0 = index / 64;
        final int flag1 = index % 64;
        return hasFlag(flag0, flag1);
    }

    /** 是否包含一个标记 */
    protected boolean hasFlag(int flag0, int flag1) {
        checkBounds(flag0);
        return (longs[flag0] & (1L << flag1)) != 0;
    }

    /** 计算范围内标记数量 */
    public int flagCount(int groupStart, int groupEnd) {
        int count = 0;
        for (int i = groupStart; i <= groupEnd; i++) {
            if (hasFlag(i)) {
                count++;
            }
        }
        return count;
    }

    /** 统计 */
    public int flagCount() {
        int count = 0;
        for (int flag0 = 0; flag0 < longs.length; flag0++) {
            for (int flag1 = 0; flag1 < 64; flag1++) {
                if (hasFlag(flag0, flag1)) {
                    count++;
                }
            }
        }
        return count;
    }

    /** {@code System.out.println(toString()) } */
    public BitFlag pint() {
        System.out.println(toString());
        return this;
    }

    public BitFlag clear() {
        longs = null;
        return this;
    }

    /**
     * 重置当前对象
     *
     * @param sizeLong 你需要几个long来存储
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-06-13 15:19
     */
    public BitFlag reset(int sizeLong) {
        longs = null;
        checkBounds(sizeLong - 1);
        return this;
    }

    public BitFlag setLongs(long[] longs) {
        this.longs = longs;
        return this;
    }

    @Override
    public String toString() {
        String show = "";
        if (longs != null) {
            for (Long aLong : longs) {
                show = toBinaryString("", StringUtil.padLeft(Long.toBinaryString(aLong), 64, '0'))
                        + (StringUtil.emptyOrNull(show) ? "" : "_")
                        + show;
            }
        }
        return "0b" + show;
    }

    public static String toString(byte i) {
        final StringBuilder sb = new StringBuilder("0b");
        for (int j = 0; j < 8; j++) {
            sb.append((i & 1 << j) != 0 ? 1 : 0);
        }
        return sb.toString();
    }

    public static String toString(short i) {
        String string = StringUtil.padLeft(Integer.toBinaryString(i), 16, '0');
        return toBinaryString("0b", string);
    }

    public static String toString(int i) {
        String string = StringUtil.padLeft(Integer.toBinaryString(i), 32, '0');
        return toBinaryString("0b", string);
    }

    public static String toString(long l) {
        String string = StringUtil.padLeft(Long.toBinaryString(l), 64, '0');
        return toBinaryString("0b", string);
    }

    private static String toBinaryString(String start, String str) {
        StringBuilder stringBuilder = new StringBuilder(start);
        for (int i = 1; i <= str.length(); i++) {
            stringBuilder.append(str.charAt(i - 1));
            if (i > 1 && i < str.length() && i % 4 == 0) {
                stringBuilder.append("_");
            }
        }
        return stringBuilder.toString();
    }

}
