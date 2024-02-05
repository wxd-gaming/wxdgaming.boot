package org.wxd.boot.starter.webapi;

import org.wxd.boot.core.collection.ObjMap;
import org.wxd.boot.core.lang.RandomUtils;
import org.wxd.boot.core.threading.Async;
import org.wxd.boot.core.threading.ExecutorLog;
import org.wxd.boot.core.timer.ann.Scheduled;
import org.wxd.boot.net.controller.ann.TextController;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.net.web.hs.HttpSession;

/**
 * 公共
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-13 18:50
 **/
@TextController
public class PublicApi {

    @TextMapping(remarks = "公共接口")
    public String index(HttpSession httpSession, ObjMap objMap) {
        return "holle";
    }

    @TextMapping(remarks = "test")
    public void test0(HttpSession httpSession, ObjMap objMap) throws Exception {
        httpSession.responseOver();
        httpSession.responseText("test");
    }

    @TextMapping(remarks = "test")
    public String test1(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(6);
        Thread.sleep(random);
        return "test";
    }

    @TextMapping(remarks = "test")
    public String test2(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(6);
        Thread.sleep(random);
        return "test";
    }

    @Async(vt = true)
    @TextMapping(remarks = "test")
    public String test3(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(6);
        Thread.sleep(random);
        return "test";
    }

    @Scheduled
    @Async(vt = true)
    @ExecutorLog(logTime = 33)
    public void s1() throws Exception {
    }

}
