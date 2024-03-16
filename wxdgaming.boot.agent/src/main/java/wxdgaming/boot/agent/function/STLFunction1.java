package wxdgaming.boot.agent.function;

/**
 * 有1参数。有返回值
 * </p>
 * 类::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STLFunction1<T, P1, R> extends SerializableLambda {

    R apply(P1 p1) throws Exception;

}
