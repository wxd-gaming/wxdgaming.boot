package org.wxd.boot.starter;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * 基础模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
abstract class BaseModule extends AbstractModule {

    public void bindSingleton(Class<?> clazz) {
        bind(clazz).in(Singleton.class);
    }

    public <T> void bindSingleton(Class<T> father, Class<? extends T> son) {
        bind(father).to(son).in(Singleton.class);
    }

    public <B> void bindSingleton(Class<B> clazz, B instance) {
        bind(clazz).toInstance(instance);
    }

    @Override
    protected final void configure() {
        binder().requireExplicitBindings();
        binder().requireExactBindingAnnotations();
        binder().disableCircularProxies();

        try {
            bind();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void bind() throws Exception;

}
