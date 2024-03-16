package wxdgaming.boot.agent.function;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-03-18 15:48
 **/
@FunctionalInterface
public interface ConsumerE2<T1, T2> extends SerializableLambda {

    void accept(T1 t1, T2 t2) throws Exception;

}
