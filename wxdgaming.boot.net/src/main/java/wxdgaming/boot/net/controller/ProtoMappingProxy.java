package wxdgaming.boot.net.controller;

import java.util.concurrent.atomic.AtomicReference;

public interface ProtoMappingProxy {

    void proxy(AtomicReference<Object> out, Object instance, Object[] params);

}
