package wxdgaming.boot.agent.system;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.function.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-11-05 17:51
 **/
@Slf4j
public class LambdaUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 获取实体类的字段名称
     */
    public static <R> String ofFieldName(SLFunction0<R> fn) {
        return fn.ofField().getName();
    }

    /**
     * 获取实体类的字段名称
     */
    public static <T> String ofFieldName(SLFunction1<T, ?> fn) {
        return fn.ofField().getName();
    }

    public static <T> Class<?> ofClass(SLFunction1<T, ?> fn) {
        return fn.ofClass();
    }

    /** 不支持构造函数 没有参数，没有返回值方法 */
    public static Method ofMethod(ConsumerE0 fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T> Method ofMethod(ConsumerE1<T> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T1, T2> Method ofMethod(ConsumerE2<T1, T2> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T1, T2, T3> Method ofMethod(ConsumerE3<T1, T2, T3> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T1, T2, T3, T4> Method ofMethod(ConsumerE4<T1, T2, T3, T4> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T1, T2, T3, T4, T5> Method ofMethod(ConsumerE5<T1, T2, T3, T4, T5> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 有参数，没有返回值方法 */
    public static <T1, T2, T3, T4, T5, T6> Method ofMethod(ConsumerE6<T1, T2, T3, T4, T5, T6> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数 没有参数带返回值 */
    public static <R> Method ofMethod(SLFunction0<R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, R> Method ofMethod(SLFunction1<T1, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, T2, R> Method ofMethod(SLFunction2<T1, T2, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, T2, T3, R> Method ofMethod(SLFunction3<T1, T2, T3, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, T2, T3, T4, R> Method ofMethod(SLFunction4<T1, T2, T3, T4, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, T2, T3, T4, T5, R> Method ofMethod(SLFunction5<T1, T2, T3, T4, T5, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 不支持构造函数，有参数带返回值 */
    public static <T1, T2, T3, T4, T5, T6, R> Method ofMethod(SLFunction6<T1, T2, T3, T4, T5, T6, R> fn) {
        // 从function取出序列化方法
        return fn.ofMethod();
    }

    /** 没有返回值，有参数的，比如 set 方法 获取字段 */
    public static <T> Field ofField(Consumer1<T> fn) {
        return fn.ofField();
    }

    /** 有返回值，没有参数，比如 get方法 获取字段 */
    public static <R> Field ofField(SLFunction0<R> fn) {
        return fn.ofField();
    }

    /** 有返回值，有一个参数的函数，通常是 set 方法返回自己 */
    public static <T, R> Field ofField(SLFunction1<T, R> fn) {
        return fn.ofField();
    }

    /** 通过方法获取映射信息 */
    public static <T> SerializedLambda ofSerializedLambda(SLFunction1<T, ?> fn) {
        return fn.getSerializedLambda();
    }


    /***
     * 创建代理 lambda 表达式调用
     * @param object 需要代理的对象
     * @param serializableLambda 代理映射接口
     */
    public static <M> void findDelegate(Object object, SerializableLambda serializableLambda, Consumer1<Mapping<M>> call) {
        Class inClass = serializableLambda.ofClass();
        Method inMethod = serializableLambda.ofMethod();
        findDelegate(inClass, inMethod, object, call);
    }

    /**
     * 创建代理 lambda 表达式调用
     *
     * @param object   需要代理的对象
     * @param inClass  代理映射接口
     * @param inMethod 代理映射方法
     * @param call     回调
     */
    public static <M> void findDelegate(Class<M> inClass, Method inMethod, Object object, Consumer1<Mapping<M>> call) {
        try {
            Type[] genericParameterTypes = inMethod.getGenericParameterTypes();
            Class<?> objectClass = object.getClass();
            Method[] methods = objectClass.getMethods();
            for (Method method : methods) {
                Type[] gpt = method.getGenericParameterTypes();
                if (genericParameterTypes.length != gpt.length) continue;
                boolean source = true;
                for (int i = 0; i < gpt.length; i++) {
                    if (!gpt[i].equals(genericParameterTypes[i])) {
                        source = false;
                        break;
                    }
                }
                if (method.getReturnType() != inMethod.getReturnType()) {
                    source = false;
                }
                if (source) {
                    Mapping delegate = createDelegate(inClass, inMethod, object, method);
                    try {
                        call.accept(delegate);
                    } catch (Throwable throwable) {
                        throw Throw.as(inClass + " - " + inMethod + " - " + object + " - " + method, throwable);
                    }
                }
            }
        } catch (Throwable throwable) {
            throw Throw.as(throwable);
        }
    }

    /**
     * 创建代理 lambda 表达式调用
     *
     * @param object             需要代理的对象
     * @param method             需要代理的对象
     * @param serializableLambda 代理映射接口
     */
    public static <M> Mapping<M> createDelegate(Object object, Method method, SerializableLambda serializableLambda) {
        Class inClass = serializableLambda.ofClass();
        Method inMethod = serializableLambda.ofMethod();
        return createDelegate(inClass, inMethod, object, method);
    }

    /**
     * 创建代理 lambda 表达式调用
     *
     * @param inClass  代理映射接口
     * @param inMethod 代理映射方法
     * @param object   需要代理的对象
     * @param method   需要代理的对象
     */
    public static <M> Mapping<M> createDelegate(Class<M> inClass, Method inMethod, Object object, Method method) {
        try {
            /*获取方法对象 委托*/
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            /*指定方法不以反射运行*/
            MethodHandle mh = lookup.unreflect(method);
            /*获取方法的类型*/
            MethodType type = mh.type();
            /*将方法的实例对象类型加到方法类型工厂里*/
            MethodType factoryType = MethodType.methodType(inClass, type.parameterType(0));
            /*移除方法里的实例对象类型*/
            type = type.dropParameterTypes(0, 1);
            /*获取代理对象，注意，第二个参数的字符串必须为函数式接口里的方法名*/
            CallSite metafactory = LambdaMetafactory.metafactory(lookup, inMethod.getName(), factoryType, type, mh, type);
            Object invokeExact = metafactory.getTarget().bindTo(object).invoke();
            return new Mapping(object, method, invokeExact);
        } catch (Throwable throwable) {
            throw Throw.as(inClass + " - " + inMethod + " - " + object + " - " + method, throwable);
        }
    }

    @Getter
    public static final class MappingFactory<M> {

        private final List<Mapping<M>> mappings = new ArrayList<>();

        public void put(Class<M> clsProxy, Object ins, Class<? extends Annotation> annotation) {
            List<Mapping<M>> lambdaMappings = mappings(clsProxy, ins, annotation);
            mappings.addAll(lambdaMappings);
        }

        /** 不会捕获异常，有问题终止 */
        public void forEach(Consumer<M> fn) {
            for (Mapping<M> mapping : mappings) {
                fn.accept(mapping.getMapping());
            }
        }

        /** 捕获异常，有问题会继续 */
        public void forEachTry(ConsumerE1<M> fn) {
            for (Mapping<M> mapping : mappings) {
                try {
                    fn.accept(mapping.getMapping());
                } catch (Throwable e) {
                    GlobalUtil.exception(mapping.getMethod().toString(), e);
                }
            }
        }

    }

    @Getter
    public static final class Mapping<M> {

        private final Object instance;
        private final Method method;
        /** 映射的代理对象 */
        private final M mapping;

        private Mapping(Object instance, Method method, M mapping) {
            this.instance = instance;
            this.method = method;
            this.mapping = mapping;
        }

        /** 原始对象 */
        public <R> R instance() {
            return (R) instance;
        }

    }

    /**
     * 把对象方法包含指定注解改为接口映射
     *
     * @param clsProxy   需要映射的接口
     * @param ins        需要查找代理的对象原始类
     * @param annotation 需要查找的注解
     * @param <M>        接口约束
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-06-26 11:06
     */
    public static <M> List<Mapping<M>> mappings(Class<M> clsProxy, Object ins, Class<? extends Annotation> annotation) {
        Set<Method> methods = MethodUtil.allMethods(clsProxy);
        Method proxyMethod = methods.iterator().next();
        return mappings(clsProxy, proxyMethod, ins, annotation);
    }

    /**
     * 把对象方法包含指定注解改为接口映射
     *
     * @param clsProxy    需要映射的接口
     * @param proxyMethod 需要映射的接口方法
     * @param ins         需要查找代理的对象原始类
     * @param annotation  需要查找的注解
     * @param <M>         接口约束
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-06-26 11:06
     */
    public static <M> List<Mapping<M>> mappings(Class<M> clsProxy, Method proxyMethod, Object ins, Class<? extends Annotation> annotation) {
        Class<?> cls = ins.getClass();
        Set<Method> methods = MethodUtil.allMethods(cls);
        List<Mapping<M>> mappings = new ArrayList<>();
        for (Method method : methods) {
            Annotation methodAnnotation = method.getAnnotation(annotation);
            if (methodAnnotation != null) {
                Mapping<M> delegate = LambdaUtil.createDelegate(clsProxy, proxyMethod, ins, method);
                mappings.add(delegate);
            }
        }
        return mappings;
    }

}
