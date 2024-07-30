package wxdgaming.boot.agent.function;


import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.MethodUtil;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-20 19:27
 **/
public interface SerializableLambda extends Serializable {
    static final Pattern RETURN_TYPE_PATTERN = Pattern.compile("\\(.*\\)L(.*);");
    static final Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\((.*)\\).*");

    public static SerializedLambda getSerializedLambda(Object obj) {
        try {
            Method method = obj.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException("获取Lambda信息失败", e);
        }
    }

    default SerializedLambda getSerializedLambda() {
        return getSerializedLambda(this);
    }

    default Class<?> ofClass() {
        SerializedLambda serializedLambda = getSerializedLambda();
        return ofClass(serializedLambda);
    }

    static Class<?> ofClass(SerializedLambda serializedLambda) {
        try {
            String implClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(implClass);
            return aClass;
        } catch (ClassNotFoundException e) {
            throw Throw.as(e);
        }
    }

    default Field ofField() {
        SerializedLambda serializedLambda = getSerializedLambda();
        return ofField(serializedLambda);
    }

    static Field ofField(SerializedLambda serializedLambda) {
        try {
            // 从lambda信息取出method、field、class等
            String methodName = serializedLambda.getImplMethodName();
            Class<?> ofClass = ofClass(serializedLambda);
            final Field[] declaredFields = ofClass.getDeclaredFields();
            Field field = null;

            if (methodName.startsWith("is")) {
                field = ofField(declaredFields, methodName.substring(2));
            } else if (methodName.startsWith("get") || methodName.startsWith("set")) {
                field = ofField(declaredFields, methodName.substring(3));
            }
            if (field == null) {
                field = ofField(declaredFields, methodName);
            }
            if (field == null) {
                throw new RuntimeException("类：" + ofClass.getName() + "." + methodName + "() 没有对应的 字段");
            }
            return field;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    static Field ofField(final Field[] declaredFields, String methodName) {
        for (Field declaredField : declaredFields) {
            if (declaredField.getName().equalsIgnoreCase(methodName)) {
                return declaredField;
            }
        }
        return null;
    }

    /** 能通过lambda调用的只有一个方法，不可能多肽方法, 不支持构造函数 */
    default String ofMethodName() {
        SerializedLambda serializedLambda = getSerializedLambda();
        return ofMethodName(serializedLambda);
    }

    /** 能通过lambda调用的只有一个方法，不可能多肽方法, 不支持构造函数 */
    static String ofMethodName(SerializedLambda serializedLambda) {
        return serializedLambda.getImplMethodName();
    }

    /** 能通过lambda调用的只有一个方法，不可能多肽方法, 不支持构造函数 */
    default Method ofMethod() {
        SerializedLambda serializedLambda = getSerializedLambda();
        return ofMethod(serializedLambda);
    }

    /** 能通过lambda调用的只有一个方法，不可能多肽方法, 不支持构造函数 */
    static Method ofMethod(SerializedLambda serializedLambda) {
        String implMethodName = serializedLambda.getImplMethodName();
        Class<?> aClass = ofClass(serializedLambda);
        Map<String, Method> stringMethodMap = MethodUtil.readAllMethod(true, aClass);
        return stringMethodMap.values().stream().filter(v -> v.getName().equals(implMethodName)).findAny().orElse(null);
    }

    /** 获取Lambda表达式返回类型 */
    default Class<?> getReturnType() {
        SerializedLambda serializedLambda = getSerializedLambda();
        return getReturnType(serializedLambda);
    }

    static Class<?> getReturnType(SerializedLambda serializedLambda) {
        String expr = serializedLambda.getInstantiatedMethodType();
        Matcher matcher = RETURN_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new RuntimeException("获取Lambda信息失败");
        }
        String className = matcher.group(1).replace("/", ".");
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载类", e);
        }
    }

    /** 获取Lambda表达式的参数类型 */
    default Class<?>[] getParameterTypes() {
        return getParameterTypes(Thread.currentThread().getContextClassLoader());
    }

    default Class<?>[] getParameterTypes(ClassLoader classLoader) {
        SerializedLambda serializedLambda = getSerializedLambda();
        return getParameterTypes(classLoader, serializedLambda);
    }

    static Class<?>[] getParameterTypes(SerializedLambda serializedLambda) {
        return getParameterTypes(Thread.currentThread().getContextClassLoader(), serializedLambda);
    }

    static Class<?>[] getParameterTypes(ClassLoader classLoader, SerializedLambda serializedLambda) {
        String expr = serializedLambda.getInstantiatedMethodType();
        Matcher matcher = PARAMETER_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new RuntimeException("获取Lambda信息失败");
        }
        expr = matcher.group(1);

        List<? extends Class<?>> collect = Arrays.stream(expr.split(";"))
                .filter(s -> !s.isBlank())
                .map(s -> s.replace("L", "").replace("/", "."))
                .map(s -> {
                    try {
                        return classLoader.loadClass(s);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("无法加载类", e);
                    }
                })
                .collect(Collectors.toList());
        Class<?>[] classes = collect.toArray(new Class<?>[collect.size()]);
        return classes;
    }
}

