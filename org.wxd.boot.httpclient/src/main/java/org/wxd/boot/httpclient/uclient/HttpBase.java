package org.wxd.boot.httpclient.uclient;

import io.netty.util.AsciiString;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.zip.GzipUtil;
import org.wxd.boot.httpclient.HttpContentType;
import org.wxd.boot.httpclient.ssl.SslContextClient;
import org.wxd.boot.httpclient.ssl.SslProtocolType;
import org.wxd.boot.str.StringUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-11-15 12:17
 **/
@Slf4j
@Setter
@Accessors(chain = true)
public abstract class HttpBase<H extends HttpBase> {

    protected final UrlResponse urlResponse;
    protected HttpContentType httpContentType = HttpContentType.Application;
    protected SslProtocolType sslProtocolType = SslProtocolType.SSL;
    protected final Map<String, String> reqHeaderMap = new LinkedHashMap<>();
    protected int connTimeout = 3000;
    protected int readTimeout = 3000;
    protected int retry = 1;
    /** 分段传输协议 */
    protected String boundary = null;
    protected String reqHttpMethod;

    protected HttpBase(String uriPath) {
        urlResponse = new UrlResponse(this, uriPath);
        addHeader("accept-encoding", "gzip");
        addHeader("user-agent", "java.org.wxd j21");
    }

    /** 处理需要发送的数据 */
    protected void writer(HttpURLConnection urlConnection) throws Exception {

    }

    public UrlResponse request() {
        Throwable throwable = null;
        int r = 1;
        for (; r <= retry; r++) {
            openURLConnection();
            try {
                writer(this.urlResponse.urlConnection);
                /*开始读取内容*/
                this.urlResponse.responseCode();
                InputStream inputStream;
                if (this.urlResponse.urlConnection.getInputStream() == null && this.urlResponse.urlConnection.getErrorStream() != null) {
                    inputStream = this.urlResponse.urlConnection.getErrorStream();
                } else {
                    inputStream = this.urlResponse.urlConnection.getInputStream();
                }
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[512];
                    int len = -1;
                    while ((len = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    byte[] toByteArray = bos.toByteArray();
                    String encoding = this.urlResponse.urlConnection.getContentEncoding();
                    if (encoding != null
                            && encoding.toLowerCase().contains("gzip")
                            && toByteArray.length > 0) {
                        this.urlResponse.bodys = GzipUtil.unGZip(toByteArray);
                    } else {
                        this.urlResponse.bodys = toByteArray;
                    }
                } finally {
                    inputStream.close();
                }
                return this.urlResponse;
            } catch (Throwable e) {
                throwable = e;
            } finally {
                if (this.urlResponse.urlConnection != null) {
                    this.urlResponse.urlConnection.disconnect();
                }
            }
        }
        throw Throw.as(this.urlResponse.toString() + ", 重试：" + r, throwable);
    }

    protected void openURLConnection() {
        try {
            URL realUrl = new URI(this.urlResponse.uriPath).toURL();

            this.urlResponse.urlConnection = (HttpURLConnection) realUrl.openConnection();

            if (this.urlResponse.urlConnection instanceof HttpsURLConnection httpsURLConnection) {
                httpsURLConnection.setSSLSocketFactory(SslContextClient.sslContext(sslProtocolType).getSocketFactory());
                httpsURLConnection.setHostnameVerifier(new TrustAnyHostnameVerifier());
            }

            this.urlResponse.urlConnection.setUseCaches(true);
            this.urlResponse.urlConnection.setConnectTimeout(connTimeout);
            this.urlResponse.urlConnection.setReadTimeout(readTimeout);

            if (httpContentType == HttpContentType.Multipart) {
                boundary = StringUtil.getRandomString(15);
                reqHeaderMap.put("content-type", httpContentType.toString() + "; boundary=" + boundary);
            } else {
                reqHeaderMap.put("content-type", httpContentType.toString());
            }

            for (Map.Entry<String, String> headerEntry : reqHeaderMap.entrySet()) {
                this.urlResponse.urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }

            addHeader("connection", "close");
            /*
            必须设置false，否则会自动redirect到重定向后的地址
            conn.setInstanceFollowRedirects(false);
             */

            /*get or post*/
            this.urlResponse.urlConnection.setRequestMethod(reqHttpMethod);
            if (log.isDebugEnabled()) {
                log.debug(reqHttpMethod + " " + this.urlResponse.uriPath);
                final String collect = this.urlResponse.urlConnection
                        .getRequestProperties()
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + ":" + String.join("=", entry.getValue()))
                        .collect(Collectors.joining(", "));

                log.debug("http head：" + collect);
            }
            this.urlResponse.urlConnection.setDoInput(true);
            this.urlResponse.urlConnection.setDoOutput(true);

        } catch (Exception e) {
            throw Throw.as("请求的url：" + this.urlResponse.uriPath, e);
        }
    }


    /**
     * 设置参数头
     *
     * @param headerKey   采用这个 HttpHeaderNames
     * @param HeaderValue
     * @return
     */
    public H addHeader(AsciiString headerKey, String HeaderValue) {
        addHeader(headerKey.toString(), HeaderValue);
        return (H) this;
    }

    public H addHeader(String headerKey, String HeaderValue) {
        this.reqHeaderMap.put(headerKey, HeaderValue);
        return (H) this;
    }

    /**
     * 获取文件的上传类型，图片格式为image/png,image/jpg等。非图片为application/octet-stream
     *
     * @param f
     * @return
     * @throws Exception
     */
    protected HttpContentType getContentType(File f) throws Exception {
        String fileName = f.getName();
        String extendName = fileName.substring(fileName.indexOf(".") + 1).toLowerCase();
        switch (extendName) {
            case "htm":
            case "html":
            case "jsp":
            case "asp":
            case "aspx":
            case "xhtml":
                return HttpContentType.Html;
            case "css":
                return HttpContentType.CSS;
            case "js":
                return HttpContentType.Javascript;
            case "xml":
                return HttpContentType.Xml;
            case "json":
                return HttpContentType.Json;
            case "xjson":
                return HttpContentType.XJson;
            case "ico":
                return HttpContentType.ICO;
            case "icon":
                return HttpContentType.ICON;
            case "gif":
                return HttpContentType.GIF;
            case "jpg":
            case "jpe":
            case "jpeg":
                return HttpContentType.JPG;
            case "png":
                return HttpContentType.PNG;
        }
        return HttpContentType.OctetStream;
    }

    protected static class TrustAnyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }
}


