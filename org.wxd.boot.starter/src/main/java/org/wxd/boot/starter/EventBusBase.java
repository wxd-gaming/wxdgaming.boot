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

    public <Key extends Serializable, S extends IScript<Key>> S getScript(Class<S> scriptClass, Serializable scriptId) {
        Map<Serializable, IScript> iScriptMap = scripts.computeIfAbsent(scriptClass, l -> {
            ConcurrentHashMap<Serializable, IScript> map = new ConcurrentHashMap<>();
            beanStream(scriptClass).forEach(object -> {
                IScript iScript = map.get(object.scriptKey());
                if (iScript != null && !iScript.getClass().getName().equalsIgnoreCase(object.getClass().getName())) {
                    throw new RuntimeException("script class=" + scriptClass + ", script key=" + object.scriptKey() + ", 文件 " + iScript.getClass() + " 与 文件" + object.getClass() + " 冲突");
                }
                map.put(object.scriptKey(), object);
            });
            return map;
        });
        if (iScriptMap == null) return null;
        return (S) iScriptMap.get(scriptId);
    }

    public <Key extends Serializable, S extends IScript<Key>> void script(Class<S> scriptClass, Serializable scriptId, ConsumerE1<S> consumer) {
        S script = getScript(scriptClass, scriptId);
        if (script != null) {
            try {
                consumer.accept(script);
            } catch (Exception e) {
                log.error("scriptClass={}, key={}", script.getClass(), scriptId, e);
            }
        }
    }

    public interface IScript<Key extends Serializable> {

        default Key scriptKey() {
            return null;
        }

    }

}
