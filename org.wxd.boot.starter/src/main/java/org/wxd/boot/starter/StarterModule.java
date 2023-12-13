package org.wxd.boot.starter;

import org.wxd.agent.exception.Throw;
import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.starter.action.ActionConfig;
import org.wxd.boot.starter.config.Config;
import org.wxd.boot.starter.i.IConfigInit;

/**
 * 基础模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class StarterModule extends SystemModule {

    final ReflectContext reflectContext;

    public StarterModule(ReflectContext reflectContext) {
        this.reflectContext = reflectContext;
    }

    protected void bind() {
        reflectContext.classWithAnnotated(Config.class).forEach(aClass -> {
            try {
                Object o = ActionConfig.action(aClass);
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

}
