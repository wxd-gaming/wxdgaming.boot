package org.wxd.boot.httpclient.uclient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.httpclient.HttpHeadValueType;
import org.wxd.boot.str.StringUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

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
public class UrlPostText extends UrlPost {

    protected UrlPostText(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = "POST";
    }

    /** 设置提交的参数 */
    public UrlPostText setParams(String params) {
        this.urlResponse.postText = params;
        return this;
    }

    /** 设置提交的参数 */
    public UrlPostText setJson(String json) {
        this.urlResponse.postText = json;
        this.httpHeadValueType = HttpHeadValueType.Json;
        return this;
    }

    /** 设置提交的参数 */
    public UrlPostText setXJson(String json) {
        this.urlResponse.postText = json;
        this.httpHeadValueType = HttpHeadValueType.XJson;
        return this;
    }

    @Override protected void writeTextParams(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {
        if (StringUtil.notEmptyOrNull(this.urlResponse.postText)) {
            outWriter.write(this.urlResponse.postText);
            if (log.isDebugEnabled()) {
                log.debug("http send：" + this.urlResponse.postText);
            }
        }
    }

}
