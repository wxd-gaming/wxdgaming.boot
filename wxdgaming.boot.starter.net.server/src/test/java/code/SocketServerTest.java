package code;


import wxdgaming.boot.starter.net.server.SocketConfig;
import wxdgaming.boot.starter.net.server.SocketServer;

import java.util.Scanner;

/**
 * 测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-12 20:56
 **/
public class SocketServerTest {

    public static void main(String[] args) {
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setPort(8080);
        SocketServer socketServer = new SocketServer(socketConfig);
        socketServer.start();
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
    }

}
