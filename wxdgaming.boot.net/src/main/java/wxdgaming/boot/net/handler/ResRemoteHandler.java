package wxdgaming.boot.net.handler;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.ProtoMapping;
import wxdgaming.boot.net.message.RpcEvent;
import wxdgaming.boot.net.message.rpc.ResRemote;

/**
 * 处理 ReqRemote 消息
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 17:25
 **/
@Slf4j
@ProtoController
public class ResRemoteHandler {

    @ProtoMapping
    public void action(SocketSession session, ResRemote reqRemote) {
        if (reqRemote.getRpcId() > 0) {
            String params = reqRemote.getParams();
            if (reqRemote.getGzip() == 1) {
                params = GzipUtil.unGzip2String(params);
            }
            RpcEvent syncRequest = RpcEvent.RPC_REQUEST_CACHE_PACK.getIfPresent(reqRemote.getRpcId());
            if (syncRequest != null) {
                syncRequest.response(reqRemote.getParams());
            } else {
                log.info(
                        "{} 同步消息回来后，找不到同步对象 {}, rpcId={}, params={}",
                        session.toString(),
                        this.toString(),
                        reqRemote.getRpcId(),
                        params,
                        new RuntimeException()
                );
            }
        }
    }

}
