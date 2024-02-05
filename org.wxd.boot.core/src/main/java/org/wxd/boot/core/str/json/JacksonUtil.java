package org.wxd.boot.core.str.json;// package org.wxd.str.json;
//
// import com.fasterxml.jackson.annotation.JsonAutoDetect;
// import com.fasterxml.jackson.annotation.JsonInclude;
// import com.fasterxml.jackson.annotation.JsonTypeInfo;
// import com.fasterxml.jackson.core.JsonParser;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.Module;
// import com.fasterxml.jackson.databind.*;
// import com.fasterxml.jackson.databind.jsontype.NamedType;
// import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
// import org.wxd.exception.Throw;
// import org.wxd.field.extend.SFunction;
// import org.wxd.lang.JsonObject;
//
// import java.io.IOException;
// import java.io.Serializable;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
///**
// * Jackson Util
// *
// * @author: Troy.Chen(無心道, 15388152619)
// * @version: 2022-04-20 17:55
// **/
// public class FastJsonUtil implements Serializable {
//
//    /** 普通的格式化 */
//    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//    /** 格式化输出类信息 @class */
//    private static final ObjectMapper OBJECT_MAPPER_WRITE_CLASS = new ObjectMapper();
//
//    static {
//        {
//            /*反序列化的时候如果多了其他属性,不抛出异常*/
//            OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            //如果是空对象的时候,不抛异常
//            OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//            /*属性为null不转换*/
//            OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            /*枚举用name*/
//            OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
//            OBJECT_MAPPER.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
//            OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//            OBJECT_MAPPER.setVisibility(OBJECT_MAPPER.getVisibilityChecker()
//                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
//            );
//        }
//        {
//            /*反序列化的时候如果多了其他属性,不抛出异常*/
//            OBJECT_MAPPER_WRITE_CLASS.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            //如果是空对象的时候,不抛异常
//            OBJECT_MAPPER_WRITE_CLASS.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//            /*属性为null不转换*/
//            OBJECT_MAPPER_WRITE_CLASS.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            /*枚举用name*/
//            OBJECT_MAPPER_WRITE_CLASS.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
//            OBJECT_MAPPER_WRITE_CLASS.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
//            OBJECT_MAPPER_WRITE_CLASS.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//            OBJECT_MAPPER_WRITE_CLASS.setVisibility(OBJECT_MAPPER_WRITE_CLASS.getVisibilityChecker()
//                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
//                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
//                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
//            );
//
//
//            /*这个位置是格式化输出类型*/
//            OBJECT_MAPPER_WRITE_CLASS.activateDefaultTyping(
//                    LaissezFaireSubTypeValidator.instance,
//                    ObjectMapper.DefaultTyping.EVERYTHING,
//                    JsonTypeInfo.As.PROPERTY
//            );
//        }
//    }
//
//    /** 注册自定义解析 */
//    public static void registerModule(Module module) {
//        OBJECT_MAPPER.registerModule(module);
//        OBJECT_MAPPER_WRITE_CLASS.registerModule(module);
//    }
//
//    /** 注册自定义解析 */
//    public static void registerSubtypes(NamedType... namedTypes) {
//        OBJECT_MAPPER.registerSubtypes(namedTypes);
//        OBJECT_MAPPER_WRITE_CLASS.registerSubtypes(namedTypes);
//    }
//
//    /** 格式化 */
//    public static String toJson(Object object) {
//        try {
//            return OBJECT_MAPPER.writeValueAsString(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    /** 格式化 ,包含数据类型 {@code @class} */
//    public static String toJsonWriteType(Object object) {
//        try {
//            return OBJECT_MAPPER_WRITE_CLASS.writeValueAsString(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    /** 格式化 */
//    public static String toJsonFmt(Object object) {
//        try {
//            ObjectWriter objectWriter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
//            return objectWriter.writeValueAsString(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    /** 格式化,包含数据类型 {@code @class} */
//    public static String toJsonFmtWriteType(Object object) {
//        try {
//            ObjectWriter objectWriter = OBJECT_MAPPER_WRITE_CLASS.writerWithDefaultPrettyPrinter();
//            return objectWriter.writeValueAsString(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    /** 转化成字节流 */
//    public static byte[] toBytes(Object object) {
//        try {
//            return OBJECT_MAPPER.writeValueAsBytes(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    /** 格式化,包含数据类型 {@code @class} */
//    public static byte[] toBytesWriteType(Object object) {
//        try {
//            ObjectWriter objectWriter = OBJECT_MAPPER_WRITE_CLASS.writerWithDefaultPrettyPrinter();
//            return objectWriter.writeValueAsBytes(object);
//        } catch (Exception e) {
//            throw Throw.as(e);
//        }
//    }
//
//    public static <T> T parse(byte[] bytes, JavaType clazz) {
//        try {
//            return OBJECT_MAPPER.readValue(bytes, clazz);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static <T> T parse(byte[] bytes, Class<T> clazz) {
//        try {
//            return OBJECT_MAPPER.readValue(bytes, clazz);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static <T> T parse(byte[] bytes, TypeReference<T> tTypeReference) {
//        try {
//            return OBJECT_MAPPER.readValue(bytes, tTypeReference);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static JsonObject parseJsonObject(String str) {
//        try {
//            return OBJECT_MAPPER.readValue(str, JsonObject.class);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static <T> T parse(String str, Class<T> clazz) {
//        try {
//            return OBJECT_MAPPER.readValue(str, clazz);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     *
//     */
//    public static <T, F> T parse(String str, SFunction<F, ?> fn) {
//        try {
//            JavaType javaType = JavaTypeImpl.genericFieldTypes(fn);
//            return OBJECT_MAPPER.readValue(str, javaType);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static <T> T parse(String str, JavaType clazz) {
//        try {
//            return OBJECT_MAPPER.readValue(str, clazz);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static <T> T parse(String str, TypeReference<T> tTypeReference) {
//        try {
//            return OBJECT_MAPPER.readValue(str, tTypeReference);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    /**
//     * 多重泛型  数据结构
//     * List<R>
//     */
//    public static <R> List<R> parseArray(String jsonString, Class<R> innerClass) {
//        JavaType javaType = JavaTypeImpl.genericTypes(List.class, innerClass);
//        return parseArray(jsonString, javaType);
//    }
//
//    public static <R> List<R> parseArray(String jsonString, JavaType javaType) {
//        try {
//            return OBJECT_MAPPER.readValue(jsonString, javaType);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static Map<String, String> parseStringMap(String jsonString) {
//        JavaType javaType = JavaTypeImpl.genericTypes(HashMap.class, String.class, String.class);
//        try {
//            return OBJECT_MAPPER.readValue(jsonString, javaType);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//}
