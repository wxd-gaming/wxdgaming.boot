package wxdgaming.boot.net.controller;

/**
 * 处理器检查器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-02-03 21:25
 **/
public interface IController {

    /** 数量超过上限预警 */
    default int execWarnCount() {return 10;}

}
