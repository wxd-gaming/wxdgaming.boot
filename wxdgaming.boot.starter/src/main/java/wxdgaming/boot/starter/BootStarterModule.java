package wxdgaming.boot.starter;

import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.starter.action.ActionConfig;
import wxdgaming.boot.starter.config.Config;
import wxdgaming.boot.starter.i.IConfigInit;
import wxdgaming.boot.starter.net.filter.HttpFilter;
import wxdgaming.boot.starter.net.filter.ProtoFilter;
import wxdgaming.boot.starter.net.filter.RpcFilter;
import wxdgaming.boot.starter.service.*;

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

    @Override protected void configure() {
        super.configure();
        reflectContext.withAnnotated(Config.class).forEach(content -> {
            try {
                Object o = ActionConfig.action(content.getCls());
                if (o != null) {
                    if (o instanceof IConfigInit) {
                        ((IConfigInit) o).configInit();
                    }
                    Class clazz = o.getClass();
                    bindSingleton(clazz, o);
                }
            } catch (Throwable throwable) {
                throw Throw.as(throwable);
            }
        });
    }

    protected BootStarterModule bind() throws Throwable {

        BootConfig bootConfig = BootConfig.getInstance();

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

        /* TODO 添加 aop 拦截器 */
        MappingFactory.HttpMappingSubmitBefore = (event) -> {
            return AppContext.context()
                    .beanStream(HttpFilter.class)
                    .allMatch(filter -> filter.doFilter(event));
        };

        /* TODO 添加 aop 拦截器 */
        MappingFactory.RPCMappingSubmitBefore = (event) -> {
            return AppContext.context()
                    .beanStream(RpcFilter.class)
                    .allMatch(filter -> filter.doFilter(event));
        };

        /* TODO 添加 aop 拦截器 */
        MappingFactory.ProtoMappingSubmitBefore = (event) -> {
            return AppContext.context()
                    .beanStream(ProtoFilter.class)
                    .allMatch(filter -> filter.doFilter(event));
        };

        bindSingleton(BootConfig.class, bootConfig);
        bindSingleton(IocMainContext.class);
        bindSingleton(ScheduledService.class);
        return this;
    }


}
