package wxdgaming.boot.starter;

import wxdgaming.boot.agent.system.ReflectContext;

import java.util.function.Consumer;

/**
 * 服务器容器模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
public abstract class ServiceModule extends BaseModule<ServiceModule> {

    public ServiceModule(ReflectContext reflectContext) {
        super(reflectContext, null);
    }

    public ServiceModule(ReflectContext reflectContext, Consumer<ServiceModule> onConfigure) {
        super(reflectContext, onConfigure);
    }

    @Override protected void configure() {
        try {
            bind();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
