package org.wxd.boot.starter.service;

import org.wxd.boot.ann.Sort;
import org.wxd.boot.net.web.hs.HttpServer;
import org.wxd.boot.starter.HttpConfig;
import org.wxd.boot.starter.IocContext;
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

    public HsService(HttpConfig config) throws Exception {
        setName(config.getName())
                .setHost(config.getHost())
                .setWanIp(config.getWanIp())
                .setPort(config.getPort())
                .setSslType(config.sslProtocolType())
                .setSslContext(config.sslContext())
                .setNeedCache(config.isNeedCache())
                .setResourcesPath(config.getResourcesPath())
                .initBootstrap();

        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            for (WebConfig.Header header : config.getHeaders()) {
                getHeaderMap().put(header.getKey(), header.getValue());
            }
        }
    }

    @Sort(999999)
    @Override public void start(IocContext iocInjector) throws Exception {
        open();
    }

    @Sort(1)
    @Override public void shutdown() throws Exception {
        close();
    }

}
