package org.wxd.boot.net.http.service.simple;

import com.sun.net.httpserver.HttpHandler;
import junit.framework.TestCase;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.net.http.service.simple.SimpleHttpServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleHttpServerTest extends TestCase {

    public static void main(String[] args) {

        SimpleHttpServer.Builder
                .of()
                .setPort(10801)
                // .setSslPort(10443)
                .setSslType(SslProtocolType.SSL)
                .setExecutor(Executors.getVTExecutor())
                .setJksPath("xiaw-jks/xiaw.net-2023-07-15.jks")
                .setJksPwdPath("xiaw-jks/xiaw.net-2023-07-15-pwd.txt")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
                        System.out.println(System.currentTimeMillis() + " handle " + exchange.toString() + " thread " + Thread.currentThread());
                        String ok = "stringBuilder".toString();
                        byte[] bytes = ok.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("content-type", "application/json");
                        exchange.sendResponseHeaders(200, 0);
                        // exchange.getResponseBody().write(bytes);
                        exchange.getResponseBody().flush();
                        exchange.close();
                    }
                })
                .build()
                .start();
    }

}