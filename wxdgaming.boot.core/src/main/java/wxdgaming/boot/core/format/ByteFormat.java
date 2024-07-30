package wxdgaming.boot.core.format;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ConvertUtil;
import wxdgaming.boot.core.str.StringUtil;

import java.io.Serializable;

/**
 * 流量统计
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-11-21 17:09
 **/
@Getter
@Setter
@Accessors(chain = true)
public class ByteFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static enum FormatInfo {
        All(0, ""),
        GB(1024f * 1024 * 1024, "G"),
        MB(1024f * 1024, "M"),
        KB(1024f, "K"),
        B(1f, "");

        private final float code;
        private final String comment;

        FormatInfo(float code, String comment) {
            this.code = code;
            this.comment = comment;
        }

        public float getCode() {
            return code;
        }

        public String getComment() {
            return comment;
        }
    }

    private final StringBuilder sb = new StringBuilder();
    /** 所有的字节 */
    private volatile long allBytes;

    public void clear() {
        allBytes = 0;
    }

    /** 所有的字节 */
    public ByteFormat addFlow(long len) {
        allBytes += len;
        return this;
    }

    private void allInfo(StringBuilder stringBuilder) {
        long b = allBytes % 1024;
        long k = allBytes / 1024;
        long m = k / 1024;
        k %= 1024;
        long g = m / 1024;
        m = m % 1024;
        if (g > 0)
            stringBuilder.append(StringUtil.padLeft(g, 4, ' ')).append(" G, ");
        if (m > 0)
            stringBuilder.append(StringUtil.padLeft(m, 4, ' ')).append(" M, ");
        if (k > 0)
            stringBuilder.append(StringUtil.padLeft(k, 4, ' ')).append(" K, ");
        if (b > 0)
            stringBuilder.append(StringUtil.padLeft(b, 4, ' ') + " B");
    }

    private void formatInfo(FormatInfo formatInfo, StringBuilder stringBuilder) {
        Object obj = allBytes;
        if (formatInfo != FormatInfo.B) {
            obj = ConvertUtil.float2(allBytes / formatInfo.getCode());
        }
        stringBuilder.append(StringUtil.padLeft(obj, 12, ' ')).append(" ").append(formatInfo.getComment());
    }

    public void toString(FormatInfo formatInfo, StringBuilder stringBuilder) {
        if (formatInfo == FormatInfo.All) {
            allInfo(stringBuilder);
        } else {
            formatInfo(formatInfo, stringBuilder);
        }
    }

    public String toString(FormatInfo formatInfo) {
        sb.setLength(0);
        if (formatInfo == FormatInfo.All) {
            allInfo(sb);
        } else {
            formatInfo(formatInfo, sb);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(FormatInfo.All);
    }
}
