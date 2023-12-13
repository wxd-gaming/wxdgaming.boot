package org.wxd.boot.starter;

import org.wxd.agent.function.ConsumerE2;
import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.starter.batis.*;
import org.wxd.boot.starter.service.*;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.xml.XmlUtil;
import org.wxd.boot.system.JvmUtil;

/**
 * 基础模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class BootStarterModule extends SystemModule {

    final ReflectContext reflectContext;

    public BootStarterModule(ReflectContext reflectContext) {
        this.reflectContext = reflectContext;
    }

    protected void bind() throws Exception {
        BootConfig bootConfig = XmlUtil.fromXml4File("boot.xml", BootConfig.class);

        JvmUtil.setProperty(JvmUtil.Default_Executor_Core_Size, bootConfig.getDefaultExecutor().getCoreSize());
        JvmUtil.setProperty(JvmUtil.Default_Executor_Max_Size, bootConfig.getDefaultExecutor().getMaxSize());

        JvmUtil.setProperty(JvmUtil.Logic_Executor_Core_Size, bootConfig.getLogicExecutor().getCoreSize());
        JvmUtil.setProperty(JvmUtil.Logic_Executor_Max_Size, bootConfig.getLogicExecutor().getMaxSize());

        ConsumerE2<Class, ServerConfig> action = (aClass, o) -> {
            if (o.getPort() > 0) {
                if (StringUtil.emptyOrNull(o.getName())) {
                    o.setName(MappingFactory.FINAL_DEFAULT);
                }
                Object newInstance = aClass.getDeclaredConstructor(o.getClass()).newInstance(o);
                bindSingleton(aClass, newInstance);
            }
        };

        ConsumerE2<Class, DbConfig> dbAction = (aClass, o) -> {
            if (o.getDbPort() > 0) {
                if (StringUtil.emptyOrNull(o.getName())) {
                    o.setName(MappingFactory.FINAL_DEFAULT);
                }
                Object newInstance = aClass.getDeclaredConstructor(o.getClass()).newInstance(o);
                bindSingleton(aClass, newInstance);
            }
        };

        {
            action.accept(TsService.class, bootConfig.getServer());
            action.accept(TsService1.class, bootConfig.getServer1());
            action.accept(TsService2.class, bootConfig.getServer2());
            action.accept(TsService3.class, bootConfig.getServer3());
        }
        {
            action.accept(HsService.class, bootConfig.getHttp());
            action.accept(HsService1.class, bootConfig.getHttp1());
            action.accept(HsService2.class, bootConfig.getHttp2());
            action.accept(HsService3.class, bootConfig.getHttp3());
        }
        {
            action.accept(WsService.class, bootConfig.getWsserver());
            action.accept(WsService1.class, bootConfig.getWsserver1());
            action.accept(WsService2.class, bootConfig.getWsserver2());
            action.accept(WsService3.class, bootConfig.getWsserver3());
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
        bindSingleton(InjectorContext.class);
        bindSingleton(ScheduledService.class);
    }


}
