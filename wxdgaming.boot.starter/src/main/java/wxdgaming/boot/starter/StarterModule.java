package wxdgaming.boot.starter;

import wxdgaming.boot.agent.system.ReflectContext;

import java.util.function.Consumer;

/**
 * 基础模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class StarterModule extends BaseModule {

    public StarterModule(ReflectContext reflectContext, Consumer<? super BaseModule> onConfigure) {
        super(reflectContext, onConfigure);
    }

    protected StarterModule bind() {
        return this;
    }

    @Override public StarterModule bindSingleton(Class<?> clazz) {
        super.bindSingleton(clazz);
        return this;
    }

    @Override public <T> StarterModule bindSingleton(Class<T> father, Class<? extends T> son) {
        super.bindSingleton(father, son);
        return this;
    }

    @Override public <B> StarterModule bindSingleton(Class<B> clazz, B instance) {
        super.bindSingleton(clazz, instance);
        return this;
    }
}
