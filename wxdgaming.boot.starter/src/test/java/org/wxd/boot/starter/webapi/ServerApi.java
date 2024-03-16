package org.wxd.boot.starter.webapi;

import com.google.inject.Inject;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.web.hs.HttpSession;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.i.IBeanInit;
import wxdgaming.boot.starter.service.HsService;
import wxdgaming.boot.starter.service.TsService;

/**
 * 公共
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-13 18:50
 **/
@TextController()
public class ServerApi implements IBeanInit {

    @Inject TsService tsService;
    @Inject HsService hsService;

    @Override public void beanInit(IocContext iocContext) throws Exception {

    }

    @TextMapping(remarks = "sdk处理")
    public String index(HttpSession httpSession, ObjMap objMap) {
        return "holle";
    }

    @TextMapping(remarks = "sdk处理", needAuth = 1)
    public String sdk(HttpSession httpSession, ObjMap objMap) {
        return "sdk";
    }

}
