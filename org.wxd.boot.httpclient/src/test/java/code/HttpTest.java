package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.httpclient.jclient.JHttpBuilder;
import org.wxd.boot.httpclient.jclient.JPostText;

/**
 * 测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-16 10:15
 **/
@Slf4j
public class HttpTest {

    @Test
    public void t1(){
        String url = "https://laosiji.swjoy.com/client/api/5712/qr_code.do";
        JPostText request = JHttpBuilder.postText(url).paramText("data=eyJn4dWlkIjoiNTcxMl8xNjg5MjEwNTQ0ODUxXzk2OTk5MjI2Iiwib3JkZXJObyI6IjhhZTYyMjgxZTdjZjQzOTU4ODgxZGU0ODIxZTZjMWE2Iiwicm1iIjoxMiwiaWR4IjoiNzIiLCJ0aW1lIjoxNzAyNjkzNTQ0LCJzd1RhZyI6InN3Iiwicm9sZUlkIjoiMTU4MzYxMzQwMzM0NjIzOTUyNiJ9&sign=7395de180756be1fd5d249e19bf10b5b").request();
        String s = request.bodyString();
        log.debug("{}",s);
    }

}
