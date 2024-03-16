package wxdgaming.boot.core.str.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import wxdgaming.boot.agent.function.SLFunction1;
import wxdgaming.boot.agent.io.Objects;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.bit.BitFlag;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-21 10:09
 **/
public class FastJsonUtil {

    /** 默认值 */
    public static final SerializerFeature[] Writer_Features;
    /** 格式化 */
    public static final SerializerFeature[] Writer_Features_Fmt;
    /** 写入字段类型 */
    public static final SerializerFeature[] Writer_Features_Type_Name;
    /** 写入字段类型，并且格式化 */
    public static final SerializerFeature[] Writer_Features_Type_Name_Fmt;
    /**
     * 这个是会给所有的值对象加入引号，不管是否是字符串；
     * <p>json 标准key值必须是字符串
     */
    public static final SerializerFeature[] Writer_Features_Key_String;
    /**
     * 这个是会给所有的值对象加入引号，不管是否是字符串；
     * <p>json 标准key值必须是字符串, js long精度问题需要转string
     */
    public static final SerializerFeature[] Writer_Features_K_V_String;
    /**
     * 这个是会给所有的值对象加入引号，不管是否是字符串；
     * <p>json 标准key值必须是字符串
     */
    public static final SerializerFeature[] Writer_Features_Fmt_Key_String;
    /**
     * 这个是会给所有的值对象加入引号，不管是否是字符串；
     * <p>json 标准key值必须是字符串, js long精度问题需要转string
     */
    public static final SerializerFeature[] Writer_Features_Fmt_K_V_String;

    public static final Feature[] Reader_Features;


    static {
        /*fast json 启动类型自动推断*/
        Writer_Features = new SerializerFeature[]{SerializerFeature.QuoteFieldNames,    /*给字段加引号*/
                SerializerFeature.WriteMapNullValue,   /*map字段如果为null,输出为null*/
                SerializerFeature.WriteNullListAsEmpty,   /*List字段如果为null,输出为[],而非null*/
                SerializerFeature.WriteNullNumberAsZero,   /*数值字段如果为null,输出为0,而非null*/
                SerializerFeature.WriteNullBooleanAsFalse,   /*Boolean字段如果为null,输出为false,而非null*/
                SerializerFeature.WriteNullStringAsEmpty,   /*String字段如果为null,输出为"",而非null*/
                SerializerFeature.SkipTransientField,   /*忽律 transient*/
                SerializerFeature.WriteEnumUsingName,   /*枚举用 toString() */
                SerializerFeature.IgnoreNonFieldGetter,   /*忽略 没有 get 属性 继续写入*/
                SerializerFeature.DisableCircularReferenceDetect,   /*屏蔽循环引用*/
                SerializerFeature.SortField,   /*排序*/
                SerializerFeature.MapSortField}   /*排序*/
        ;

        Reader_Features = new Feature[]{
                Feature.OrderedField,
                Feature.SupportAutoType
        };

        Writer_Features_Fmt = Objects.merge(Writer_Features, SerializerFeature.PrettyFormat);

        Writer_Features_Type_Name = Objects.merge(Writer_Features, SerializerFeature.WriteClassName);

        Writer_Features_Type_Name_Fmt = Objects.merge(Writer_Features_Type_Name, SerializerFeature.PrettyFormat);

        Writer_Features_Key_String = Objects.merge(Writer_Features, SerializerFeature.WriteNonStringKeyAsString);/*所有的 key 都用引号*/
        Writer_Features_K_V_String = Objects.merge(Writer_Features_Key_String, SerializerFeature.WriteNonStringValueAsString);/*所有的 value 都用引号*/

        Writer_Features_Fmt_Key_String = Objects.merge(Writer_Features_Key_String, SerializerFeature.PrettyFormat);/*所有的 key 都用引号*/
        Writer_Features_Fmt_K_V_String = Objects.merge(Writer_Features_K_V_String, SerializerFeature.PrettyFormat);/*所有的 value 都用引号*/

        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);

        SerializeConfig.getGlobalInstance().put(BitSet.class, BitSetSerializerFastJson.default_instance);
        ParserConfig.getGlobalInstance().putDeserializer(BitSet.class, BitSetSerializerFastJson.default_instance);

