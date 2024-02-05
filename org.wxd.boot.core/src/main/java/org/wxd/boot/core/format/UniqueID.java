package org.wxd.boot.core.format;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.wxd.boot.core.lang.ObjectBase;
import org.wxd.boot.core.timer.MyClock;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 唯一编号生成器
 *
 * @author hank
 */
public final class UniqueID {

    /** 相当于1970年1月1日，到2022年7月1日 经过了这么多天 */
    public static final int OffSetDays = 19450;
    /** 1048575 */
    public static final long Head_Max = 0XFFFFF;
    /** 自增最大id 8796093022207 */
    public static final long Uid_Increment_Max = 0X7FFFFFFFFFFL;
    /** 表示位移数量 */
    public static final int BitLen = 43;

    private UniqueID() {}

    /**
     * 生成全球唯一Id
     *
     * @return
     */
    public static String UUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /** 从uid中还原server id */
    public static int head(long uid) {
        return (int) (uid >> BitLen);
    }

    /** 自己做数据库存储id种子 */

    @Getter
    public static class Uid extends ObjectBase {

        private final AtomicLong origin;

        public Uid(AtomicLong origin) {
            this.origin = origin;
        }

        public Uid(long origin) {
            this.origin = new AtomicLong(origin);
        }

        public long next(long head) {

            Preconditions.checkArgument(
                    1 <= head && head < Head_Max,
                    "1<= head <" + Head_Max
            );

            if (origin.get() >= Uid_Increment_Max) {
                throw new RuntimeException("id 超出界限 " + origin.get());
            }

            long l = head << BitLen | origin.incrementAndGet();
            if (l >> BitLen != head) {
                throw new RuntimeException("反解析 head 异常 id 超出界限 " + l + ", " + origin.get());
            }

            if (l <= 0)
                throw new RuntimeException("id 超出界限 " + l + ", " + origin.get());

            return l;
        }

    }

    public static class HeadUid extends Uid {
        private final int head;

        public HeadUid(AtomicLong origin, int head) {
            super(origin);
            this.head = head;
        }

        public HeadUid(long origin, int head) {
            super(origin);
            this.head = head;
        }

        public long next() {
            return super.next(head);
        }
    }

    /** 根据时间做uid */
    public static class TimeUid extends Uid {

        public TimeUid() {
            super(seed());
        }

    }

    private static AtomicLong seed() {
        long days = MyClock.days(MyClock.millis());
        days -= OffSetDays;
        final long i = MyClock.getHour() * 3600L + MyClock.getMinute() * 60L + MyClock.getSecond();
        long l = days << 10;
        long id = ((l | i) << 18) * 100;
        return new AtomicLong(id);
    }

}
