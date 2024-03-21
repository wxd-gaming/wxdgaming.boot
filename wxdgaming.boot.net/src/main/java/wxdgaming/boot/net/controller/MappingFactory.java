package wxdgaming.boot.net.controller;


import wxdgaming.boot.agent.function.ConsumerE2;
import wxdgaming.boot.core.collection.concurrent.ConcurrentTable;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.net.NioBase;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.controller.ann.TextMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 映射工程
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:06
 **/
public class MappingFactory {

    /** text mapping submit 监听 */
    public static ConsumerE2<Session, Event> TextMappingSubmitBefore = null;
    /** proto mapping submit 监听 */
    public static ConsumerE2<Session, Event> ProtoMappingSubmitBefore = null;

    public static final Class<? extends NioBase> FINAL_DEFAULT = NioBase.class;
    /** 消息id -> 映射 */
    public static final ConcurrentTable<Class<? extends NioBase>, Integer, ProtoMappingRecord> PROTO_MAP = new ConcurrentTable<>();
    /** 路由 -> 映射 */
    public static final ConcurrentTable<Class<? extends NioBase>, String, TextMappingRecord> TEXT_MAP = new ConcurrentTable<>();

    public static void putProto(Class<? extends NioBase> service, String remarks, int messageId, Object instance, Method method) {
        /** 虚拟线程 */
        if (service == null) service = FINAL_DEFAULT;
        PROTO_MAP.put(
                service,
                messageId,
                new ProtoMappingRecord(service, remarks, messageId, instance, method)
        );
    }

    public static void putText(TextMapping textMapping, Class<? extends NioBase> service, String path, String remarks, Object instance, Method method) {
        /** 虚拟线程 */
        if (service == null) service = FINAL_DEFAULT;
        TEXT_MAP.put(
                service,
                path,
                new TextMappingRecord(textMapping, path, remarks, instance, method)
        );
    }

    public static ProtoMappingRecord protoMappingRecord(Class<? extends NioBase> service, int messageId) {
        ProtoMappingRecord mapping = PROTO_MAP.get(service, messageId);
        if (mapping == null) {
            mapping = PROTO_MAP.get(FINAL_DEFAULT, messageId);
        }
        return mapping;
    }

    public static TextMappingRecord textMappingRecord(Class<? extends NioBase> service, String path) {
        TextMappingRecord mapping = TEXT_MAP.get(service, path);
        if (mapping == null) {
            mapping = TEXT_MAP.get(FINAL_DEFAULT, path);
        }
        return mapping;
    }

    public static Stream<ProtoMappingRecord> protoMappingRecord(Class<? extends NioBase> service) {
        Stream<ProtoMappingRecord> stream = PROTO_MAP.opt(MappingFactory.FINAL_DEFAULT)
                .map(Map::values)
                .stream()
                .flatMap(Collection::stream);
        if (!FINAL_DEFAULT.equals(service)) {
            stream = Stream.concat(
                    stream,
                    PROTO_MAP.opt(service)
                            .map(Map::values)
                            .stream()
                            .flatMap(Collection::stream)
            );
        }
        return stream;
    }

    public static Stream<TextMappingRecord> textMappingRecord(Class<? extends NioBase> service) {
        Stream<TextMappingRecord> stream = TEXT_MAP.opt(MappingFactory.FINAL_DEFAULT)
                .map(Map::values)
                .stream()
                .flatMap(Collection::stream);
        if (!FINAL_DEFAULT.equals(service)) {
            stream = Stream.concat(
                    stream,
                    TEXT_MAP.opt(service)
                            .map(Map::values)
                            .stream()
                            .flatMap(Collection::stream)
            );
        }
        stream = stream.sorted(Comparator.comparing(TextMappingRecord::path));
        return stream;
    }

}
