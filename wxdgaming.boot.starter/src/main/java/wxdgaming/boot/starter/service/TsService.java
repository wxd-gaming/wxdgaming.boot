package wxdgaming.boot.starter.service;

import io.netty.buffer.ByteBuf;
import wxdgaming.boot.core.ann.Sort;
import wxdgaming.boot.net.ts.TcpServer;
import wxdgaming.boot.net.ts.TcpSession;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.TcpConfig;
import wxdgaming.boot.starter.i.IShutdown;
import wxdgaming.boot.starter.i.IStart;

/**
 * tcp server
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-11 18:20
 **/
public class TsService extends TcpServer<TcpSession> implements IStart, IShutdown {

    public TsService(TcpConfig config) throws Exception {
        setName(config.getName())
                .setHost(config.getHost())
                .setWanIp(config.getWanIp())
                .setPort(config.getPort())
                .setSslType(config.sslProtocolType())
                .setSslContext(config.sslContext())
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

    @Override public void read(TcpSession session, ByteBuf byteBuf) {
        super.read(session, byteBuf);
    }

}
