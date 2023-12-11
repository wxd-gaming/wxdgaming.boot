package org.wxd.boot.net.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.exception.Throw;
import org.wxd.boot.net.SocketSession;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.GlobalUtil;

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
public final class MessageController implements Runnable, Cloneable {

    private ProtoMappingRecord mapping;
    private SocketSession session;
    @JSONField(serialize = false, deserialize = false)
    private Message message;

    public MessageController(ProtoMappingRecord mapping, SocketSession session, Message message) {
        this.mapping = mapping;
        this.session = session;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Object bean = mapping.instance();
            mapping.method().invoke(bean, session, message);
        } catch (Throwable throwable) {
            throwable = Throw.filterInvoke(throwable);
            log.error(this.toString(), throwable);
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
