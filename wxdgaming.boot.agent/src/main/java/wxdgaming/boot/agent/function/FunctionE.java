package wxdgaming.boot.agent.function;

import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-26 11:16
 **/
@FunctionalInterface
public interface FunctionE<T, R> extends Serializable {

    R apply(T t) throws Throwable;

}
