package wxdgaming.boot.starter;

import com.google.inject.Singleton;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.starter.action.ActionConfig;
import wxdgaming.boot.starter.config.Config;
import wxdgaming.boot.starter.i.IConfigInit;

/**
 * 基础模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class StarterModule extends BaseModule {

    public StarterModule(ReflectContext reflectContext, Class... classes) {
        super(reflectContext, classes);
    }

    protected StarterModule bind() {
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

        reflectContext.classStream().filter(c ->
                AnnUtil.ann(c, Singleton.class) != null
                        || AnnUtil.ann(c, TextController.class) != null
                        || AnnUtil.ann(c, ProtoController.class) != null
        ).forEach(this::bindSingleton);
        return this;
    }

}
