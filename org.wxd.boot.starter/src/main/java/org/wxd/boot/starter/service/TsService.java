package org.wxd.boot.starter.service;

import org.wxd.boot.ann.Sort;
import org.wxd.boot.net.ts.TcpServer;
import org.wxd.boot.net.ts.TcpSession;
import org.wxd.boot.starter.InjectorContext;
import org.wxd.boot.starter.ServerConfig;
import org.wxd.boot.starter.i.IShutdown;
import org.wxd.boot.starter.i.IStart;

/**
 * tcp server
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 18:20
 **/
public class TsService extends TcpServer<TcpSession> implements IStart, IShutdown {

    public TsService(ServerConfig serverConfig) throws Exception {
        setName(serverConfig.getName())
                .setHost(serverConfig.getHost())
                .setWanIp(serverConfig.getWanIp())
                .setPort(serverConfig.getPort())
                .setSslType(serverConfig.sslProtocolType())
                .setSslContext(serverConfig.sslContext())
                .initBootstrap();
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
