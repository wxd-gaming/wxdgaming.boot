package wxdgaming.boot.agent.function;

/**
 * 有3参数。有返回值
 * </p>
 * 类::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STLFunction3<T, P1, P2, P3, R> extends SerializableLambda {

    R apply(P1 p1, P2 p2, P3 p3) throws Throwable;

}
