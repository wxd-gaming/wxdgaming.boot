package org.wxd.boot.str.json;

import org.wxd.agent.function.SLFunction1;
import org.wxd.agent.system.LambdaUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-09-11 18:15
 **/
public class ParameterizedTypeImpl implements ParameterizedType, Serializable {

    private static final long serialVersionUID = 1L;

    private static final ConcurrentHashMap<String, ParameterizedTypeImpl> ParameterizedTypeImplMap = new ConcurrentHashMap<>();

    /**
     * 获取json序列化类型
     *
     * @param fn
     * @return
     */
    public static <T> Type genericFieldTypes(SLFunction1<T, ?> fn) {
        Field field = LambdaUtil.ofField(fn);
        Class<?> ownerType = field.getType();
        Type genericType = field.getGenericType();
        return genericFieldTypes(ownerType, genericType);
    }

    /**
     * 获取json序列化类型
     *
     * @param field
     * @return
     */
    public static Type genericFieldTypes(Field field) {
        Class<?> ownerType = field.getType();
        Type genericType = field.getGenericType();
        return genericFieldTypes(ownerType, genericType);
    }

    public static Type genericFieldTypes(Class<?> ownerType, Type genericType) {
        if (ownerType.equals(genericType)
                || ownerType.equals(Object.class)
                || genericType.equals(Object.class)) {
            return ownerType;
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type rawType = parameterizedType.getRawType();
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return genericTypes(ownerType, rawType, typeArguments);
    }

    public static Type genericTypes(Type ownerType, Type rawType, Type... clazzs) {
        String typeString = ownerType.getTypeName();
        for (Type clazz : clazzs) {
            typeString += clazz.getTypeName();
        }
        return ParameterizedTypeImplMap.computeIfAbsent(typeString, l -> new ParameterizedTypeImpl(ownerType, rawType, clazzs));
    }

    Type ownerType;
    Type rawType;
    Type[] clazzs;

    private ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] clazzs) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.clazzs = clazzs;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return clazzs;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType instanceof Class) {
            sb.append(((Class<?>) ownerType).getSimpleName());
        } else {
            sb.append(ownerType.getTypeName());
        }
        if (clazzs != null && clazzs.length > 0) {
            sb.append("<");
            for (int i = 0; i < clazzs.length; i++) {
                if (i > 0) sb.append(", ");
                Type clazz = clazzs[i];
                if (clazz instanceof Class) {
                    sb.append(((Class<?>) clazz).getSimpleName());
                } else {
                    sb.append(clazz.getTypeName());
                }
            }
            sb.append(">");
        }
        return sb.toString();
    }
}
