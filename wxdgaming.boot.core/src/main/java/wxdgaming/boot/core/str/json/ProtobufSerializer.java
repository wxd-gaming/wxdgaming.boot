package wxdgaming.boot.core.str.json;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.str.StringUtil;

/**
 * protobuf 消息协议序列化
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-21 10:11
 **/
public class ProtobufSerializer {

    /** 把消息转化成json字符串 */
    public static String toJson(MessageOrBuilder object) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.setLength(0);
            JsonFormat.printer().sortingMapKeys().appendTo(object, stringBuilder);
            return StringUtil.filterLine(stringBuilder.toString());
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /** 把json转化成message */
    public static <R extends Message> R parse4Json(String json, Class<?> messageClass) {
        try {
            Message.Builder builder = (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
            parse4Json(json, builder);
            return (R) builder.build();
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }

    /** 把json转化成message */
    public static void parse4Json(String json, Message.Builder builder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }

    /** 把json转化成message */
    public static <R extends Message.Builder> R parseBuilder4Json(String json, Class<?> builderClass) {
        try {
            int indexOf = builderClass.getName().indexOf("$Builder");
            String name = builderClass.getName().substring(0, indexOf);
            Class<?> messageClass = Thread.currentThread().getContextClassLoader().loadClass(name);
            Message.Builder builder = (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
            parse4Json(json, builder);
            return (R) builder;
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }

    public static <R extends Message.Builder> R parseBuilder4Bytes(Class<?> builderClass, byte[] bytes) {
        try {
            int indexOf = builderClass.getName().indexOf("$Builder");
            String name = builderClass.getName().substring(0, indexOf);
            Class<?> messageClass = Thread.currentThread().getContextClassLoader().loadClass(name);
            Message.Builder builder = (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
            parse4Bytes(builder, bytes);
            return (R) builder;
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }

    public static <R extends Message> R parse4Bytes(Class<?> messageClass, byte[] bytes) {
        try {
            Message.Builder builder = (Message.Builder) messageClass.getMethod("newBuilder").invoke(null);
            parse4Bytes(builder, bytes);
            return (R) builder.build();
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }

    /** 把json转化成message */
    public static void parse4Bytes(Message.Builder builder, byte[] bytes) {
        try {
            builder.mergeFrom(bytes);
        } catch (Throwable e) {
            throw Throw.as(e);
        }
    }
}
