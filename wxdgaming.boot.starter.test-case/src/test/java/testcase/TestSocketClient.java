package testcase;

import io.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.net.controller.MappingProtoAction;
import wxdgaming.boot.net.controller.MappingTextAction;
import wxdgaming.boot.net.handler.ResRemoteHandler;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.message.RpcEvent;
import wxdgaming.boot.net.ts.TcpClient;
import wxdgaming.boot.net.ts.TcpSession;

/**
 * socket
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 19:45
 **/
public class TestSocketClient {

    TcpClient<TcpSession> client = null;

    @Before
    public void init() throws Exception {
        if (client != null) return;

        MessagePackage.loadMessageId_HashCode("wxdgaming");

        /*手动注册处理器*/
        ResRemoteHandler resRemoteHandler = new ResRemoteHandler();
        MappingTextAction.bindCmd(resRemoteHandler);
        MappingProtoAction.register(resRemoteHandler);

        client = new TcpClient<TcpSession>()
                .setName("test")
                .setHost("127.0.0.1")
                .setPort(17000);

        ChannelFuture connect = client.initBootstrap()
                .connect();
        connect.sync();
        Thread.sleep(1000);


    }

    @Test
    public void sendRpc() throws Exception {
        TcpSession session = client.getAllSessionQueue().idle();
        RpcEvent rpc = session.rpc("/rpc/hello", MapOf.newJSONObject("key", "1").fluentPut("value", "2"));
        String string = rpc.get();
        System.out.println(string);
        Thread.sleep(1000);
    }

}
