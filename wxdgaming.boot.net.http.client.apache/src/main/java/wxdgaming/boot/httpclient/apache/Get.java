package wxdgaming.boot.httpclient.apache;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * 基于apache的http get 请求
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-04-28 16:02
 **/
@Slf4j
public class Get extends HttpBase<Get> {

    public Get(HttpClientPool httpClientPool, String uriPath) {
        super(httpClientPool, uriPath);
    }

    @Override public void request0() throws IOException {
        HttpGet get = createGet();
        if (log.isDebugEnabled()) {
            log.info("send url={}", url());
        }
        response.httpResponse = httpClientPool.getCloseableHttpClient().execute(get);
        HttpEntity entity = response.httpResponse.getEntity();
        response.bodys = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
    }

}
