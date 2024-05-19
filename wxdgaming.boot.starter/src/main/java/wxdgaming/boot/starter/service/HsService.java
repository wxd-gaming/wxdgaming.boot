package wxdgaming.boot.starter.service;

import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.net.web.hs.HttpServer;
import wxdgaming.boot.starter.HttpConfig;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.KV;
import wxdgaming.boot.starter.i.IShutdown;
import wxdgaming.boot.starter.i.IStart;

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
            for (KV header : config.getHeaders()) {
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
