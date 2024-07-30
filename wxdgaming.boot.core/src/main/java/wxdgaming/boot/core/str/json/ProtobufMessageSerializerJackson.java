package wxdgaming.boot.core.str.json;// package org.wxd.str.json;
//
// import com.fasterxml.jackson.core.JacksonException;
// import com.fasterxml.jackson.core.JsonGenerator;
// import com.fasterxml.jackson.core.JsonParser;
// import com.fasterxml.jackson.databind.DeserializationContext;
// import com.fasterxml.jackson.databind.JsonDeserializer;
// import com.fasterxml.jackson.databind.JsonSerializer;
// import com.fasterxml.jackson.databind.SerializerProvider;
// import com.fasterxml.jackson.databind.module.SimpleModule;
// import com.google.protobuf.Message;
// import com.google.protobuf.MessageOrBuilder;
// import com.google.protobuf.util.JsonFormat;
// import org.wxd.exception.Throw;
// import org.wxd.system.ClassUtil;
//
// import java.io.IOException;
// import java.util.Collection;
//
///**
// * fastjson 处理proto buf的序列化和反序列化
// *
// * @author: wxd-gaming(無心道, 15388152619)
// * @version: 2022-04-09 17:50
// **/
// public class ProtobufMessageSerializerJackson {
//
//    public static void init(String... packages) {
//
//        Collection<Class<?>> builderClasses = ClassUtil.getClasses(MessageOrBuilder.class, packages);
//        action(builderClasses);
//
//    }
//
//    /**
//     * 把对应的类加入到 fast json的序列化
//     *
//     * @param classes
//     */
//    public static void action(Collection<Class<?>> classes) {
//        for (Class<?> aClass : classes) {
//            action(aClass);
//        }
//    }
//
//    public static void action(Class clazz) {
//        SimpleModule simpleModule = new SimpleModule();
//        if (Message.Builder.class.isAssignableFrom(clazz) || Message.class.isAssignableFrom(clazz)) {
//            simpleModule.addSerializer(clazz, new ProtobufJsonSerializer(clazz));
//            simpleModule.addDeserializer(clazz, new ProtobufJsonDeserializer(clazz));
//        }
//        FastJsonUtil.registerModule(simpleModule);
//    }
//
//    public static class ProtobufJsonSerializer extends JsonSerializer<MessageOrBuilder> {
//
//        private Class<?> protoClass;
//
//        public ProtobufJsonSerializer(Class<?> protoClass) {
//            this.protoClass = protoClass;
//        }
//
//        @Override
//        public void serialize(MessageOrBuilder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//            if (Message.Builder.class.isAssignableFrom(value.getClass())) {
//                gen.writeString(message2Json((Message.Builder) value));
//            } else if (Message.class.isAssignableFrom(value.getClass())) {
//                gen.writeString(message2Json((Message) value));
//            }
//        }
//    }
//
//    public static class ProtobufJsonDeserializer extends JsonDeserializer<MessageOrBuilder> {
//
//        private Class<?> protoClass;
//
//        public ProtobufJsonDeserializer(Class<?> protoClass) {
//            this.protoClass = protoClass;
//        }
//
//        @Override
//        public MessageOrBuilder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
//            String valueAsString = p.getValueAsString();
//            if (Message.Builder.class.isAssignableFrom(protoClass)) {
//                return parseMessageBuilder4Json(valueAsString, protoClass);
//            } else {
//                return parseMessage4Json(valueAsString, protoClass);
//            }
//        }
//
//    }
//
//}
