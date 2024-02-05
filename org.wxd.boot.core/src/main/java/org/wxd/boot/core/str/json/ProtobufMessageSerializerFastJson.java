package org.wxd.boot.core.str.json;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.core.str.StringUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * fast json 自定义 {@link Message} 序列化处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-21 10:11
 **/
public class ProtobufMessageSerializerFastJson implements ObjectSerializer, ObjectDeserializer {

    public static ProtobufMessageSerializerFastJson default_instance = new ProtobufMessageSerializerFastJson();

    public static void init(String... packages) {
        init(Thread.currentThread().getContextClassLoader(), packages);
    }

    public static void init(ClassLoader classLoader, String... packages) {
        ReflectContext.Builder.of(classLoader, packages).build()
                .classWithSuper(MessageOrBuilder.class)
                .forEach(ProtobufMessageSerializerFastJson::action);
    }

    public static void init(Collection<Class> builderClasses) {
        action(builderClasses);
    }

    /** 把对应的类加入到 fast json的序列化 */
    public static void action(Collection<Class> classes) {
        for (Class aClass : classes) {
            action(aClass);
        }
    }

    /** 把对应的类加入到 fast json的序列化 */
    public static void action(Class clazz) {

        if (Message.Builder.class.isAssignableFrom(clazz) || Message.class.isAssignableFrom(clazz)) {
            SerializeConfig.getGlobalInstance().put(clazz, ProtobufMessageSerializerFastJson.default_instance);
            ParserConfig.getGlobalInstance().putDeserializer(clazz, ProtobufMessageSerializerFastJson.default_instance);
        }

    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        Class ft = (Class) fieldType;
        if (ft == null) {
            ft = object.getClass();
        }
        if (Message.Builder.class.isAssignableFrom(ft)) {
            serializer.write(ProtobufSerializer.toJson((Message.Builder) object));
        } else if (Message.class.isAssignableFrom(ft)) {
            serializer.write(ProtobufSerializer.toJson((Message) object));
        }
    }

    /** 由于集合序列化后为子类,再进行反序列化时,无法还原原对象,需要修改反序列化方法,手动修改反序列化逻辑 */
    @Override
    public Object deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String fieldValue = (String) parser.parse(fieldName);
        if (StringUtil.notEmptyOrNull(fieldValue)) {
            Class ft = (Class) type;
            if (Message.Builder.class.isAssignableFrom(ft)) {
                return ProtobufSerializer.parseBuilder4Json(fieldValue, (Class) type);
            } else {
                return ProtobufSerializer.parse4Json(fieldValue, (Class) type);
            }
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override public long getFeatures() {
        return ObjectSerializer.super.getFeatures();
    }
}
