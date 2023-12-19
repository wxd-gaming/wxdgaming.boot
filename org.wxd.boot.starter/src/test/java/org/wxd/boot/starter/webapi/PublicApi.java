package org.wxd.boot.starter.webapi;

import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RandomUtils;
import org.wxd.boot.net.controller.ann.TextController;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.net.web.hs.HttpSession;
import org.wxd.boot.threading.Async;
import org.wxd.boot.threading.EventRunnable;
import org.wxd.boot.threading.ExecutorVirtualServices;
import org.wxd.boot.threading.Executors;

/**
 * 公共
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-13 18:50
 **/
@TextController
public class PublicApi {

    final ExecutorVirtualServices login_services = Executors.newExecutorVirtualServices("login", 200);

    @TextMapping(remarks = "公共接口")
    public String index(HttpSession httpSession, ObjMap objMap) {
        return "holle";
    }

    @TextMapping(remarks = "test")
    public void test0(HttpSession httpSession, ObjMap objMap) throws Exception {
        httpSession.responseOver();
        login_services.submit(new EventRunnable(httpSession.getUriPath(), 150, 400) {
            @Override public void run() {
                // int random = RandomUtils.random(6);
                // try {
                //     Thread.sleep(random);
                // } catch (InterruptedException e) {
                //     throw new RuntimeException(e);
                // }
                httpSession.getResponseContent().write("test");
                httpSession.response();
            }
        });

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

}
