package wxdgaming.boot.starter.i;

/**
 * 服务器关闭的时候会调用
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-02-22 14:45
 **/
public interface IShutdown {

    /**
     * 服务器关闭的时候会调用
     */
    void shutdown() throws Exception;

}
