package logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;

import java.io.InputStream;

/**
 * 测试logback动态配置
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-01-27 14:28
 **/
@Slf4j
public class LogTest {

    public static void main(String[] args) throws Exception {
        log.info("l0");
        resetLogback("s1");
        log.info("l1");
        LoggerFactory.getLogger("root").info("s1");
        resetLogback("s2");
        log.info("l2");
        LoggerFactory.getLogger("root").info("s2");
        System.in.read();
    }

    public static void resetLogback(String sid) throws JoranException {
        System.setProperty("LOG_PATH", "0/");
        // 加载logback.xml配置文件
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        if (!sid.endsWith("/")) {
            sid += "/";
        }
        lc.putProperty("SID", sid);
        Record2<String, InputStream> inputStream = FileUtil.findInputStream("logback.xml");
        if (inputStream == null) {
            inputStream = FileUtil.findInputStream("logback-test.xml");
        }
        configurator.doConfigure(inputStream.t2());
        LoggerFactory.getLogger("root").info("--------------- init end ---------------");
    }

}
