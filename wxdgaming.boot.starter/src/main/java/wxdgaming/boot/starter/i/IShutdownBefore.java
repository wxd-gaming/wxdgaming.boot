package wxdgaming.boot.starter.i;

/**
 * 在执行shutdown之前执行
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-04-16 10:01
 **/
public interface IShutdownBefore {

    /** 在执行shutdown之前执行 */
    void shutdownBefore() throws Exception;

}
