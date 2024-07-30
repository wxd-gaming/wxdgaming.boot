package wxdgaming.boot.starter;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.starter.action.ActionConfig;
import wxdgaming.boot.starter.config.Config;
import wxdgaming.boot.starter.i.IConfigInit;

import java.util.function.Consumer;

/**
 * 基础模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
@Slf4j
@Getter
abstract class BaseModule extends AbstractModule {

    protected final ReflectContext reflectContext;
    protected final Consumer<? super BaseModule> onConfigure;

    public BaseModule(ReflectContext reflectContext, Consumer<? super BaseModule> onConfigure) {
        this.reflectContext = reflectContext;
        this.onConfigure = onConfigure;
    }

    public BaseModule bindSingleton(Class<?> clazz) {
        log.debug("bind {} clazz={}", this.hashCode(), clazz);
        bind(clazz).in(Singleton.class);
        return this;
    }

    public <T> BaseModule bindSingleton(Class<T> father, Class<? extends T> son) {
        log.debug("bind {} father={} bind son={}", this.hashCode(), father, son);
        bind(father).to(son).in(Singleton.class);
        return this;
    }

    public <B> BaseModule bindSingleton(Class<B> clazz, B instance) {
        log.debug("bind {} clazz={} bind instance={}", this.hashCode(), clazz, instance.getClass());
        bind(clazz).toInstance(instance);
        return this;
    }

    @Override
    protected final void configure() {
        binder().requireExplicitBindings();
        binder().requireExactBindingAnnotations();
        // binder().disableCircularProxies();/*禁用循环依赖*/

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

        reflectContext
                .classStream()
                .filter(c ->
                        AnnUtil.ann(c, Singleton.class) != null
                                || AnnUtil.ann(c, TextController.class) != null
                                || AnnUtil.ann(c, ProtoController.class) != null
                )
                .forEach(this::bindSingleton);

        try {
            if (onConfigure != null) {
                onConfigure.accept(this);
            }
            bind();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract BaseModule bind() throws Throwable;

}
