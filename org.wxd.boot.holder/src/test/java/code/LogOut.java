package code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.LogbackUtil;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-27 15:37
 **/
public class LogOut {

    public static void main(String[] args) throws Exception {
        LogbackUtil.resetLogback(LogOut.class.getClassLoader(), args[0]);
        System.out.println(System.getProperty("user.s"));
        Logger root = LoggerFactory.getLogger("root");
        for (int i = 0; i < 10; i++) {
            root.info("{}", args[0]);
            Thread.sleep(1000);
        }

    }

}
