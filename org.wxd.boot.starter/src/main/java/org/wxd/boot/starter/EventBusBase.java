package org.wxd.boot.starter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.function.ConsumerE1;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-07-31 15:01
 **/
@Slf4j
@Getter
public abstract class EventBusBase extends IocSubContext {

    public <Key extends Serializable, S extends IScript<Key>> S script(Class<S> scriptClass, Key scriptId) {
        return beanStream(scriptClass).filter(script -> Objects.equals(script.scriptKey(), scriptId)).findAny().orElse(null);
    }

    public <Key extends Serializable, S extends IScript<Key>> Stream<S> scripts(Class<S> scriptClass, Key scriptId) {
        return beanStream(scriptClass).filter(script -> Objects.equals(script.scriptKey(), scriptId));
    }

    public <Key extends Serializable, S extends IScript<Key>> void scripts(Class<S> scriptClass, Key scriptId, ConsumerE1<S> consumer) {
        scripts(scriptClass, scriptId).forEach(script -> {
            try {
                consumer.accept(script);
            } catch (Exception e) {
                log.error("scriptClass={}, key={}", script.getClass(), scriptId, e);
            }
        });
    }

    public interface IScript<Key extends Serializable> {

        default Key scriptKey() {
            return null;
        }

    }

}
