package wxdgaming.boot.agent.function;

/**
 * 传递两个参数的消费类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-10 10:25
 **/
@FunctionalInterface
public interface Function4<T1, T2, T3, T4, R> extends SerializableLambda {

    R apply(T1 t1, T2 t2, T3 t3, T4 t4);

}
