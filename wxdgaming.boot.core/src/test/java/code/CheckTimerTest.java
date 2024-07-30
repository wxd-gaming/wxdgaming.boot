package code;

import org.junit.Test;

/**
 * 测试时间检查
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-18 18:04
 **/
public class CheckTimerTest {

    @Test
    public void t1() {

        Runnable runnable = new Runnable() {
            @Override public void run() {

            }
        };
        Class<? extends Runnable> aClass = runnable.getClass();
        System.out.println(aClass);
    }

    public static class T {}

}
