package wxdgaming.boot.starter;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.*;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.MethodUtil;
import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.starter.config.Config;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * context处理接口
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-04-21 10:13
 **/
public interface ContextAction {

    Logger log = LoggerFactory.getLogger(ContextAction.class);

    Injector getInjector();

    ConcurrentHashMap<String, List<Object>> getIocBeanMap();

    default <R> R getInstance(Class<R> bean) {
        log.debug("{} {}", getInjector().hashCode(), bean.getName());
        return getInjector().getInstance(bean);
    }

    /** 字段 添加 实例化 */
    default void autoField(Object instance) {
        getInjector().injectMembers(getInjector());
    }

    /**
     * 返回查找对象集合
     *
     * @param filter 需要查找的类 或者 接口
     * @param <R>
     * @return
     */
    default <R> R findBean(Class<R> filter) {
        return beanStream(filter, (String) null).findFirst().orElse(null);
    }

    /**
     * 返回查找对象集合
     *
     * @param filter 需要查找的类 或者 接口
     * @param <R>
     * @return
     */
    default <R> Stream<R> beanStream(Class<R> filter) {
        return beanStream(filter, (String) null);
    }

    /**
     * 返回查找对象集合
     *
     * @param filter           需要查找的类 或者 接口
     * @param serializedLambda 查找的对象的方法名 排序的时候需要的
     * @param <R>
     * @return
     */
    default <R> Stream<R> beanStream(Class<R> filter, SerializableLambda serializedLambda) {
        return beanStream(filter, serializedLambda.ofMethodName());
    }

    /**
     * 循环处理
     *
     * @param filter
     * @param consumer
     * @param <R>
     */
    default <R> void forEachBean(Class<R> filter, ConsumerE1<R> consumer) {
        forEachBean(filter, (String) null, consumer);
    }

    default <R> void forEachBean(Class<R> filter, ConsumerE1<R> consumer, Consumer<Throwable> onError) {
        forEachBean(filter, (String) null, consumer, onError);
    }

    default <R> void forEachBean(Class<R> filter, String methodName, ConsumerE1<R> consumer) {
        forEachBean(filter, methodName, consumer, null);
    }

    default <R> void forEachBean(Class<R> filter, String methodName, ConsumerE1<R> consumer, Consumer<Throwable> onError) {
        beanStream(filter, methodName).forEach(r -> {
            try {
                consumer.accept(r);
            } catch (Throwable e) {
                log.error("event 事件 {}", r, e);
                if (onError != null) onError.accept(e);
            }
        });
    }

    default <R> void forEachBean(Class<R> filter, ConsumerE0 function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    default <T1> void forEachBean(Class<T1> filter, ConsumerE1<T1> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    default <T1, T2> void forEachBean(Class<T1> filter, ConsumerE2<T1, T2> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    default <T1, T2, T3> void forEachBean(Class<T1> filter, ConsumerE3<T1, T2, T3> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    default <T1, T2, T3, T4> void forEachBean(Class<T1> filter, ConsumerE4<T1, T2, T3, T4> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    default <R> void forEachBean(Class<R> filter, SerializableLambda serializedLambda, Object... args) {
        Method method = serializedLambda.ofMethod();
        forEachBean(filter, method, args);
    }

    default <R> void forEachBean(Class<R> filter, SerializableLambda serializedLambda, Consumer<Throwable> onError, Object... args) {
        Method method = serializedLambda.ofMethod();
        forEachBean(filter, method, onError, args);
    }

    default <R> void forEachBean(Class<R> filter, Method method, Object... args) {
        forEachBean(filter, method, null, args);
    }

    default <R> void forEachBean(Class<R> filter, Method method, Consumer<Throwable> onError, Object... args) {
        beanStream(filter, method.getName()).forEach(r -> {
            try {
                method.invoke(r, args);
            } catch (Throwable e) {
                log.error("event 事件 {}", r, e);
                if (onError != null) onError.accept(e);
            }
        });
    }

    static void allBindings(Injector context, Map<Key<?>, Binding<?>> allBindings) {
        if (context.getParent() != null) {
            allBindings(context.getParent(), allBindings);
        }
        allBindings.putAll(context.getAllBindings());
    }

    /**
     * 返回查找对象集合
     *
     * @param filter     需要查找的类 或者 接口
     * @param methodName 查找的对象的方法名 排序的时候需要的
     * @param <R>
     * @return
     */
    default <R> Stream<R> beanStream(Class<R> filter, String methodName) {

        String findKey = filter.getName().toLowerCase() + "#" + String.valueOf(methodName).toLowerCase();
        /*构建缓存*/
        return getIocBeanMap().computeIfAbsent(findKey, l -> {

                    HashMap<String, Object> hashMap = new HashMap<>();
                    Map<Key<?>, Binding<?>> allBindings = new HashMap<>();
                    allBindings(getInjector(), allBindings);
                    final Set<Key<?>> keys = allBindings.keySet();
                    try {
                        for (Key<?> key : keys) {
                            if (filter.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                                final Object instance = getInjector().getInstance(key);
                                hashMap.put(instance.getClass().getName(), instance);
                            }
                        }
                    } catch (Exception e) {
                        throw Throw.as(e);
                    }

                    List<Object> list = hashMap.values().stream()
                            .filter(v -> filter.isAssignableFrom(v.getClass()))
                            .sorted((o1, o2) -> {
                                int c1 = Optional.ofNullable(AnnUtil.ann(o1.getClass(), Config.class)).map(v -> 1).orElse(2);
                                int c2 = Optional.ofNullable(AnnUtil.ann(o2.getClass(), Config.class)).map(v -> 1).orElse(2);
                                if (c1 != c2) {
                                    return Integer.compare(c1, c2);
                                }
                                Method method1 = null;
                                Method method2 = null;
                                if (StringUtil.notEmptyOrNull(methodName)) {
                                    method1 = MethodUtil.findMethod(o1.getClass(), methodName);
                                    method2 = MethodUtil.findMethod(o2.getClass(), methodName);
                                }
                                int ms1 = sortValue(o1.getClass(), method1);
                                int ms2 = sortValue(o1.getClass(), method2);
                                if (ms1 != ms2) {
                                    return Integer.compare(ms1, ms2);
                                }
                                return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                            })
                            .toList();
                    return List.copyOf(list);
                })
                .stream()
                .map(v -> (R) v);
    }

    default int sortValue(Class<?> clazz, Method method) {
        Sort sort = AnnUtil.ann(method, Sort.class);
        if (sort != null) return sort.value();
        sort = AnnUtil.ann(clazz, Sort.class);
        if (sort != null) return sort.value();
        return 99999;
    }

}
