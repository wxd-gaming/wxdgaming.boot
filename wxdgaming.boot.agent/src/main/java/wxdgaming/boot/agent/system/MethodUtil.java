package wxdgaming.boot.agent.system;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/***
 * 读取方法
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-10-20 15:29
 */
@Slf4j
public class MethodUtil {

    private static final Set<String> filterMethod = new HashSet<>();

    static {
        filterMethod.add("public java.lang.String java.lang.Object.toString()");
        filterMethod.add("public boolean java.lang.Object.equals(java.lang.Object)");
        filterMethod.add("public int java.lang.Object.hashCode()");
        filterMethod.add("public native int java.lang.Object.hashCode()");
        filterMethod.add("public final native void java.lang.Object.notify()");
        filterMethod.add("public final native void java.lang.Object.notifyAll()");
        filterMethod.add("public final void java.lang.Object.wait() throws java.lang.InterruptedException");
        filterMethod.add("public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException");
        filterMethod.add("public final void java.lang.Object.wait(long) throws java.lang.InterruptedException");
        filterMethod.add("public final native java.lang.Class java.lang.Object.getClass()");
    }

    public static Set<Method> allMethods(Class<?> cls) {
        Set<Method> sa1 = Set.of(cls.getMethods());
        Set<Method> sa2 = Set.of(cls.getDeclaredMethods());

        Set<Method> all = new HashSet<>();
        all.addAll(sa1);
        all.addAll(sa2);
        all.removeIf(method -> {
            if (filterMethod.contains(method.toString())) return true;
            if ("toString".equals(method.getName()) && method.getParameterTypes().length == 0) return true;
            if ("hashCode".equals(method.getName()) && method.getParameterTypes().length == 0) return true;
            if ("equals".equals(method.getName()) && method.getParameterTypes().length == 1) return true;
            return false;
        });
        return all;
    }

    public static Method findMethod(Class<?> cls, String methodName) {
        return findMethod(false, cls, methodName);
    }

    /** 如果有重载的方法，只会随机返回一个 */
    public static Method findMethod(boolean readStatic, Class<?> cls, String methodName) {
        Map<String, Method> stringMethodTreeMap = readAllMethods0(cls);
        return stringMethodTreeMap
                .values()
                .stream()
                .filter(method -> {
                    if (!readStatic && Modifier.isStatic(method.getModifiers())) {
                        /*非静态*/
                        return false;
                    }
                    return method.getName().equals(methodName);
                })
                .findAny()
                .orElse(null);
    }


    /**
     * 查找 cls 类 里 非静态函数
     * <p>
     * 并且 函数需要的参数是 parameterClass
     *
     * @param cls            需要辨识的类型
     * @param parameterClass 需要查找的函数包含的参数类型
     * @return
     */
    public static Map<String, Method> readAllMethod(Class<?> cls, Class<?>... parameterClass) {
        return readAllMethod(false, cls, parameterClass);
    }

    /**
     * @param readStatic     读取静态字段
     * @param cls            需要分析的类型
     * @param parameterClass 需要判断函数第一个参数是否继承自此类型
     * @return
     */
    public static Map<String, Method> readAllMethod(boolean readStatic, Class<?> cls, Class<?>... parameterClass) {
        Map<String, Method> stringMethodTreeMap = readAllMethods0(cls);
        Map<String, Method> fmmap = new LinkedHashMap<>();
        for (Method method : stringMethodTreeMap.values()) {
            //            System.out.println(method.getName() + ", " + method.hashCode());
            if (!readStatic && Modifier.isStatic(method.getModifiers())) {
                /*非静态*/
                continue;
            }

            if (method.isBridge()) {
                /*桥模式，表示这个方法一定被继承，或者从写*/
                continue;
            }

            /*参数类型 带泛型*/
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (parameterClass.length > 0 && genericParameterTypes.length < parameterClass.length) {
                continue;
            }

            boolean checked = true;
            for (int i = 0; i < parameterClass.length; i++) {
                Class<?> pc = parameterClass[i];
                Type parameterizedType = genericParameterTypes[i];
                try {
                    if (parameterizedType instanceof ParameterizedType pt) {
                        Type rawType = pt.getRawType();
                        Class<?> parameter = (Class<?>) rawType;
                        if (!pc.isAssignableFrom(parameter)) {
                            checked = false;
                            break;
                        }
                    } else {
                        Class<?> parameter = (Class<?>) parameterizedType;
                        if (!pc.isAssignableFrom(parameter)) {
                            checked = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.warn("类：" + cls.getName() + ", 无法解析的方法：" + method.getName() + ", 第 " + (i + 1) + " 个类型参数名：" + parameterizedType.toString(), e);
                }
            }

            if (checked) {
                String methodFullName = methodFullName(method);
                /*非桥模式，也就是覆盖了父类或者实现了接口*/
                fmmap.put(methodFullName, method);
            }
        }
        return fmmap;
    }

    /**
     * 查找 cls 类 里 非静态函数
     * <p>
     * 并且 函数需要的参数是 parameterClass
     *
     * @param cls 需要分析的类型
     */
    private static Map<String, Method> readAllMethods0(Class<?> cls) {
        Map<String, Method> methodList = new LinkedHashMap<>();
        readAllMethods0(cls, methodList);
        return methodList;
    }

    private static void readAllMethods0(Class<?> cls, Map<String, Method> methodList) {
        try {
            Class<?> superclass = cls.getSuperclass();
            if (superclass != null && !Object.class.equals(superclass)) {
                readAllMethods0(superclass, methodList);
            }
            Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                readAllMethods0(anInterface, methodList);
            }
            Method[] declaredMethods = cls.getDeclaredMethods();
            readAllMethods0(declaredMethods, methodList);
        } catch (Throwable throwable) {
            throw Throw.as(cls.getName(), throwable);
        }
    }

    private static void readAllMethods0(Method[] methods, Map<String, Method> methodList) {
        for (Method method : methods) {
            if (method.isBridge()) {
                /*桥模式，表示这个方法一定被继承，或者从写*/
                continue;
            }
            String methodFullName = methodFullName(method);
            /*非桥模式，也就是覆盖了父类或者实现了接口*/
            methodList.put(methodFullName, method);
        }
    }


    /**
     * 获取method 的全名，包括参数
     *
     * @param method
     * @return
     */
    public static String methodFullName(Method method) {
        StringBuilder methodName = new StringBuilder(method.getName());
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (Type genericParameterType : genericParameterTypes) {
            if (genericParameterType instanceof Class) {
                methodName.append("_").append(((Class<?>) genericParameterType).getSimpleName());
            } else if (genericParameterType instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    methodName.append("_").append(((Class<?>) rawType).getSimpleName());
                } else {
                    methodName.append("_").append(parameterizedType.getTypeName());
                }
            } else {

                methodName.append("_").append(genericParameterType.getTypeName());
            }
        }
        return methodName.toString();
    }
}
