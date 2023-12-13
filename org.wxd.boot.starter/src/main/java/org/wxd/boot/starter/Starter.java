package org.wxd.boot.starter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.agent.exception.Throw;
import org.wxd.agent.function.ConsumerE2;
import org.wxd.agent.function.STVFunction1;
import org.wxd.agent.io.FileWriteUtil;
import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.AAAAA;
import org.wxd.boot.collection.OfSet;
import org.wxd.boot.starter.action.ActionProtoController;
import org.wxd.boot.starter.action.ActionTextController;
import org.wxd.boot.starter.action.ActionTimer;
import org.wxd.boot.starter.i.*;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.json.ProtobufMessageSerializerFastJson;
import org.wxd.boot.system.GlobalUtil;
import org.wxd.boot.system.JvmUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 启动器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 19:11
 **/
public class Starter {

    private static volatile InjectorContext mainIocInjector = null;
    private static volatile InjectorContext childIocInjector = null;

    /** 服务器启动成功 */
    private static File OKFile = new File("ok.txt");

    public static InjectorContext curIocInjector() {
        if (childIocInjector == null) return mainIocInjector;
        return childIocInjector;
    }

    public static void startBoot(Class... startClasses) {
        final String[] packages = Arrays.stream(startClasses)
                .map(v -> v.getPackage().getName())
                .toArray(String[]::new);
        startBoot(packages);
    }

    public static void startBoot(String... packages) {
        Set<String> packages1 = OfSet.asSet(packages);
        packages1.add(AAAAA.class.getPackage().getName());
        String[] array = packages1.toArray(new String[0]);
        ReflectContext.Builder reflectContext = ReflectContext.Builder.of(array);
        initBoot(reflectContext);
    }

