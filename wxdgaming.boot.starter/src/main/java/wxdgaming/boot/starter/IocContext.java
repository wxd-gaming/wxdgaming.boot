package wxdgaming.boot.starter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-12 19:28
 **/
@Slf4j
public abstract class IocContext implements ContextAction {

    @Inject protected Injector injector;

    protected final ConcurrentHashMap<String, List<Object>> iocBeanMap = new ConcurrentHashMap<>();

    @Override public Injector getInjector() {
        return injector;
    }

    @Override public ConcurrentHashMap<String, List<Object>> getIocBeanMap() {
        return iocBeanMap;
    }
}
