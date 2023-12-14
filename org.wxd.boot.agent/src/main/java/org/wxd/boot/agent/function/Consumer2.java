package org.wxd.boot.agent.function;

/**
 * 传递两个参数的消费类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-10 10:25
 **/
@FunctionalInterface
public interface Consumer2<T1, T2> extends SerializableLambda {

    void accept(T1 t1, T2 t2);

}
