package org.wxd.boot.starter.service;

import io.netty.buffer.ByteBuf;
import org.wxd.boot.ann.Sort;
import org.wxd.boot.net.controller.cmd.ITokenCache;
import org.wxd.boot.net.ts.TcpServer;
import org.wxd.boot.net.ts.TcpSession;
import org.wxd.boot.starter.IocContext;
import org.wxd.boot.starter.TcpConfig;
import org.wxd.boot.starter.i.IShutdown;
import org.wxd.boot.starter.i.IStart;

/**
 * tcp server
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 18:20
 **/
public class TsService extends TcpServer<TcpSession> implements IStart, IShutdown {

    public TsService(TcpConfig tcpConfig) throws Exception {
        setName(tcpConfig.getName())
                .setHost(tcpConfig.getHost())
                .setWanIp(tcpConfig.getWanIp())
                .setPort(tcpConfig.getPort())
                .setSslType(tcpConfig.sslProtocolType())
                .setSslContext(tcpConfig.sslContext())
                .initBootstrap();
    }

    @Sort(999999)
    @Override public void start(IocContext iocInjector) throws Exception {
        open();
    }

    @Sort(1)
    @Override public void shutdown() throws Exception {
        close();
    }

    @Override public void read(ITokenCache tokenCache, TcpSession session, ByteBuf byteBuf) {
        super.read(tokenCache, session, byteBuf);
    }

}
