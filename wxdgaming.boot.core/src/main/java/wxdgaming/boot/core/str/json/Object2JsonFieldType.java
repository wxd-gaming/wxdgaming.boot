package wxdgaming.boot.core.str.json;// package org.wxd.str.json;
//
// import com.fasterxml.jackson.databind.ser.PropertyFilter;
// import org.wxd.field.ClassMapping;
// import org.wxd.field.extend.FieldAnn;
// import org.wxd.field.extend.FieldType;
//
// import java.util.Map;
//
///**
// * 通过把对象解析字段转化成json
// *
// * @author: Troy.Chen(無心道, 15388152619)
// * @version: 2021-07-02 10:36
// **/
// public class Object2JsonFieldType {
//
//    /**
//     * 根据扩展字段序列化属性
//     */
//    public static String toJsonString(Object object, FieldType fieldType) {
//        return toJsonString(object, fieldType, JsonUtil.Features);
//    }
//
//    public static String toJsonString(Object object, FieldType fieldType, int features) {
//        ClassMapping wrapper = ClassBuilder.wrapper(object.getClass(), fieldType);
//        Map<String, String> stringStringMap = wrapper.toStringMap(object);
//        return Object2Json.toJson(stringStringMap, features);
//    }
//
//    public static String toJsonString(ClassMapping wrapper, Object object) {
//        return toJsonString(wrapper, object, JsonUtil.Features);
//    }
//
//    public static String toJsonString(ClassMapping wrapper, Object object, int features) {
//        try (SerializeWriter out = new SerializeWriter(null, features, JsonUtil.EmptyFilters)) {
//            JSONSerializer serializer = new JSONSerializer(out);
//
//            PropertyFilter filter = (Object source, String name, Object value) -> {
//                if (object.equals(source)) {
//                    return wrapper.getFieldMap().containsKey(name);
//                }
//                return true;
//            };
//
//            serializer.getPropertyFilters().add(filter);
//            serializer.write(object);
//            return out.toString();
//        }
//    }
//
//    /**
//     * 通过解析类，然后转化成redis用的string map
//     *
//     * @param object    对象
//     * @param fieldType {@link FieldAnn#fieldTypes()}
//     * @return
//     */
//    public static Map<String, String> toStringMap(Object object, FieldType fieldType) {
//        return toStringMap(object, fieldType, JsonUtil.Features);
//    }
//
//    /**
//     * 通过解析类，然后转化成redis用的string map
//     *
//     * @param object   对象
//     * @param features {@link com.ty.com.tytools.utils.JvmUtil}
//     * @return
//     */
//    public static Map<String, String> toStringMap(Object object, FieldType fieldType, int features) {
//        ClassMapping wrapper = ClassBuilder.wrapper(object.getClass(), fieldType);
//        return toStringMap(wrapper, object, features);
//    }
//
//    /**
//     * 通过解析类，然后转化成redis用的string map
//     *
//     * @param wrapper 对象解析映射
//     * @param object  对象
//     * @return
//     */
//    public static Map<String, String> toStringMap(ClassMapping wrapper, Object object) {
//        return toStringMap(wrapper, object, JsonUtil.Features);
//    }
//
//    /**
//     * 通过解析类，然后转化成redis用的string map
//     *
//     * @param wrapper  对象解析映射
//     * @param object   对象
//     * @param features {@link com.ty.com.tytools.utils.JvmUtil}
//     * @return
//     */
//    public static Map<String, String> toStringMap(ClassMapping wrapper, Object object, int features) {
//        final String toJSONString = toJsonString(wrapper, object, features);
//        return JsonParse2Object.parseStringMap(toJSONString);
//    }
//
//}
