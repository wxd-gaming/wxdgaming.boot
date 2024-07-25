package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.publisher.Mono;
import wxdgaming.boot.net.http.HttpHeadNameType;
import wxdgaming.boot.net.http.client.url.Get;
import wxdgaming.boot.net.http.client.url.HttpBuilder;
import wxdgaming.boot.net.http.client.url.Response;

import java.io.File;

@Slf4j
public class HttpClientTest {

    @Test
    public void t1() throws Exception {

        String url = "http://test-center.xiaw.net:18800/sjcq/wanIp";
        log.info("{}", HttpBuilder.get(url).request().bodyString());
        log.info("{}", HttpBuilder.get(url).retry(2)/*设置重试次数，比如请求失败了*/.request().bodyString());

        Mono<Response<Get>> async = HttpBuilder.get(url).retry(2).async();
        async
                .subscribe(response -> {
                    log.info(response.bodyString());
                })
                .onError(throwable -> log.info("{}", url, throwable));

        HttpBuilder.postMulti(url)
                .header(HttpHeadNameType.AUTHORIZATION, "ddddd")
                .putParams("1", "1")
                .asyncString(bodyString -> log.info(bodyString));

        HttpBuilder.postFile(url, new File("target/logs/app.log"))
                .header(HttpHeadNameType.AUTHORIZATION, "ddddd")
                .putParams("1", "1");

        Thread.sleep(3000);

    }

    @Test
    public void t3() throws Exception {
        for (int i = 0; i < 100; i++) {

            String url = "http://127.0.0.1:18801/onlyoffice/callback";
            String json = "{\n" +
                    "    \"changesurl\": \"https://documentserver/url-to-changes.zip\",\n" +
                    "    \"forcesavetype\": 0,\n" +
                    "    \"history\": {\n" +
                    "        \"changes\": \"changes\",\n" +
                    "        \"serverVersion\": \"serverVersion\"\n" +
                    "    },\n" +
                    "    \"filetype\": \"docx\",\n" +
                    "    \"key\": \"Khirz6zTPdfd7\",\n" +
                    "    \"status\": 6,\n" +
                    "    \"url\": \"http://127.0.0.1:18801/onlyoffice/callback/url-to-changes.zip\",\n" +
                    "    \"users\": [\"6d5a81d0\"],\n" +
                    "    \"userdata\": \"sample userdata\"\n" +
                    "}";
            HttpBuilder.postJson(url, json)
                    .asyncString(bodyString -> log.info(bodyString));

        }
        Thread.sleep(3000);
    }

}
