package org.wxd.boot.net.web.hs.uclient;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.net.web.hs.HttpContentType;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-05 12:55
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class UrlPost extends HttpBase {

    protected UrlPost(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = HttpMethod.POST;
    }

    @Override
    protected void writer(HttpURLConnection urlConnection) throws Exception {
        OutputStream outputStream = urlConnection.getOutputStream();
        OutputStreamWriter outWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writeTextParams(outputStream, outWriter);
        writesEnd(outputStream, outWriter);
    }

    /** 普通字符串数据 */
    protected void writeTextParams(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {

    }

    /** 添加结尾数据 */
    protected void writesEnd(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {
        if (httpContentType == HttpContentType.Multipart) {
            outWriter.append("--").append(boundary).append("--").append("\r\n");
            outWriter.append("\r\n");
        }
    }

}
