package org.wxd.agent.function;

/**
 * 没有参数。但是有返回值的
 * </p>
 * 实例::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface SLFunction0<R> extends SerializableLambda {

    R apply() throws Exception;

}
