package wxdgaming.boot.agent.function;

/**
 * 没有返回值，有2个参数的方法
 * </p>
 * 类::方法
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STVFunction2<T, P1, P2> extends SerializableLambda {

    void apply(P1 p1, P2 p2) throws Throwable;

}
