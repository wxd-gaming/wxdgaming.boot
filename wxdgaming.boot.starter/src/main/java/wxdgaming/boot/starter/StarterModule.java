package wxdgaming.boot.starter;

import wxdgaming.boot.agent.system.ReflectContext;

import java.util.function.Consumer;

/**
 * 每一个ioc创建的时候管理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-09-15 10:12
 **/
class StarterModule extends BaseModule<StarterModule> {

    public StarterModule(ReflectContext reflectContext, Consumer<StarterModule> onConfigure) {
        super(reflectContext, onConfigure);
    }

    protected StarterModule bind() {
        return this;
    }

}
