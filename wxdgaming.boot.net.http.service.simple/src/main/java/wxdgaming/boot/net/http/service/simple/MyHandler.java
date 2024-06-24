package wxdgaming.boot.net.http.service.simple;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MyHandler implements HttpHandler {

    @Override public void handle(HttpExchange exchange) throws IOException {
        Executors.getDefaultExecutor().execute(new Event() {
            @Override public long getLogTime() {
                return 1;
            }

            @Override public void onEvent() throws Exception {
                int k = 5;
                StringBuilder stringBuilder = new StringBuilder(k);

                InputStream requestBody = exchange.getRequestBody();

                for (int i = 0; i < k; i++) {
                    stringBuilder.append("i");
                }
                String ok = stringBuilder.toString();
                byte[] bytes = ok.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("content-type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.getResponseBody().flush();
                exchange.close();
            }
        });
    }

}
