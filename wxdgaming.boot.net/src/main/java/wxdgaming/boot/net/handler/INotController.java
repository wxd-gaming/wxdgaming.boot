package wxdgaming.boot.net.handler;


import wxdgaming.boot.net.SocketSession;

/**
 * 找不到监听的调用，通常用 lambda 表达式
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-11 15:27
 **/
public interface INotController<S extends SocketSession> {

    /**
     * <p>true 表示处理成功
     * <p>false 表示处理失败
     */
    boolean notController(S session, int msgID, byte[] bytes);
}
