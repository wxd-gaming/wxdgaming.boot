package wxdgaming.boot.starter;

import wxdgaming.boot.agent.system.ReflectContext;

import java.util.function.Consumer;

/**
 * 用户自定义
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-13 09:30
 **/
public abstract class UserModule extends BaseModule {

    public UserModule(ReflectContext reflectContext) {
        super(reflectContext, null);
    }

    public UserModule(ReflectContext reflectContext, Consumer<? super BaseModule> onConfigure) {
        super(reflectContext, onConfigure);
    }

}
