package org.wxd.boot.proxycount;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.format.ByteFormat;
import org.wxd.boot.format.TimeFormat;

/**
 * 消息统计
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-08 16:13
 **/
@Getter
@Setter
@Accessors(chain = true)
public class MsgCount {

    private String name;
    /** 统计次数 */
    private int count;
    /** 统计消息执行耗时 */
    private TimeFormat execTime = new TimeFormat();
    /** 字节数 */
    private ByteFormat allBytes = new ByteFormat();

    public void addByteLen(long len, long costTime) {
        this.allBytes.addFlow(len);
        this.execTime.addTime(costTime);
        this.count++;
    }

    public int compareTo1(MsgCount o) {
        if (this.allBytes.getAllBytes() == o.getAllBytes().getAllBytes()) {
            return Integer.compare(o.count, this.count);
        }
        return Long.compare(o.getAllBytes().getAllBytes(), this.allBytes.getAllBytes());
    }

    public int compareTo2(MsgCount o) {

        long l1 = this.getExecTime().getAllTime() / count;
        long l2 = o.getExecTime().getAllTime() / o.getCount();

        if (l1 == l2) {
            return Long.compare(o.count, this.count);
        }

        return Long.compare(l2, l1);
    }

    @Override
    public String toString() {

        return String.format(
                "name = %-40s, count = %-10s, exec-timer = %-10s, bytes =  %-20s, all = %d",
                name, count, execTime.toString(TimeFormat.FormatInfo.Second), allBytes.toString(ByteFormat.FormatInfo.All), allBytes.getAllBytes()
        );

    }
}
