package org.wxd.boot.starter.webapi;

import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.RandomUtils;
import wxdgaming.boot.core.threading.ThreadInfo;
import wxdgaming.boot.core.threading.ExecutorLog;
import wxdgaming.boot.core.timer.ann.Scheduled;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.web.hs.HttpSession;

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

    @ThreadInfo(vt = true)
    @TextMapping(remarks = "test")
    public String test3(HttpSession httpSession, ObjMap objMap) throws Exception {
        int random = RandomUtils.random(6);
        Thread.sleep(random);
        return "test";
    }

    @Scheduled
    @ThreadInfo(vt = true)
    @ExecutorLog(logTime = 33)
    public void s1() throws Exception {
    }

    @Scheduled("0 0 0")/*每小时执行*/
    @ThreadInfo(vt = true)/*因为耗时，异步交给虚拟线程执行*/
    @ExecutorLog(logTime = 33)/*如果执行超过33毫秒就开始加入监控日志*/
    public void s2() throws Exception {
    }

}
