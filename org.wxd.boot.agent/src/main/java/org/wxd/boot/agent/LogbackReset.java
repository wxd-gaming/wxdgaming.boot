package org.wxd.boot.agent;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.lang.Record2;

import java.io.InputStream;

/**
 * 测试logback动态配置
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-27 14:28
 **/
public class LogbackReset {

    public static void resetLogback(String userDir) throws JoranException {
        resetLogback(Thread.currentThread().getContextClassLoader(), userDir);
    }

    public static void resetLogback(ClassLoader classLoader, String userDir) throws JoranException {
        System.setProperty("LOG_PATH", "");
        // 加载logback.xml配置文件
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        if (userDir != null && !userDir.isBlank() && !userDir.isEmpty()) {
            if (!userDir.endsWith("/")) {
                userDir += "/";
            }
        }
        lc.putProperty("LOG_PATH", userDir);
        Record2<String, InputStream> inputStream = FileUtil.findInputStream(classLoader, "logback.xml");
        if (inputStream == null) {
            inputStream = FileUtil.findInputStream(classLoader, "logback-test.xml");
        }
        configurator.doConfigure(inputStream.t2());
        LoggerFactory.getLogger("root").info("--------------- init end ---------------");
    }

}
