package org.wxd.boot.core.format;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.core.timer.MyClock;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-07-02 14:36
 **/
@Slf4j
public class NewId implements Serializable {
    /** 相当于1970年1月1日，到2022年7月1日 经过了这么多天 */
    public static final int OffSetDays = 19174;

    /** 8位 */
    public static final long Offset8 = 0xFF;
    /** 20位 */
    public static final long Offset20 = 0xFFFFF;
    public static final long Offset32 = 0xFFFFFFFFL;
    public static final long Offset35 = 0x7FFFFFFFFL;

    final long hexId;
    final long type;

    volatile long lastDays = 0;
    volatile long lastSecondByDay = 0;
    volatile long seed = 0;

    public static void main(String[] args) throws InterruptedException {

        System.out.println(Long.MAX_VALUE);
        System.out.println(Offset32);
        System.out.println((4000 << 17 | 86400));

        final NewId newId = new NewId(80001, 120);
        {
            final long id = newId.newId();
            System.out.println(id);
            System.out.println("serverId=" + newId.hexId(id));
            System.out.println("type=" + newId.type(id));
            System.out.println("idValue=" + newId.idValue(id));
        }

        Set<Long> ids = new HashSet<>();
        while (true) {
            for (int i = 0; i < 16000; i++) {
                final long id = newId.newId();
                if (!ids.add(id)) {
                    System.out.println("重复id " + id + " " + new Date());
                    System.out.println("结束 " + ids.size());
                    return;
                }
//                log.debug("{}", id);
            }
            Thread.sleep(900);
        }
    }

    public NewId(int hexId, int type) {

        if (hexId > Offset20) throw new RuntimeException("hexId 不能大于 " + Offset20);
        if (type > Offset8) throw new RuntimeException("type 不能大于 " + Offset8);

        this.hexId = hexId;
        this.type = type;

    }

    public long newId() {
        final long days = MyClock.days() - OffSetDays;
        final long secondByDay = MyClock.dayOfSecond();
        if (lastDays != days || secondByDay != lastSecondByDay) {
            seed = 0;
            lastDays = days;
            lastSecondByDay = secondByDay;
        }
        long id = (days << 17 | secondByDay) << 18;
        seed++;
        id += seed;
//        System.out.println("idValue=" + id);
        long newId = type << 55 | hexId << 35 | id;
        return newId;
    }

    public int hexId(long value) {
        return (int) (value >> 35 & Offset20);
    }

    public int type(long value) {
        return (int) (value >> 55 & Offset8);
    }

    public long idValue(long value) {
        return (value & Offset35);
    }

}
