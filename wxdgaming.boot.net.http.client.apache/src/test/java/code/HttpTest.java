package code;

import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;
import wxdgaming.boot.httpclient.apache.HttpBuilder;

/**
 * 测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-08-02 11:48
 **/
public class HttpTest {

    @Test
    @RepeatedTest(20)
    public void httpTest() {

        String string = HttpBuilder.postText(
                        "http://127.0.0.1:19000",
                        "token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbk5hbWUiOiIxMzMwMDAwMDAwMCIsImlzcyI6InFqX3N0YXJ0ZXIiLCJleHAiOjE3MjMxNzM4MDEsInR5cGUiOjEsImlhdCI6MTcyMjU2OTAwMSwidXNlcklkIjoxfQ.yvQW_JwzaEIUgjdWLfIEftB0XolbUB-UDQNSQGlWP78")
                .setContentType(ContentType.APPLICATION_FORM_URLENCODED)
                .request()
                .bodyString();
        System.out.println(string);
    }

}
