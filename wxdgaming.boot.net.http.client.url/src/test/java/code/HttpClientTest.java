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

}
