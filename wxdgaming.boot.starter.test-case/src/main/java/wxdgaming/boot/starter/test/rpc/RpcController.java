package wxdgaming.boot.starter.test.rpc;

import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;

/**
 * rpc控制器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 19:51
 **/
@TextController
public class RpcController {

    @TextMapping
    public String hello() {
        return "hello";
    }

}
