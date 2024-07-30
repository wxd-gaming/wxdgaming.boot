package wxdgaming.boot.core.format;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ConvertUtil;
import wxdgaming.boot.core.str.StringUtil;

import java.io.Serializable;

/**
 * 时间格式化
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-11-21 17:39
 **/
@Getter
@Setter
@Accessors(chain = true)
public class TimeFormat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static enum FormatInfo {
        All(0, ""),
        Day(24f * 60 * 60 * 1000, "d"),
        Hour(60f * 60 * 1000, "h"),
        Minute(60 * 1000f, "m"),
        Second(1000f, "s"),
        MS(1f, "ms");
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

    /** 为了保留两位小数, 精度是 毫秒 * 100 */
    private long allTime;

    /**
     * 为了保留两位小数
     *
     * @param cost 精度是 毫秒 * 100
     * @return
     */
    public TimeFormat addTime(long cost) {
        allTime += cost;
        return this;
    }

    private void allInfo(StringBuilder stringBuilder) {
        long ms = allTime / 100 % 1000;

        long s = allTime / 100 / 1000 % 60;
        long m = allTime / 100 / 1000 / 60;

        long h = m / 60;

        m = m % 60;

        long d = h / 24;
        h = h % 24;
        if (d > 0) {
            stringBuilder.append(d).append(" d, ");
        }
        if (h > 0) {
            stringBuilder.append(h).append(" h, ");
        }
        if (m > 0) {
            stringBuilder.append(m).append(" m, ");
        }
        if (s > 0) {
            stringBuilder.append(s).append(" s, ");
        }
        if (ms > 0) {
            stringBuilder.append(ms).append(" ms");
        }
    }

    private void formatInfo(FormatInfo formatInfo, StringBuilder stringBuilder) {
        Object obj = allTime;
        if (formatInfo != FormatInfo.MS) {
            obj = ConvertUtil.float2(allTime / 100 / formatInfo.getCode());
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
        StringBuilder stringBuilder = new StringBuilder();
        if (formatInfo == FormatInfo.All) {
            allInfo(stringBuilder);
        } else {
            formatInfo(formatInfo, stringBuilder);
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toString(FormatInfo.All);
    }

}