    public static void initBoot(ReflectContext.Builder builder) {
        if (mainIocInjector != null) throw new RuntimeException("不允许第二次启动");
        JvmUtil.setLogbackConfig();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            /*全局未捕获线程异常*/
            @Override public void uncaughtException(Thread t, Throwable e) {
                try {
                    System.out.println(t);
                    e.printStackTrace(System.out);
                } catch (Throwable t0) {}
            }
        });
        try {
            ReflectContext build = builder.build();

            List<BaseModule> list = Stream.concat(
                            Stream.of(new BootStarterModule(build), new StarterModule(build)),
                            build.classWithSuper(UserModule.class).map(v -> {
                                try {
                                    return v.getDeclaredConstructor().newInstance();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    )
                    .map(v -> (BaseModule) v)
                    .toList();

            Injector injector = Guice.createInjector(Stage.PRODUCTION, list);
            mainIocInjector = injector.getInstance(InjectorContext.class);
            iocInitBean(mainIocInjector, build);
            final Logger log = LoggerFactory.getLogger(Starter.class);
            JvmUtil.addShutdownHook(() -> {
                log.info("------------------------------停服信号处理------------------------------");
                GlobalUtil.Shutting.set(true);
                {
                    STVFunction1<Object, IShutdownBefore> shutdownBefore = IShutdownBefore::shutdownBefore;
                    curIocInjector().beanStream(IShutdownBefore.class, shutdownBefore).forEach(object -> {
                        try {
                            log.info("shutdownBefore：{} {}", object.getClass(), object.toString());
                            object.shutdownBefore();
                        } catch (Throwable e) {
                            throw Throw.as(object.getClass().getName() + ".shutdown()", e);
                        }
                    });
                }
                {
                    STVFunction1<Object, IShutdown> shutdown = IShutdown::shutdown;
                    curIocInjector().beanStream(IShutdown.class, shutdown).forEach(object -> {
                        try {
                            log.info("shutdown：{} {}", object.getClass(), object.toString());
                            object.shutdown();
                        } catch (Throwable e) {
                            throw Throw.as(object.getClass().getName() + ".shutdown()", e);
                        }
                    });
                }
                {
                    STVFunction1<Object, IShutdownEnd> shutdown = IShutdownEnd::shutdownEnd;
                    curIocInjector().beanStream(IShutdownEnd.class, shutdown).forEach(object -> {
                        try {
                            log.info("shutdownEnd：{} {}", object.getClass(), object.toString());
                            object.shutdownEnd();
                        } catch (Throwable e) {
                            throw Throw.as(object.getClass().getName() + ".shutdown()", e);
                        }
                    });
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                boolean delete = OKFile.delete();
                log.info("------------------------------停服处理结束-{}------------------------------", delete);
                JvmUtil.halt(0);
            });
            log.info("主容器初始化完成：{}", mainIocInjector.hashCode());
        } catch (Throwable throwable) {
            LoggerFactory.getLogger(Starter.class).error("启动失败", throwable);
            JvmUtil.halt(-1);
        }
    }

    public static InjectorContext createChildInjector(ReflectContext reflectContext) {
        return childIocInjector = createChildInjector(mainIocInjector, reflectContext);
    }

    public static InjectorContext createChildInjector(InjectorContext parentInjector, ReflectContext reflectContext) {
        final Logger log = LoggerFactory.getLogger(Starter.class);
        try {
            List<BaseModule> modules = Stream.concat(
                            Stream.of(new StarterModule(reflectContext)),
                            reflectContext.classWithSuper(UserModule.class).map(v -> {
                                try {
                                    return v.getDeclaredConstructor().newInstance();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    )
                    .map(v -> (BaseModule) v)
                    .toList();

            Injector injector = parentInjector.getInjector();
            injector.createChildInjector(modules);
            InjectorContext iocInjector = injector.getInstance(InjectorContext.class);
            iocInitBean(iocInjector, reflectContext);
            log.info("子容器初始化完成：{}", iocInjector.hashCode());
            return iocInjector;
        } catch (Throwable throwable) {
            throw Throw.as("子容器初始化失败", throwable);
        }
    }

    static void iocInitBean(InjectorContext injector, ReflectContext reflectContext) throws Exception {
        final Logger log = LoggerFactory.getLogger(Starter.class);
        if (log.isDebugEnabled()) {
            long count = reflectContext.getClasses().size();
            log.debug("find class size ：" + count);
        }

        /*todo fastjson 注册 protoBuff 处理 处理配置类*/
        reflectContext.classStream().forEach(ProtobufMessageSerializerFastJson::action);

        /*处理定时器资源*/
        ActionTimer.action(injector, reflectContext);
        /*处理 ProtoController 资源*/
        ActionProtoController.action(injector, reflectContext);
        /*http 处理器加载*/
        ActionTextController.action(injector, reflectContext);

        /*todo 调用 init 方法 father */
        ConsumerE2<IBeanInit, InjectorContext> startFun = IBeanInit::beanInit;
        injector.beanStream(IBeanInit.class, startFun).forEach(iBeanInit -> {
            if (reflectContext.classStream().anyMatch(v -> v.equals(iBeanInit.getClass()))) {
                try {
                    iBeanInit.beanInit(injector);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                log.debug("bean init {}", iBeanInit.getClass());
            }
        });
    }

    public static void start(boolean debug, int serverId, String serverName, String... extInfos) {
        {
            ConsumerE2<IStart, InjectorContext> startFun = IStart::start;
            curIocInjector().forEachBean(IStart.class, startFun, curIocInjector());
        }
        {
            ConsumerE2<IStartEnd, InjectorContext> startEndFun = IStartEnd::startEnd;
            curIocInjector().forEachBean(IStartEnd.class, startEndFun, curIocInjector());
        }
        print(debug, serverId, serverName, extInfos);
        printOk();
    }

    /** 启动成功标记 */
    public static void printOk() {
        FileWriteUtil.writeString(OKFile, JvmUtil.processIDString());
    }

    /** 启动失败 */
    public static void printFail() {
        FileWriteUtil.writeString(OKFile, "0");
    }

    /** 删除文件 */
    public static void delOk() {
        OKFile.delete();
    }

    public static void print(boolean debug, int serverId, String serverName, String... extInfos) {
        StringBuilder stringAppend = new StringBuilder(1024);

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        stringAppend.append("\n\n")
                .append("                                               _ooOoo_\n")
                .append("                                              o8888888o\n")
                .append("                                              88\" . \"88\n")
                .append("                                              (| -_- |)\n")
                .append("                                               O\\ = /O\n")
                .append("                                           ____/`---'\\____\n")
                .append("                                         .   ' \\\\| |// `.\n")
                .append("                                          / \\\\||| W |||// \\\n")
                .append("                                        / _||||| -X- |||||- \\\n")
                .append("                                          | | \\\\\\ X /// | |\n")
                .append("                                        | \\_| ''\\-D-/'' |_/ |\n")
                .append("                                         \\ .-\\__ `" + year.charAt(0) + "` __/-. /\n")
                .append("                                      ___`. .' /--" + year.charAt(1) + "--\\ `. . __\n")
                .append("                                   .\"\" '< `.___\\_<" + year.charAt(2) + ">_/___.' >' \"\".\n")
                .append("                                  | | : `- \\`.;`\\ " + year.charAt(3) + " /`;.`/ - ` : | |\n")
                .append("                                    \\ \\ `-. \\_ __\\ /__ _/ .-` / /\n")
                .append("                            ======`-.____`-.___\\_____/___.-`____.-'======\n")
                .append("                                               `=---='\n")
                .append("                                                                         \n")
                .append("                            .............................................\n")
                .append("                                                                         \n")
                .append("------------->> [ " + StringUtil.padRight("debug = " + debug + " | " + JvmUtil.processIDString(), 80, ' ') + " ] <<-------------\n")
                .append("------------->> [ " + StringUtil.padRight(serverId + " | " + serverName, 80, ' ') + " ] <<-------------\n")
                .append("------------->> [ " + StringUtil.padRight(JvmUtil.timeZone(), 80, ' ') + " ] <<-------------\n");
        for (String extInfo : extInfos) {
            stringAppend.append("------------->> [ " + StringUtil.padRight(extInfo, 80, ' ') + " ] <<-------------\n");
        }
        stringAppend.append("\n");
        LoggerFactory.getLogger(Starter.class).warn(stringAppend.toString());
    }

}
