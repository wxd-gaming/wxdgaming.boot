package wxdgaming.boot.net.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.system.GlobalUtil;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.net.SocketSession;

import java.util.concurrent.atomic.AtomicReference;

/**
 * socket message 处理器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-08-05 14:22
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public final class ProtoListenerAction extends Event {

    private ProtoMappingRecord mapping;
    private SocketSession session;
    @JSONField(serialize = false, deserialize = false)
    private Message message;

    public ProtoListenerAction(ProtoMappingRecord mapping, SocketSession session, Message message) {
        super(mapping.method());
        this.mapping = mapping;
        this.session = session;
        this.message = message;
    }

    @Override public String getTaskInfoString() {
        return toString();
    }

    @Override public void onEvent() throws Exception {
        try {
            Object bean = mapping.instance();
            AtomicReference<Object> out = new AtomicReference<>();
            mapping.mapping().proxy(out, bean, new Object[]{session, message});
            //mapping.method().invoke(bean, session, message);
        } catch (Throwable throwable) {
            throwable = Throw.filterInvoke(throwable);
            GlobalUtil.exception("\n" + this.toString(), throwable);
        }
    }

    @Override
    public String toString() {
        return
                "session = " + session
                        + ";\ncontroller=" + mapping.method().getDeclaringClass().getName()
                        + ";\nmessage = " + message.getClass().getName() + FastJsonUtil.toJson(message);
    }

}
