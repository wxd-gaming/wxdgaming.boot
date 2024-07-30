package wxdgaming.boot.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.message.RpcEvent;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-30 09:18
 **/
interface SessionRpc extends ByteBufWrapper {

    Logger log = LoggerFactory.getLogger(SessionRpc.class);

    /**
     * 心跳保活机制
     */
    default SocketSession heart() {
        rpc("rpc.heart", "heart", 1).send();
        return (SocketSession) this;
    }

    default RpcEvent rpc(String cmd, String key, Object value) {
        return rpc(cmd, new ObjMap().append(key, value));
    }

    default RpcEvent rpc(String cmd, ObjMap objMap) {
        return rpc(cmd, objMap.toJson());
    }

    default RpcEvent rpc(String cmd, String dataJson) {
        return new RpcEvent((SocketSession) this, cmd, dataJson);
    }

}
