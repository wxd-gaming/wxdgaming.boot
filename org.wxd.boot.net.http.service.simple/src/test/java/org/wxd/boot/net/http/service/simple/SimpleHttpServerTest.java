package org.wxd.boot.net.http.service.simple;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import junit.framework.TestCase;
import org.wxd.boot.net.http.ssl.SslProtocolType;
import org.wxd.boot.net.http.service.simple.SimpleHttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleHttpServerTest extends TestCase {

    public static void main(String[] args) {
        HttpHandler handler = new HttpHandler() {
            @Override public void handle(HttpExchange exchange) throws IOException {
                StringBuilder stringBuilder = new StringBuilder(5 * 1024 * 1024);
                for (int i = 0; i < 5 * 1024 * 1024; i++) {
                    stringBuilder.append("i");
                }
                String ok = stringBuilder.toString();
                byte[] bytes = ok.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            }
        };

        SimpleHttpServer.Builder
                .of()
                .setPort(80)
                .setSslPort(443)
                .setSslType(SslProtocolType.SSL)
                .setJksPath("xiaw-jks/xiaw.net-2023-07-15.jks")
                .setJksPwdPath("xiaw-jks/xiaw.net-2023-07-15-pwd.txt")
                .setHandler(handler)
                .build()
                .start();
    }
}