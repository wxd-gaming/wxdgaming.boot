package org.wxd.boot.net.httpservice;

import com.sun.net.httpserver.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.http.ssl.SslContextServer;
import org.wxd.boot.http.ssl.SslProtocolType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 简易 http 服务器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-23 18:10
 **/
@Getter
public class SimpleHttpServer {

    final HttpServer httpServer;
    final HttpsServer httpsServer;

    SimpleHttpServer(HttpServer httpServer, HttpsServer httpsServer) {
        this.httpServer = httpServer;
        this.httpsServer = httpsServer;
    }

    public SimpleHttpServer start() {
        httpOpt().ifPresent(HttpServer::start);
        httpsOpt().ifPresent(HttpServer::start);
        return this;
    }

    public SimpleHttpServer close() {
        httpOpt().ifPresent(s -> s.stop(3));
        httpsOpt().ifPresent(s -> s.stop(3));
        return this;
    }

    public Optional<HttpServer> httpOpt() {
        return Optional.ofNullable(httpServer);
    }

    public Optional<HttpsServer> httpsOpt() {
        return Optional.ofNullable(httpsServer);
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {

        public static Builder of() {
            return new Builder();
        }

        Builder() {
        }

        int port = 0;
        int sslPort = 0;
        SslProtocolType sslType = SslProtocolType.SSL;
        String jksPath = null;
        String jksPwdPath = null;
        Executor executor = null;
        HttpHandler handler;

        public SimpleHttpServer build() {
            try {

                if (executor == null) {
                    executor = Executors.newFixedThreadPool(10);
                }
                HttpHandler tmp = new HttpHandler() {
                    @Override public void handle(HttpExchange exchange) throws IOException {
                        try {
                            handler.handle(exchange);
                        } catch (Exception e) {

                        }
                    }
                };

                HttpsServer httpsServer = null;
                if (sslPort > 0) {
                    httpsServer = HttpsServer.create(new InetSocketAddress(sslPort), 0);

                    httpsServer.setHttpsConfigurator(new HttpsConfigurator(
                            SslContextServer.sslContext(sslType, jksPath, jksPwdPath)
                    ));
                    httpsServer.setExecutor(executor);
                    httpsServer.createContext("/", handler);
                }

                HttpServer httpServer = null;
                if (port > 0) {
                    httpServer = HttpServer.create(new InetSocketAddress(port), 0);
                    httpServer.setExecutor(executor);
                    httpServer.createContext("/", handler);
                }

                if (httpsServer == null && httpServer == null) throw new RuntimeException("port = 0 and ssl port = 0");

                return new SimpleHttpServer(httpServer, httpsServer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
