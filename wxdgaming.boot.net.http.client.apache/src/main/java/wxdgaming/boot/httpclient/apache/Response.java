package wxdgaming.boot.httpclient.apache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.net.http.HttpHeadNameType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 基于 java 原生的http 信息请求
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-11-15 12:34
 **/
@Slf4j
@Getter
public final class Response<H extends HttpBase> {

    final H httpBase;
    final String uriPath;
    String postText = null;
    protected CloseableHttpResponse httpResponse;
    protected byte[] bodys = null;

    Response(H httpBase, String uriPath) {
        this.httpBase = httpBase;
        this.uriPath = uriPath;
    }

    public String getHeader(String header) {
        return Optional.ofNullable(this.httpResponse.getFirstHeader(header))
                .map(NameValuePair::getValue)
                .orElse(null);
    }

    public int responseCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }

    public byte[] body() {
        if ("gzip".equalsIgnoreCase(getHeader(HttpHeadNameType.Content_Encoding.getValue()))) {
            return GzipUtil.unGZip(bodys);
        }
        return bodys;
    }

    public String bodyString() {
        return bodyString(StandardCharsets.UTF_8);
    }

    public String bodyString(Charset charset) {
        return new String(body(), charset);
    }

    public RunResult bodySyncJson() {
        return bodySyncJson(StandardCharsets.UTF_8);
    }

    public RunResult bodySyncJson(Charset charset) {
        return RunResult.parse(bodyString(charset));
    }

    public String bodyUnicodeDecodeString() {
        return StringUtils.unicodeDecode(bodyString());
    }

    public Response<H> logDebug() {
        log.debug("res: {} {}", bodyString(), this.toString());
        return this;
    }

    public Response<H> logInfo() {
        log.info("res: {} {}", bodyString(), this.toString());
        return this;
    }

    public Response<H> systemOut() {
        System.out.println("res: " + bodyString() + " " + this.toString());
        return this;
    }

    @Override public String toString() {
        return httpBase.getClass().getSimpleName() + " url: " + uriPath + Optional.ofNullable(postText).map(v -> ", postText: " + postText).orElse("");
    }
}
