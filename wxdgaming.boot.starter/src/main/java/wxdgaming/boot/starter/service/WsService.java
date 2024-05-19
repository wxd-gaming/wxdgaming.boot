package wxdgaming.boot.starter.service;

import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.net.web.ws.WebSession;
import wxdgaming.boot.net.web.ws.WebSocketServer;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.KV;
import wxdgaming.boot.starter.WebConfig;
import wxdgaming.boot.starter.i.IShutdown;
import wxdgaming.boot.starter.i.IStart;

/**
 * web socket service
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 17:49
 **/
public class WsService extends WebSocketServer<WebSession> implements IStart, IShutdown {

    public WsService(WebConfig config) throws Exception {
        setName(config.getName())
                .setHost(config.getHost())
                .setWanIp(config.getWanIp())
                .setPort(config.getPort())
                .setSslType(config.sslProtocolType())
                .setSslContext(config.sslContext())
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
