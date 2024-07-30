package wxdgaming.boot.starter.client;

import wxdgaming.boot.net.ts.TcpClient;
import wxdgaming.boot.net.ts.TcpSession;
import wxdgaming.boot.starter.TcpConfig;

/**
 * tcp client
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-06-10 18:45
 **/
public class TsClient extends TcpClient<TcpSession> {

    public TsClient(TcpConfig tcpConfig) {

        setName(tcpConfig.getName());
        setHost("127.0.0.1");
        setWanIp(tcpConfig.getWanIp());
        setPort(10001);

    }


}
