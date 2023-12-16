package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.httpclient.ssl.SslProtocolType;
import org.wxd.boot.httpclient.url.HttpBuilder;

/**
 * 测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-16 10:15
 **/
@Slf4j
public class HttpTest {

    @Test
    public void t1() throws InterruptedException {
        String url = "https://laosiji.swjoy.com/client/api/5712/qr_code.do";

        org.wxd.boot.httpclient.jdk.HttpBuilder.postText(url)
                .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .async(body -> body.systemOut());

        org.wxd.boot.httpclient.jdk.HttpBuilder.postText(url)
                .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request()
                .systemOut();

        Thread.sleep(5000);
    }

    @Test
    public void t2() throws InterruptedException {
        String url = "https://laosiji.swjoy.com/client/api/5712/qr_code.do";

        // HttpBuilder.postText(url)
        //         .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
        //         .asyncByString(body -> log.debug("{}", body));

        HttpBuilder.get("https://laosiji.swjoy.com/client/api/5712/qr_code.do?data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request().systemOut();

        HttpBuilder.postText(url)
                .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request()
                .systemOut();

        HttpBuilder.get("http://127.0.0.1:18800/sjcq/proxySw?data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request().systemOut();

        HttpBuilder.postText("http://127.0.0.1:18800/sjcq/proxySw")
                .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request()
                .systemOut();

        Thread.sleep(5000);
    }

    @Test
    public void t3() throws Exception {
        HttpBuilder.postText("http://47.108.81.97:18800/sjcq/proxySw")
                .ssl(SslProtocolType.TLSV12)
                .paramText("data=eyJndWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b")
                .request()
                .systemOut();
    }

    @Test
    public void t4() throws Exception {
        for (int i = 0; i < 4000; i++) {

            HttpBuilder.postText("http://127.0.0.1:18800/sjcq/crossState")
                    .ssl(SslProtocolType.TLSV12)
                    .paramText("")
                    .request()
                    .systemOut();

            Thread.sleep(1);
        }
    }

}
