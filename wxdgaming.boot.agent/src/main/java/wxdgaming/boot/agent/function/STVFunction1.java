package wxdgaming.boot.agent.function;

/**
 * 没有返回值，有1个参数的方法
 * </p>
 * 类::方法
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STVFunction1<T, P1> extends SerializableLambda {

    void apply(P1 p1) throws Throwable;

}
