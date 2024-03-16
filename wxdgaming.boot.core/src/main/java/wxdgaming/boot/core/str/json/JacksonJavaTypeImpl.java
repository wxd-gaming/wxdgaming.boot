package wxdgaming.boot.core.str.json;// package org.wxd.str.json;
//
// import com.fasterxml.jackson.databind.JavaType;
// import com.fasterxml.jackson.databind.type.TypeFactory;
// import org.wxd.field.LambdaUtil;
// import org.wxd.field.extend.SFunction;
//
// import java.lang.reflect.Field;
// import java.lang.reflect.ParameterizedType;
// import java.lang.reflect.Type;
// import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author: Troy.Chen(無心道, 15388152619)
// * @version: 2022-04-20 18:25
// **/
// public class JacksonJavaTypeImpl {
//
//    private static final ConcurrentHashMap<String, JavaType> ParameterizedTypeImplMap = new ConcurrentHashMap<>();
//
//    /**
//     * 获取json序列化类型
//     *
//     * @param fn
//     * @return
//     */
//    public static <T> JavaType genericFieldTypes(SFunction<T, ?> fn) throws Exception {
//        Field field = LambdaUtil.ofField(fn);
//        return genericFieldTypes(field);
//    }
//
//    /**
//     * 获取json序列化类型
//     *
//     * @param field
//     * @return
//     */
//    public static JavaType genericFieldTypes(Field field) {
//        Class<?> ownerType = field.getType();
//        Type genericType = field.getGenericType();
//        return genericFieldTypes(ownerType, genericType);
//    }
//
//    public static JavaType genericFieldTypes(Class<?> ownerType, Type genericType) {
//        if (ownerType.equals(genericType)
//                || ownerType.equals(Object.class)
//                || genericType.equals(Object.class)) {
//            return genericTypes(ownerType);
//        }
//        ParameterizedType parameterizedType = (ParameterizedType) genericType;
//        Type[] typeArguments = parameterizedType.getActualTypeArguments();
//        Type rawType = parameterizedType.getRawType();
//        if (rawType instanceof Class) {
//            return genericTypes((Class<?>) rawType, typeArguments);
//        } else {
//            return genericTypes(ownerType, typeArguments);
//        }
//    }
//
//    public static JavaType genericTypes(Class<?> ownerType, Type... clazzs) {
//        String typeString = ownerType.getTypeName();
//        JavaType[] types = new JavaType[clazzs.length];
//
//        for (int i = 0; i < clazzs.length; i++) {
//            Type clazz = clazzs[i];
//            typeString += clazz.getTypeName();
//            types[i] = TypeFactory.defaultInstance().constructType(clazz);
//        }
//
//        return ParameterizedTypeImplMap.computeIfAbsent(
//                typeString,
//                l -> TypeFactory.defaultInstance().constructParametricType(ownerType, types)
//        );
//
//    }
//
//}
