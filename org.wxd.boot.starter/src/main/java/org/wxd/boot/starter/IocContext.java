package org.wxd.boot.starter;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.wxd.boot.agent.function.*;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.agent.system.MethodUtil;
import org.wxd.boot.core.ann.Sort;
import org.wxd.boot.core.str.StringUtil;
import org.wxd.boot.starter.config.Config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 容器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 19:28
 **/
@Slf4j
@Getter
public abstract class IocContext {

    @Inject protected Injector injector;

    public <R> R getInstance(Class<R> bean) {
        log.debug("{} {}", injector.hashCode(), bean.getName());
        return injector.getInstance(bean);
    }

    /** 字段 添加 实例化 */
    public void autoField(Object instance) {
        injector.injectMembers(injector);
    }

    /**
     * 返回查找对象集合
     *
     * @param filter 需要查找的类 或者 接口
     * @param <R>
     * @return
     */
    public <R> R findBean(Class<R> filter) {
        return beanStream(filter, (String) null).findFirst().orElse(null);
    }

    /**
     * 返回查找对象集合
     *
     * @param filter 需要查找的类 或者 接口
     * @param <R>
     * @return
     */
    public <R> Stream<R> beanStream(Class<R> filter) {
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
    public <R> Stream<R> beanStream(Class<R> filter, SerializableLambda serializedLambda) {
        return beanStream(filter, serializedLambda.ofMethodName());
    }

    /**
     * 循环处理
     *
     * @param filter
     * @param consumer
     * @param <R>
     */
    public <R> void forEachBean(Class<R> filter, ConsumerE1<R> consumer) {
        forEachBean(filter, (String) null, consumer);
    }

    public <R> void forEachBean(Class<R> filter, ConsumerE1<R> consumer, Consumer<Throwable> onError) {
        forEachBean(filter, (String) null, consumer, onError);
    }

    public <R> void forEachBean(Class<R> filter, String methodName, ConsumerE1<R> consumer) {
        forEachBean(filter, methodName, consumer, null);
    }

    public <R> void forEachBean(Class<R> filter, String methodName, ConsumerE1<R> consumer, Consumer<Throwable> onError) {
        beanStream(filter, methodName).forEach(r -> {
            try {
                consumer.accept(r);
            } catch (Throwable e) {
                log.error("event 事件 {}", r, e);
                if (onError != null) onError.accept(e);
            }
        });
    }

    public <R> void forEachBean(Class<R> filter, ConsumerE0 function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    public <T1> void forEachBean(Class<T> filter, ConsumerE1<T1> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    public <T1, T2> void forEachBean(Class<T> filter, ConsumerE2<T1, T2> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    public <T1, T2, T3> void forEachBean(Class<T> filter, ConsumerE3<T1, T2, T3> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    public <T1, T2, T3, T4> void forEachBean(Class<T> filter, ConsumerE4<T1, T2, T3, T4> function, Object... args) {
        forEachBean(filter, (SerializableLambda) function, args);
    }

    public <R> void forEachBean(Class<R> filter, SerializableLambda serializedLambda, Object... args) {
        Method method = serializedLambda.ofMethod();
        forEachBean(filter, method, args);
    }

    public <R> void forEachBean(Class<R> filter, Method method, Object... args) {
        forEachBean(filter, method, null, args);
    }

    public <R> void forEachBean(Class<R> filter, Method method, Consumer<Throwable> onError, Object... args) {
        beanStream(filter, method.getName()).forEach(r -> {
            try {
                method.invoke(r, args);
            } catch (Throwable e) {
                log.error("event 事件 {}", r, e);
                if (onError != null) onError.accept(e);
            }
        });
    }

    private static void allBindings(Injector injector, Map<Key<?>, Binding<?>> allBindings) {
        if (injector.getParent() != null) {
            allBindings(injector.getParent(), allBindings);
        }
        allBindings.putAll(injector.getAllBindings());
    }

    /**
     * 返回查找对象集合
     *
     * @param filter     需要查找的类 或者 接口
     * @param methodName 查找的对象的方法名 排序的时候需要的
     * @param <R>
     * @return
     */
    public <R> Stream<R> beanStream(Class<R> filter, String methodName) {
        HashMap<String, R> hashMap = new HashMap<>();
        Map<Key<?>, Binding<?>> allBindings = new HashMap<>();
        allBindings(injector, allBindings);
        final Set<Key<?>> keys = allBindings.keySet();
        try {
            for (Key<?> key : keys) {
                if (filter.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                    final R instance = (R) injector.getInstance(key);
                    hashMap.put(instance.getClass().getName(), instance);
                }
            }
        } catch (Exception e) {
            final RuntimeException runtimeException = new RuntimeException(e.getMessage());
            runtimeException.setStackTrace(e.getStackTrace());
            throw runtimeException;
        }
        if (hashMap.isEmpty()) {
            return Stream.of();
        }

        return hashMap.values().stream()
                .filter(v -> filter.isAssignableFrom(v.getClass()))
                .sorted((o1, o2) -> {
                    int c1 = Optional.ofNullable(AnnUtil.ann(o1.getClass(), Config.class)).map(v -> 1).orElse(2);
                    int c2 = Optional.ofNullable(AnnUtil.ann(o2.getClass(), Config.class)).map(v -> 1).orElse(2);
                    if (c1 != c2) {
                        return Integer.compare(c1, c2);
                    }
                    if (StringUtil.notEmptyOrNull(methodName)) {
                        Method method1 = MethodUtil.findMethod(o1.getClass(), methodName);
                        Method method2 = MethodUtil.findMethod(o2.getClass(), methodName);
                        int ms1 = Optional.ofNullable(AnnUtil.ann(method1, Sort.class)).map(Sort::value).orElse(99999);
                        int ms2 = Optional.ofNullable(AnnUtil.ann(method2, Sort.class)).map(Sort::value).orElse(99999);
                        if (ms1 != ms2) {
                            return Integer.compare(ms1, ms2);
                        }
                    }
                    Sort cs1 = AnnUtil.ann(o1.getClass(), Sort.class);
                    Sort cs2 = AnnUtil.ann(o2.getClass(), Sort.class);
                    if (cs1 != null && cs2 != null) {
                        return Integer.compare(cs1.value(), cs2.value());
                    } else if (cs1 != null) {
                        return -1;
                    } else if (cs2 != null) {
                        return -1;
                    }
                    return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                });
    }

}
