package wxdgaming.boot.core.io;

import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.ConsumerE1;
import wxdgaming.boot.agent.function.FunctionE;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象池池化管理器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-10 11:04
 **/
public class ObjectFactory implements Serializable {

    /** 避免重载后class对象不是同一个bug */
    public static final ConcurrentHashMap<String, ObjectBox<? extends IObjectClear>> factory = new ConcurrentHashMap<>();

    /** 自动归还 */
    public static <R extends IObjectClear> void consumer(Class<R> bean, ConsumerE1<R> consumer) {
        final R object = getObject(bean);
        try {
            consumer.accept(object);
        } catch (Throwable e) {
            throw Throw.as(e);
        } finally {
            returnObject(object);
        }
    }

    /** 自动归还 */
    public static <T extends IObjectClear, R> R function(Class<T> bean, FunctionE<T, R> function) {
        final T object = getObject(bean);
        try {
            return function.apply(object);
        } catch (Throwable e) {
            throw Throw.as(e);
        } finally {
            returnObject(object);
        }
    }

    /**
     * 使用完成切记调用 {@link ObjectFactory#returnObject(IObjectClear)} 归还对象
     * <p>类需要包含无参构造函数，可以自定义实现 {@link IObjectClear} 接口来达到释放资源，还原初始化状态
     */
    public static <R extends IObjectClear> R getObject(Class<? extends R> bean) {
        ObjectBox objectBox = computeIfAbsent(bean);
        return (R) objectBox.getObject(bean);
    }

    /** 使用  {@link ObjectFactory#getObject(Class)} */
    @Deprecated
    public static <R extends IObjectClear> ObjectBox<R> computeIfAbsent(Class<? extends R> bean) {
        return (ObjectBox<R>) factory.computeIfAbsent(bean.getName(), l -> new ObjectBox<>(bean));
    }

    @Deprecated
    public static void put(Class<? extends IObjectClear> clazz, int core) {
        put(new ObjectBox<>(clazz, core));
    }

    /** 使用  {@link ObjectFactory#getObject(Class)} */
    @Deprecated
    public static void put(ObjectBox<? extends IObjectClear> objectBox) {
        factory.put(objectBox.getBeanClass().getName(), objectBox);
    }

    /** 归还 */
    public static <R extends IObjectClear> void returnObject(R bean) {
        Class aClass = bean.getClass();
        final ObjectBox init = computeIfAbsent(aClass);
        init.returnObject(bean);
    }

}
