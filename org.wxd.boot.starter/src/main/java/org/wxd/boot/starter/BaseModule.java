package org.wxd.boot.starter;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.system.ReflectContext;

/**
 * 基础模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
@Slf4j
@Getter
abstract class BaseModule extends AbstractModule {

    protected final ReflectContext reflectContext;
    protected final Class[] classes;

    public BaseModule(ReflectContext reflectContext) {
        this.reflectContext = reflectContext;
        this.classes = new Class[0];
    }

    public BaseModule(ReflectContext reflectContext, Class... classes) {
        this.reflectContext = reflectContext;
        this.classes = classes;
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
        binder().disableCircularProxies();

        try {
            for (Class aClass : classes) {
                bindSingleton(aClass);
            }
            bind();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract BaseModule bind() throws Exception;

}
