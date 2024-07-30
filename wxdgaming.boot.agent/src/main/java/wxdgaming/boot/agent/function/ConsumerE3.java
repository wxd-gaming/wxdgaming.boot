package wxdgaming.boot.agent.function;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-03-18 15:48
 **/
@FunctionalInterface
public interface ConsumerE3<T1, T2, T3> extends SerializableLambda {

    void accept(T1 t1, T2 t2, T3 t3) throws Throwable;

}