        SerializeConfig.getGlobalInstance().put(BitFlag.class, BitFlagSerializerFastJson.default_instance);
        ParserConfig.getGlobalInstance().putDeserializer(BitFlag.class, BitFlagSerializerFastJson.default_instance);

    }

    /** 一般是js用的，所有 key 值都是字符串 格式化 */
    public static String toJsonKeyAsString(Object object) {
        return JSON.toJSONString(object, Writer_Features_Key_String);
    }

    /** 一般是js用的，所有 key - value 值都是字符串 格式化 */
    public static String toJsonAllAsString(Object object) {
        return JSON.toJSONString(object, Writer_Features_K_V_String);
    }

    /** 一般是js用的，所有 key 值都是字符串 格式化 */
    public static String toJsonFmtKeyAsString(Object object) {
        return JSON.toJSONString(object, Writer_Features_Fmt_Key_String);
    }

    /** 一般是js用的，所有 key - value 值都是字符串 格式化 */
    public static String toJsonFmtAllAsString(Object object) {
        return JSON.toJSONString(object, Writer_Features_Fmt_K_V_String);
    }

    /** 格式化 */
    public static String toJson(Object object) {
        return JSON.toJSONString(object, Writer_Features);
    }

    /** 格式化 ,包含数据类型 {@code @class} */
    public static String toJsonWriteType(Object object) {
        return JSON.toJSONString(object, Writer_Features_Type_Name);
    }

    /** 格式化 */
    public static String toJsonFmt(Object object) {
        return JSON.toJSONString(object, Writer_Features_Fmt);
    }

    /** 格式化,包含数据类型 {@code @class} */
    public static String toJsonFmtWriteType(Object object) {
        return JSON.toJSONString(object, Writer_Features_Type_Name_Fmt);
    }

    /** 转化成字节流 */
    public static byte[] toBytes(Object object) {
        return JSON.toJSONBytes(object, Writer_Features);
    }

    /** 格式化,包含数据类型 {@code @class} */
    public static byte[] toBytesWriteType(Object object) {
        return JSON.toJSONBytes(object, Writer_Features_Type_Name);
    }

    /** 通过反射解析某个类的某个字段，然后转化 */
    public static <T, F> T parse(byte[] bytes, SLFunction1<F, ?> function) {
        Type type = ParameterizedTypeImpl.genericFieldTypes(function);
        return parse(bytes, type);
    }

    public static JSONObject parse(byte[] bytes) {
        return JSON.parseObject(bytes, JSONObject.class, Reader_Features);
    }

    public static ObjMap parseObjMap(byte[] bytes) {
        return JSON.parseObject(bytes, ObjMap.class, Reader_Features);
    }

    public static <T> T parse(byte[] bytes, Type clazz) {
        return JSON.parseObject(bytes, clazz, Reader_Features);
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz, Reader_Features);
    }

    public static <T> T parse(String str, Class<T> clazz) {
        return JSON.parseObject(str, clazz, Reader_Features);
    }

    public static <T, F> T parse(String str, SLFunction1<F, ?> function) {
        Type type = ParameterizedTypeImpl.genericFieldTypes(function);
        return JSON.parseObject(str, type, Reader_Features);
    }

    public static JSONObject parse(String str) {
        return JSON.parseObject(str, JSONObject.class, Reader_Features);
    }

    public static ObjMap parseObjMap(String str) {
        return JSON.parseObject(str, ObjMap.class, Reader_Features);
    }

    public static <T> T parse(String str, Type type) {
        return JSON.parseObject(str, type, Reader_Features);
    }

    public static <T> T parse(String str, TypeReference<T> tTypeReference) {
        return JSON.parseObject(str, tTypeReference, Reader_Features);
    }

    /**
     * 多重泛型  数据结构
     * List<R>
     */
    public static JSONArray parseArray(String jsonString) {
        return parse(jsonString, JSONArray.class);
    }

    /**
     * 多重泛型  数据结构
     * List<R>
     */
    public static <R> List<R> parseArray(String jsonString, Class<R> innerClass) {
        return parse(jsonString, ParameterizedTypeImpl.genericTypes(ArrayList.class, ArrayList.class, innerClass));
    }

    /**
     * 多重泛型  数据结构
     * List<R>
     */
    public static <R> List<R> parseArray(byte[] bytes, Class<R> innerClass) {
        return parse(bytes, ParameterizedTypeImpl.genericTypes(ArrayList.class, ArrayList.class, innerClass));
    }

    public static Map<String, String> parseStringMap(String jsonString) {
        return parseMap(jsonString, String.class, String.class);
    }

    public static <K, V> Map<K, V> parseMap(String jsonString, Class<K> keyType, Class<V> valueType) {
        return
                parse(
                        jsonString,
                        ParameterizedTypeImpl.genericTypes(HashMap.class, HashMap.class, keyType, valueType)
                );
    }

    public static Map<String, String> parseStringMap(byte[] bytes) {
        return parseMap(bytes, String.class, String.class);
    }

    public static <K, V> Map<K, V> parseMap(byte[] bytes, Class<K> keyType, Class<V> valueType) {
        return
                parse(
                        bytes,
                        ParameterizedTypeImpl.genericTypes(HashMap.class, HashMap.class, keyType, valueType)
                );
    }
}
