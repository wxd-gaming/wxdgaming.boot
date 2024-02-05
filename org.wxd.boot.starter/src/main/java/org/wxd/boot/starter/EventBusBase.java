package org.wxd.boot.starter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.function.ConsumerE1;
import org.wxd.boot.core.collection.concurrent.ConcurrentTable;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-07-31 15:01
 **/
@Slf4j
@Getter
public abstract class EventBusBase extends IocSubContext {

    protected final ConcurrentTable<Class<? extends IScript>, Serializable, IScript> scripts = new ConcurrentTable<>();

    public <S extends IScript> S getScript(Class<S> scriptClass, Serializable scriptId) {
        Map<Serializable, IScript> serializableObjectConcurrentHashMap = scripts.computeIfAbsent(scriptClass, l -> {
            ConcurrentHashMap<Serializable, IScript> map = new ConcurrentHashMap<>();
            beanStream(scriptClass).forEach(object -> {
                map.put(object.scriptKey(), object);
            });
            return map;
        });
        if (serializableObjectConcurrentHashMap == null) return null;
        return (S) serializableObjectConcurrentHashMap.get(scriptId);
    }

    public <S extends IScript> void script(Class<S> scriptClass, Serializable scriptId, ConsumerE1<S> consumer) {
        S script = getScript(scriptClass, scriptId);
        if (script != null) {
            try {
                consumer.accept(script);
            } catch (Exception e) {
                throw new RuntimeException("scriptClass=" + scriptClass + ",id=" + scriptId, e);
            }
        }
    }

    public interface IScript {

        default Serializable scriptKey() {
            return null;
        }

    }

}
