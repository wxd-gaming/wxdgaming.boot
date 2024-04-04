package wxdgaming.boot.net.controller;

import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.handler.SocketCoderHandler;
import wxdgaming.boot.net.message.Rpc;

public abstract class ProtoMappingProxy {

    public void proxy(Object instance, Object session, Object msg) throws Throwable {
        ((SocketCoderHandler) instance).executor(
                (SocketSession) session,
                0,
                (Rpc.ReqRemote) msg
        );
    }

}
