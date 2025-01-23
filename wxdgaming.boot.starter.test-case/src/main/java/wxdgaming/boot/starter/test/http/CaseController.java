package wxdgaming.boot.starter.test.http;

import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 19:43
 */
@TextController()
public class CaseController {

    @TextMapping(basePath = "/", path = "index")
    public String index() {
        return "index";
    }

}
