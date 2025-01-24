package wxdgaming.boot.starter.test.rpc;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.net.Session;
import wxdgaming.boot.net.controller.ann.Body;
import wxdgaming.boot.net.controller.ann.Param;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;

/**
 * rpc控制器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 19:51
 **/
@Slf4j
@TextController
public class RpcController {

    @TextMapping
    public String hello(Session session,
                        @Body String body,
                        @Param("key") String key,
                        @Param("value") String value,
                        @Param(value = "test", defaultValue = "999") Integer test) {
        log.info("{} {} {} {}", body, key, value, test);
        return "hello";
    }

}
