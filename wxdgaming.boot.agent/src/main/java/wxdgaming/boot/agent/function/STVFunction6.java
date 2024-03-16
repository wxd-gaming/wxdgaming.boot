package wxdgaming.boot.agent.function;

/**
 * 没有返回值，有参数的方法
 * </p>
 * 类::方法
 *
 * @author: Proy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STVFunction6<T, P1, P2, P3, P4, P5, P6> extends SerializableLambda {

    void apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6) throws Exception;

}
