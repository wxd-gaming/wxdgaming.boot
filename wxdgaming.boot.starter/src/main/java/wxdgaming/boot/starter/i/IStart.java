package wxdgaming.boot.starter.i;

import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.Starter;

/**
 * 通过 {@link Starter#start(boolean, int, String, String...)}，特别注意热更新是不调用
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-28 12:06
 **/
public interface IStart {

    /**
     * bean初始化调用的，特别注意热更新是不调用，优先调用的是{@link IBeanInit#beanInit(IocContext)}
     */
    void start(IocContext iocInjector) throws Exception;

}
