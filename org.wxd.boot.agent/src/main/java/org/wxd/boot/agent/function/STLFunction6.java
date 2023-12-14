package org.wxd.boot.agent.function;

/**
 * lmd表达式
 * </p>
 * 类::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STLFunction6<T, P1, P2, P3, P4, P5, P6, R> extends SerializableLambda {

    R apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) throws Exception;

}
