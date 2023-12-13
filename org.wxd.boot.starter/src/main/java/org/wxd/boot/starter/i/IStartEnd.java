package org.wxd.boot.starter.i;

import org.wxd.boot.starter.InjectorContext;

/**
 * bean初始化调用的，特别注意热更新是不调用
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 10:01
 **/
public interface IStartEnd {

    /** 初始化自己 ,是在自动注入之后调用的 */
    void startEnd(InjectorContext iocInjector) throws Exception;

}
