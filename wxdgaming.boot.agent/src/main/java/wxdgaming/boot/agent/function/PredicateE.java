package wxdgaming.boot.agent.function;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-04-22 15:00
 **/
@FunctionalInterface
public interface PredicateE<T> extends SerializableLambda {

    boolean test(T t) throws Throwable;

}
