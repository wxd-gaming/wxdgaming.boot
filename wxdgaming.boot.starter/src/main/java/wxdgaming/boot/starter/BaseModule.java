package wxdgaming.boot.starter;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.ConsumerE2;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DbConfig;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.TextController;

import java.util.function.Consumer;

/**
 * 基础模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
@Slf4j
@Getter
abstract class BaseModule<T extends BaseModule> extends AbstractModule {

    protected final ReflectContext reflectContext;
    protected final Consumer<T> onConfigure;

    public BaseModule(ReflectContext reflectContext, Consumer<T> onConfigure) {
        this.reflectContext = reflectContext;
        this.onConfigure = onConfigure;
    }

    public void bindSingleton(Class<?> clazz) {
        log.debug("bind {} {} clazz={}", this.getClass().getName(), this.hashCode(), clazz);
        bind(clazz).in(Singleton.class);
    }

    public <R> void bindSingleton(Class<R> father, Class<? extends R> son) {
        log.debug("bind {} {} father={} bind son={}", this.getClass().getName(), this.hashCode(), father, son);
        bind(father).to(son).in(Singleton.class);
    }

    public <B> void bindSingleton(Class<B> clazz, B instance) {
        log.debug("bind {} {} clazz={} bind instance={}", this.getClass().getName(), this.hashCode(), clazz, instance.getClass());
        bind(clazz).toInstance(instance);
    }

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        binder().requireExactBindingAnnotations();
        // binder().disableCircularProxies();/*禁用循环依赖*/

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
                onConfigure.accept((T) this);
            }
            bind();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void bind() throws Throwable;

    protected final ConsumerE2<Class, TcpConfig> socketAction = (aClass, config) -> {
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

    protected final ConsumerE2<Class, DbConfig> dbAction = (aClass, config) -> {
        if (config.getDbPort() > 0) {
            Object newInstance = aClass.getDeclaredConstructor(config.getClass()).newInstance(config);
            if (StringUtil.emptyOrNull(config.getName())) {
                config.setName(newInstance.getClass().getSimpleName());
            }
            bindSingleton(aClass, newInstance);
        }
    };

}
