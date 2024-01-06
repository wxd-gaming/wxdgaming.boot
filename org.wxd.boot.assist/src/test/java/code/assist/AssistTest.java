package code.assist;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.assist.IAssistMonitor;
import org.wxd.boot.assist.IAssistOutFile;
import org.wxd.boot.assist.MonitorLog;

/**
 * assist 字节码测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-06 10:17
 **/
@Slf4j
public class AssistTest implements IAssistMonitor, IAssistOutFile {

    @Test
    public void at1() throws Exception {
        IAssistMonitor.THREAD_LOCAL.set(new MonitorLog());
        /**如果要使用耗时统计添加启动参数 -javaagent:..\target\libs\assist.jar=需要监控的包名 */
        new B()
                .b1()
                .a1();
        System.out.println(IAssistMonitor.THREAD_LOCAL.get());
        IAssistMonitor.THREAD_LOCAL.remove();
    }

    public interface Mylog extends IAssistMonitor {

    }

    public static class A implements Mylog, IAssistOutFile {

        public A a1() throws Exception {
            Thread.sleep(10);
            return this;
        }

        public static void a2() throws InterruptedException {
            Thread.sleep(10);
        }

    }

    public static class B extends A {

        public B b1() throws Exception {
            Thread.sleep(10);
            return this;
        }

    }

}
