package org.wxd.boot.starter;

import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.starter.service.HsService;
import org.wxd.boot.starter.service.ScheduledService;
import org.wxd.boot.starter.service.TsService;
import org.wxd.boot.starter.service.WsService;
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

        if (bootConfig.getServer().getPort() > 0) {
            TsService tcpServer = new TsService(bootConfig.getServer());
            bindSingleton(TsService.class, tcpServer);
        }

        if (bootConfig.getHttp().getPort() > 0) {
            HsService hsService = new HsService(bootConfig.getHttp());
            bindSingleton(HsService.class, hsService);
        }

        if (bootConfig.getWsserver().getPort() > 0) {
            WsService wsService = new WsService(bootConfig.getWsserver());
            bindSingleton(WsService.class, wsService);
        }

        bindSingleton(InjectorContext.class);
        bindSingleton(ScheduledService.class);
    }


}
