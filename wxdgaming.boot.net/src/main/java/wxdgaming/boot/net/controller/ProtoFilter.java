package wxdgaming.boot.net.controller;

/**
 * proto 消息 过滤器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-10 16:38
 **/
public abstract class ProtoFilter {


    public abstract boolean doFilter(ProtoListenerAction listenerAction);

}
