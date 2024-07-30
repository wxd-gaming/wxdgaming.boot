package org.wxd.boot.starter.webapi;

import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.controller.ann.TextController;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.http.HttpHeadValueType;
import wxdgaming.boot.net.web.hs.HttpSession;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.i.IBeanInit;
import wxdgaming.boot.starter.service.HsService;
import wxdgaming.boot.starter.service.TsService;

import java.io.File;
import java.io.InputStream;

/**
 * 公共
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-13 18:50
 **/
@TextController()
public class ServerApi implements IBeanInit {

    @Inject TsService tsService;
    @Inject HsService hsService;

    @Override public void beanInit(IocContext iocContext) throws Exception {

    }

    @TextMapping(remarks = "sdk index")
    public String index(HttpSession httpSession, ObjMap objMap) {
        return "holle";
    }

    @TextMapping(remarks = "sdk s1", match = true)
    public String s1(HttpSession httpSession, ObjMap objMap) {
        String string = objMap.getString("name");
        String key = objMap.getString("key");
        long time = objMap.getLongValue("time");
        if (!httpSession.getUriPath().contains("/aabb/")) {
            httpSession.response(HttpResponseStatus.INTERNAL_SERVER_ERROR, "key error");
            return null;
        }
        String jarName = new File(httpSession.getUri().getPath()).getName();
        Record2<String, InputStream> inputStream = FileUtil.findInputStream(
                this.getClass().getClassLoader(),
                "jar/" + jarName
        );
        if (inputStream == null) {
            httpSession.response(HttpResponseStatus.NOT_FOUND, "not found jar");
            return null;
        }
        byte[] bytes = FileReadUtil.readBytes(inputStream.t2());
        httpSession.response(HttpHeadValueType.OctetStream, bytes);
        return null;
    }

    @TextMapping(remarks = "sdk处理", needAuth = 1)
    public String sdk(HttpSession httpSession, ObjMap objMap) {
        return "sdk";
    }

}
