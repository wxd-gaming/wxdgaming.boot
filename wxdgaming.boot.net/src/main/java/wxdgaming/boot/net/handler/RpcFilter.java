package wxdgaming.boot.net.handler;

/**
 * rpc 过滤器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-10 16:38
 **/
public abstract class RpcFilter {


    public abstract boolean doFilter(RpcListenerAction rpcListenerAction);

}
