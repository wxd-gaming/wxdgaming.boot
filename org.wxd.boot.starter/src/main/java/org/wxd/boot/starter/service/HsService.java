package org.wxd.boot.starter.service;

import org.wxd.boot.ann.Sort;
import org.wxd.boot.net.web.hs.HttpServer;
import org.wxd.boot.starter.InjectorContext;
import org.wxd.boot.starter.WebConfig;
import org.wxd.boot.starter.i.IShutdown;
import org.wxd.boot.starter.i.IStart;

/**
 * http 服务
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 17:51
 **/
public class HsService extends HttpServer implements IStart, IShutdown {

    public HsService(WebConfig webConfig) throws Exception {
        setName(webConfig.getName())
                .setHost(webConfig.getHost())
                .setWanIp(webConfig.getWanIp())
                .setPort(webConfig.getPort())
                .setSslType(webConfig.sslProtocolType())
                .setSslContext(webConfig.sslContext())
                .setResourcesPath(webConfig.getResourcesPath())
                .initExecutor(webConfig.getThreadCoreSize())
                .initBootstrap();

        if (webConfig.getHeaders() != null && !webConfig.getHeaders().isEmpty()) {
            for (WebConfig.Header header : webConfig.getHeaders()) {
                getHeaderMap().put(header.getKey(), header.getValue());
            }
        }
    }

    @Sort(999999)
    @Override public void start(InjectorContext iocInjector) throws Exception {
        open();
    }

    @Sort(1)
    @Override public void shutdown() throws Exception {
        close();
    }

}
