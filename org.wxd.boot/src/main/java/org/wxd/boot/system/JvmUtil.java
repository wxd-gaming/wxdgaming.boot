package org.wxd.boot.system;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.str.StringUtil;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-02 15:07
 **/
public class JvmUtil {

    /** 强制设置logback 配置目录 */
    public static void setLogbackConfig() {
        String key = "logback.configurationFile";
        if (System.getProperty(key) == null) {
            File path = FileUtil.findFile("logback.xml");
            if (path != null && !(path.getPath().contains("jar") && path.getPath().contains("!"))) {
                /*强制设置logback的目录位置*/
                setProperty(key, FileUtil.getCanonicalPath(path));
                System.out.println("logback configuration " + FileUtil.getCanonicalPath(path));
            }
        }
    }

    /** 重设日志级别 */
    public static String refreshLoggerLevel() {
        Level lv;
        Logger root = LoggerFactory.getLogger("root");
        if (root.isDebugEnabled()) {
            lv = Level.INFO;
        } else {
            lv = Level.DEBUG;
        }
        refreshLoggerLevel("", lv);
        return lv.toString();
    }

    /** 重设日志级别 */
    public static void refreshLoggerLevel(Level loggerLevel) {
        refreshLoggerLevel("", loggerLevel);
    }

    /**
     * 重设日志级别
     *
     * @param loggerPackage
     * @param loggerLevel
     */
    public static void refreshLoggerLevel(String loggerPackage, Level loggerLevel) {
        // #1.get logger context
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // #2.filter the Logger object
        loggerContext.getLoggerList()
                .stream()
                .filter(a -> StringUtil.emptyOrNull(loggerPackage) || a.getName().startsWith(loggerPackage))
                .forEach((logger) -> logger.setLevel(loggerLevel));
    }

    /** 默认线程池的，初始数量 */
    public static final String Default_Executor_Core_Size = "default.executor.core.size";
    /** 默认线程池的，最大线程数量 */
    public static final String Default_Executor_Max_Size = "default.executor.max.size";
    /** 业务逻辑 */
    public static final String Logic_Executor_Core_Size = "logic.executor.core.size";
    /** 业务逻辑 */
    public static final String Logic_Executor_Max_Size = "logic.executor.max.size";

    public static final String Netty_Boss_Thread_Size = "netty.boss.thread.size";
    public static final String Netty_Work_Thread_Size = "netty.work.thread.size";
    public static final String Netty_Idle_Time_Server = "netty.idle.time.server";
    public static final String Netty_Idle_Time_Client = "netty.idle.time.client";
    public static final String Netty_Idle_Time_Http_Server = "netty.idle.time.http.server";
    public static final String Netty_Idle_Time_Http_Client = "netty.idle.time.http.client";
    public static final String Netty_Idle_Time_Ws_Server = "netty.idle.time.ws.server";
    public static final String Netty_Idle_Time_Ws_Client = "netty.idle.time.ws.client";

    public static final String Netty_Debug_Logger = "netty.debug.logger";

    public static String processIDString = null;
    public static Integer processIDInt = null;

    public static void init() {

        setProperty("sun.stdout.encoding", "utf-8");
        setProperty("sun.stderr.encoding", "utf-8");
        setProperty("sun.jnu.encoding", "utf-8");
        setProperty("file.encoding", "utf-8");
        /** ssl bug */
        setProperty("javax.net.ssl.sessionCacheSize", "2");

        initProcessID();
    }

    public static <R> R getProperty(String key, R defaultValue, Function<String, R> convert) {
        String property = System.getProperty(key);
        if (property != null) {
            if (convert != null) {
                return convert.apply(property);
            } else {
                return (R) property;
            }
        }
        return defaultValue;
    }

    /**
     * 设置配置
     */
    public static String setProperty(String key, Object value) {
        return System.setProperty(key, String.valueOf(value));
    }

    /**
     * 获取当前进程的进程号
     */
    private static void initProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        processIDString = runtimeMXBean.getName().split("@")[0];
        processIDInt = Integer.parseInt(processIDString);
    }

    /** 获取程序启动的进程号 */
    public static int processID() {
        if (processIDInt == null) {
            initProcessID();
        }
        return processIDInt;
    }

    /** 获取程序启动的进程号 */
    public static String processIDString() {
        if (processIDString == null) {
            initProcessID();
        }
        return processIDString;
    }


    /**
     * 强制结束进程
     * <p> {@link Runtime#getRuntime()#halt(int)}
     * <p>调用这个方法不会触发jvm退出消息钩子
     *
     * @param status 状态码
     */
    public static void halt(int status) {
        for (int kk = 3; kk >= 1; kk--) {
            System.out.println("进程退出倒计时：" + kk + " 秒");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        System.out.println("进程退出：" + status);
        Runtime.getRuntime().halt(status);
    }

    /** 添加jvm退出信号消息钩子 */
    public static void addShutdownHook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }

    /** 清理配置 */
    public static String clearProperty(String key) {
        return System.clearProperty(key);
    }

    /** 查看所有的系统配置 */
    public static void showProperties() {
        System.out.println(properties());
    }

    /**
     * 查看所有的系统配置
     */
    public static String properties() {
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<Object, Object> treeMap = new TreeMap<>(System.getProperties());
        for (Map.Entry<Object, Object> entry : treeMap.entrySet()) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        return stringBuilder.toString();
    }

    /**
     * 采用 GMT时区设置
     */
    public final static TimeZone setTimeZone(String zoneId) {
        if (StringUtil.emptyOrNull(zoneId)) {
            throw new RuntimeException("zoneId = " + zoneId);
        }
        TimeZone timeZone = TimeZone.getTimeZone(zoneId);
        TimeZone.setDefault(timeZone);
        System.setProperty("user.timezone", timeZone.getID());
        System.out.println("user.timezone = " + timeZone());
        return timeZone;
    }

    /** 当前程序运行时区 */
    public final static String timeZone() {
        return System.getProperty("user.timezone");
    }

    /** 判断是不是linux系统 */
    public static boolean isLinuxOs() {
        return !osName().toLowerCase().startsWith("win");
    }

    public static String osName() {
        return System.getProperty("os.name");
    }

    public static String osVersion() {
        return System.getProperty("os.version");
    }

    public static String userHome() {
        return System.getProperty("user.dir");
    }

    public static String jdkHome() {
        return System.getProperty("java.ext.dirs");
    }

    public static String jreHome() {
        return System.getProperty("java.home");
    }

    public static String javaClassPath() {
        return System.getProperty("java.class.path");
    }

    /**
     * ssl Session 缓存数量
     */
    public static String sslSessionCacheSize() {
        return System.getProperty("javax.net.ssl.sessionCacheSize");
    }
}
