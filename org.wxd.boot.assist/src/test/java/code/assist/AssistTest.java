package code.assist;

import code.T2;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.assist.*;

/**
 * assist 字节码测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 10:17
 **/
@Slf4j
public class AssistTest implements IAssistMonitor, IAssistOutFile {

    @Test
    @MonitorAnn(filter = true)
    public void at1() throws Exception {
        org.wxd.boot.assist.IAssistMonitor.THREAD_LOCAL.set(new MonitorRecord());
        /**如果要使用耗时统计添加启动参数 -javaagent:..\target\libs\assist.jar=需要监控的包名 */
        B b = new B();
        b.b1();
        b.a1();
        MonitorRecord x = org.wxd.boot.assist.IAssistMonitor.THREAD_LOCAL.get();
        org.wxd.boot.assist.IAssistMonitor.THREAD_LOCAL.remove();
        System.out.println(x);

    }

    @Test
    @MonitorStart
    public void at2() throws Exception {
        /**如果要使用耗时统计添加启动参数 -javaagent:..\target\libs\assist.jar=需要监控的包名 */
        new B().b1().a1();
    }

    public interface Mylog extends IAssistMonitor {

    }

    public static class A implements Mylog, IAssistOutFile {

        public A a1() throws Exception {
            a2();
            return this;
        }

        public void a2() throws InterruptedException {
            new T2().t2();
        }

    }

    public static class B extends A {

        public B b1() throws Exception {
            a1();
            return this;
        }

    }

}
