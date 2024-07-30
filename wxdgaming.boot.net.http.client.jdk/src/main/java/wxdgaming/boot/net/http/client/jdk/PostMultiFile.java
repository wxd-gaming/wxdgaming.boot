package wxdgaming.boot.net.http.client.jdk;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.str.StringUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Map;

/**
 * 基于 java 原生的http协议支持
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-10-13 12:22
 **/
@Slf4j
@Setter
@Accessors(chain = true)
public class PostMultiFile extends PostMulti {

    PostMultiFile(HttpClient httpClient, String url) {
        super(httpClient, url);
    }

    public PostMultiFile putParams(Object key, Object value) {
        super.putParams(key, value);
        return this;
    }

    public PostMultiFile putParams(Map map) {
        super.putParams(map);
        return this;
    }

    @Override protected HttpRequest.Builder builder() {
        HttpRequest.Builder builder = super.builder();
        // HttpRequest.BodyPublishers.concat()
        // HttpRequest.BodyPublishers.ofFile(Paths.get(""))
        response.postText = ofFormData();
        if (StringUtil.notEmptyOrNull(response.postText)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(response.postText));
        } else {
            builder.POST(HttpRequest.BodyPublishers.noBody());
        }
        return builder;
    }

    String ofFormData() {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
        }
        return builder.toString();
    }

}
