package wxdgaming.boot.starter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.ConsumerE1;
import wxdgaming.boot.core.collection.concurrent.ConcurrentTable;
import wxdgaming.boot.starter.i.IBeanInit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-07-31 15:01
 **/
@Slf4j
@Getter
public abstract class EventBusBase extends IocSubContext implements IBeanInit {

    final ConcurrentTable<String, Serializable, List<IScript<? extends Serializable>>> listTable = new ConcurrentTable<>();

    /**
     * bean初始化调用的，即便是热更新也会调用，会优先处理ioc注入
     *
     * @param iocContext
     */
    @Override public void beanInit(IocContext iocContext) throws Exception {

    }


    /** 获取所有脚本 */
    public <Key extends Serializable, S extends IScript<Key>> Stream<S> scripts(Class<S> scriptClass, Key scriptId) {
        String name = scriptClass.getName().toLowerCase();

        List<IScript<? extends Serializable>> scriptList = listTable.computeIfAbsent(name, scriptId, l -> {
            List<IScript<?>> tmps = new ArrayList<>();
            beanStream(IScript.class).filter(script -> script.scriptKey().equals(scriptId)).forEach(tmps::add);
            if (IScriptSingleton.class.isAssignableFrom(scriptClass)) {
                /* 实现是单例的key */
                if (tmps.size() > 1) throw new RuntimeException("单例的脚本 却有多个实现 key " + scriptId + " 重复");
            }
            return List.copyOf(tmps);
        });


        return (Stream<S>) scriptList.stream();
    }

    /** 获取一个脚本 */
    public <Key extends Serializable, S extends IScript<Key>> S script(Class<S> scriptClass, Key scriptId) {
        return scripts(scriptClass, scriptId).findAny().orElse(null);
    }

    /** 执行 */
    public <Key extends Serializable, S extends IScript<Key>> void scripts(Class<S> scriptClass, Key scriptId, ConsumerE1<S> consumer) {
        scripts(scriptClass, scriptId).forEach(script -> {
            try {
                consumer.accept(script);
            } catch (Throwable e) {
                log.error("scriptClass={}, key={}", script.getClass(), scriptId, e);
            }
        });
    }

    public interface IScript<Key extends Serializable> {

        Key scriptKey();

    }

    /** 会检查key值不能重复 */
    public interface IScriptSingleton<Key extends Serializable> extends IScript<Key> {}

}