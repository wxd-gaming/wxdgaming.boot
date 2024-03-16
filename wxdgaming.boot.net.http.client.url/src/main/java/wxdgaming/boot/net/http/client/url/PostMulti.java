package wxdgaming.boot.net.http.client.url;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.http.HttpDataAction;
import wxdgaming.boot.net.http.HttpHeadValueType;

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
public class PostMulti extends Post<PostMulti> {

    protected boolean urlEncoder = true;
    protected ObjMap reqMap = new ObjMap();

    protected PostMulti(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = "POST";
    }

    public PostMulti putParams(Object key, Object value) {
        reqMap.put(key, value);
        return this;
    }

    public PostMulti putParams(Map map) {
        reqMap.putAll(map);
        return this;
    }

    @Override protected void writeTextParams(StreamWriter outWriter) throws Exception {
        if (reqMap.isEmpty()) return;
        boolean isMultipart = this.contentType == HttpHeadValueType.Multipart || contentType == HttpHeadValueType.FormData;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Object, Object> stringEntry : reqMap.entrySet()) {
            String name = String.valueOf(stringEntry.getKey());
            String value = String.valueOf(stringEntry.getValue());
            if (urlEncoder) {
                value = HttpDataAction.urlEncoder(value);
            }
            if (isMultipart) {
                stringBuilder.append("--").append(boundary).append("\r\n");
                stringBuilder.append("Content-Disposition: form-data; name=\"" + name + "\", Content-Type: " + contentType.getValue() + ", Content-Transfer-Encoding: 8bit").append("\r\n");
                stringBuilder.append("\r\n").append(value).append("\r\n");
            } else {
                if (!stringBuilder.isEmpty()) stringBuilder.append("&");
                stringBuilder.append(name).append("=").append(value);
            }
        }
        outWriter.write(stringBuilder.toString());
    }

}
