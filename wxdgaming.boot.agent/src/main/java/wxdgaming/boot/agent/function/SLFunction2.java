package wxdgaming.boot.agent.function;

/**
 * 有2参数。有返回值
 * </p>
 * 实例::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface SLFunction2<P1, P2, R> extends SerializableLambda {

    R apply(P1 p1, P2 p2) throws Exception;

}
