package code;

import org.junit.Test;
import wxdgaming.boot.core.format.data.Data2Json;

/**
 * 测试日志记录
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-08-08 12:47
 **/
public class TRecord {
    public record T1(int i1, long l2) implements Data2Json {}


    @Test
    public void t1() {
        Record t1 = new T1(1, 1);
        System.out.println(t1);
    }
}
