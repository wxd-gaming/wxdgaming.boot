package wxdgaming.boot.net.web.hs;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.nio.charset.StandardCharsets;

/**
 * 回复
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-25 16:29
 **/
@Getter
@Setter
@Accessors(chain = true)
public class HttpResult extends ObjectBase {

    HttpVersion hv = HttpVersion.HTTP_1_1;
    HttpResponseStatus hrs = HttpResponseStatus.OK;
    HttpHeadValueType contentType = HttpHeadValueType.Text;

    Object data;

    protected HttpResult() {
    }

    public void response(HttpSession session) {
        if (data instanceof byte[] bytes) {
            session.response(hv, hrs, contentType, bytes);
        } else if (data instanceof String text) {
            session.response(hv, hrs, contentType, text.getBytes(StandardCharsets.UTF_8));
        } else {
            session.response(hv, hrs, contentType, FastJsonUtil.toJsonKeyAsString(data).getBytes(StandardCharsets.UTF_8));
        }
    }

}
