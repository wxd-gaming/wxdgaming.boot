package org.wxd.boot.starter.webapi;

import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RandomUtils;
import org.wxd.boot.net.controller.ann.TextController;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.net.web.hs.HttpSession;
import org.wxd.boot.threading.Async;

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
    public String test0(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(30);
        Thread.sleep(random);
        return "test";
    }

    @Async(queue = "q1")
    @TextMapping(remarks = "test")
    public String test1(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(30);
        Thread.sleep(random);
        return "test";
    }

    @Async(queue = "q2")
    @TextMapping(remarks = "test")
    public String test2(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(30);
        Thread.sleep(random);
        return "test";
    }

    @Async(queue = "q3")
    @TextMapping(remarks = "test")
    public String test3(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(30);
        Thread.sleep(random);
        return "test";
    }

}
