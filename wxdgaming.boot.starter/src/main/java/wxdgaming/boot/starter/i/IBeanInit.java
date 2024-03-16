package wxdgaming.boot.starter.i;

import wxdgaming.boot.starter.IocContext;

/**
 * bean初始化调用的，即便是热更新也会调用
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 10:01
 **/
public interface IBeanInit {

    /** bean初始化调用的，即便是热更新也会调用，会优先处理ioc注入 */
    void beanInit(IocContext iocContext) throws Exception;

}
