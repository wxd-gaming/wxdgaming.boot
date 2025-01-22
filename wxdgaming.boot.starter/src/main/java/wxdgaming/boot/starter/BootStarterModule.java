package wxdgaming.boot.starter;

import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.str.xml.XmlUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.starter.service.*;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 进程启动的时候用的模块管理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class BootStarterModule extends BaseModule<BootStarterModule> {

    public BootStarterModule(ReflectContext reflectContext, Consumer<BootStarterModule> onConfigure) {
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


        {
            socketAction.accept(TsService.class, bootConfig.getTcpSocket());
            socketAction.accept(TsService1.class, bootConfig.getTcpSocket1());
            socketAction.accept(TsService2.class, bootConfig.getTcpSocket2());
            socketAction.accept(TsService3.class, bootConfig.getTcpSocket3());
        }
        {
            socketAction.accept(HsService.class, bootConfig.getHttp());
            socketAction.accept(HsService1.class, bootConfig.getHttp1());
            socketAction.accept(HsService2.class, bootConfig.getHttp2());
            socketAction.accept(HsService3.class, bootConfig.getHttp3());
        }
        {
            socketAction.accept(WsService.class, bootConfig.getWebSocket());
            socketAction.accept(WsService1.class, bootConfig.getWebSocket1());
            socketAction.accept(WsService2.class, bootConfig.getWebSocket2());
            socketAction.accept(WsService3.class, bootConfig.getWebSocket3());
        }

        bindSingleton(BootConfig.class, bootConfig);
        bindSingleton(IocMainContext.class);
        bindSingleton(ScheduledService.class);
        return this;
    }


}
