package wxdgaming.boot.net.http.service.simple;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.net.http.ssl.SslContextServer;
import wxdgaming.boot.net.http.ssl.SslProtocolType;

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
        HttpHandler handler = new MyHandler();

        public SimpleHttpServer build() {
            try {

                Executor executor = Executors.newFixedThreadPool(3);

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
