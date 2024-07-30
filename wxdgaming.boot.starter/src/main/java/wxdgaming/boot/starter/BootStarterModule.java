package wxdgaming.boot.starter;

import wxdgaming.boot.agent.function.ConsumerE2;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.xml.XmlUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.starter.batis.*;
import wxdgaming.boot.starter.service.*;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 基础模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class BootStarterModule extends BaseModule {

    public BootStarterModule(ReflectContext reflectContext, Consumer<? super BaseModule> onConfigure) {
        super(reflectContext, onConfigure);
    }

    protected BootStarterModule bind() throws Throwable {
        Record2<String, InputStream> inputStream = FileUtil.findInputStream(this.getClass().getClassLoader(), "boot.xml");
        System.out.println("读取文件目录：" + inputStream.t1());
        BootConfig bootConfig = XmlUtil.fromXml(inputStream.t2(), BootConfig.class);

        JvmUtil.setProperty(JvmUtil.Default_Executor_Core_Size, bootConfig.getDefaultExecutor().getCoreSize());
        JvmUtil.setProperty(JvmUtil.Default_Executor_Max_Size, bootConfig.getDefaultExecutor().getMaxSize());

        JvmUtil.setProperty(JvmUtil.VT_Executor_Core_Size, bootConfig.getVtExecutor().getCoreSize());
        JvmUtil.setProperty(JvmUtil.VT_Executor_Max_Size, bootConfig.getVtExecutor().getMaxSize());

        JvmUtil.setProperty(JvmUtil.Logic_Executor_Core_Size, bootConfig.getLogicExecutor().getCoreSize());
        JvmUtil.setProperty(JvmUtil.Logic_Executor_Max_Size, bootConfig.getLogicExecutor().getMaxSize());

        ConsumerE2<Class, TcpConfig> action = (aClass, config) -> {
            if (config.getPort() > 0) {
                if (StringUtil.notEmptyOrNull(config.getServiceClassName())) {
                    /*通过指定的类进行加载*/
                    aClass = BootStarterModule.class.getClassLoader().loadClass(config.getServiceClassName());
                }
                Object newInstance = aClass.getDeclaredConstructor(config.getClass()).newInstance(config);
                if (StringUtil.emptyOrNull(config.getName())) {
                    config.setName(newInstance.getClass().getSimpleName());
                }
                bindSingleton(aClass, newInstance);
            }
        };

        ConsumerE2<Class, DbConfig> dbAction = (aClass, config) -> {
            if (config.getDbPort() > 0) {
                Object newInstance = aClass.getDeclaredConstructor(config.getClass()).newInstance(config);
                if (StringUtil.emptyOrNull(config.getName())) {
                    config.setName(newInstance.getClass().getSimpleName());
                }
                bindSingleton(aClass, newInstance);
            }
        };

        {
            action.accept(TsService.class, bootConfig.getTcpSocket());
            action.accept(TsService1.class, bootConfig.getTcpSocket1());
            action.accept(TsService2.class, bootConfig.getTcpSocket2());
            action.accept(TsService3.class, bootConfig.getTcpSocket3());
        }
        {
            action.accept(HsService.class, bootConfig.getHttp());
            action.accept(HsService1.class, bootConfig.getHttp1());
            action.accept(HsService2.class, bootConfig.getHttp2());
            action.accept(HsService3.class, bootConfig.getHttp3());
        }
        {
            action.accept(WsService.class, bootConfig.getWebSocket());
            action.accept(WsService1.class, bootConfig.getWebSocket1());
            action.accept(WsService2.class, bootConfig.getWebSocket2());
            action.accept(WsService3.class, bootConfig.getWebSocket3());
        }
        {
            dbAction.accept(MysqlService.class, bootConfig.getMysql());
            dbAction.accept(MysqlService1.class, bootConfig.getMysql1());
            dbAction.accept(MysqlService2.class, bootConfig.getMysql2());
            dbAction.accept(MysqlService3.class, bootConfig.getMysql3());
        }
        {
            dbAction.accept(MongoService.class, bootConfig.getMongodb());
            dbAction.accept(MongoService1.class, bootConfig.getMongodb1());
            dbAction.accept(MongoService2.class, bootConfig.getMongodb2());
            dbAction.accept(MongoService3.class, bootConfig.getMongodb3());
        }
        {
            dbAction.accept(RedisService.class, bootConfig.getRedis());
            dbAction.accept(RedisService1.class, bootConfig.getRedis1());
            dbAction.accept(RedisService2.class, bootConfig.getRedis2());
            dbAction.accept(RedisService3.class, bootConfig.getRedis3());
        }
        bindSingleton(BootConfig.class, bootConfig);
        bindSingleton(IocMainContext.class);
        bindSingleton(ScheduledService.class);
        return this;
    }


}
