package wxdgaming.boot.agent.function;

/**
 * 没有返回值，没有参数的方法
 * </p>
 * 类::方法
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
@FunctionalInterface
public interface STVFunction0<T> extends SerializableLambda {

    void apply() throws Exception;

}
