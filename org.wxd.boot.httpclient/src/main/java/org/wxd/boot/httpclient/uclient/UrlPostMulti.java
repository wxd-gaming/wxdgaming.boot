package org.wxd.boot.httpclient.uclient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.httpclient.HttpDataAction;
import org.wxd.boot.str.StringUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

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
public class UrlPostMulti extends UrlPost {

    protected ObjMap reqMap = new ObjMap();

    protected UrlPostMulti(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = "POST";
    }

    public UrlPostMulti putParams(Object key, Object value) {
        reqMap.put(key, value);
        return this;
    }

    public UrlPostMulti putParams(Map map) {
        reqMap.putAll(map);
        return this;
    }

    protected void writeTextParams(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {
        if (reqMap.isEmpty()) return;
        this.urlResponse.postText = HttpDataAction.httpDataEncoder(reqMap);
        if (StringUtil.notEmptyOrNull(this.urlResponse.postText)) {
            outWriter.write(this.urlResponse.postText);
            if (log.isDebugEnabled()) {
                log.debug("http send：" + this.urlResponse.postText);
            }
        }
    }

}
