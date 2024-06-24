package org.wxd.boot.net.http.service.simple;

import junit.framework.TestCase;
import wxdgaming.boot.net.http.service.simple.MyHandler;
import wxdgaming.boot.net.http.service.simple.SimpleHttpServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

public class SimpleHttpServerTest extends TestCase {

    public static void main(String[] args) {

        SimpleHttpServer.Builder
                .of()
                .setPort(10801)
                .setSslPort(10443)
                .setSslType(SslProtocolType.SSL)
                .setJksPath("xiaw-jks/xiaw.net-2023-07-15.jks")
                .setJksPwdPath("xiaw-jks/xiaw.net-2023-07-15-pwd.txt")
                .build()
                .start();
    }

}