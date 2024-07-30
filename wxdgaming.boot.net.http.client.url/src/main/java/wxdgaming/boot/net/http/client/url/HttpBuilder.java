package wxdgaming.boot.net.http.client.url;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-05 12:55
 **/
@Slf4j
public final class HttpBuilder {

    public static Get get(String url) {
        return new Get(url);
    }

    public static PostText postText(String url) {
        return new PostText(url);
    }

    public static PostText postText(String url, String params) {
        return new PostText(url).paramText(params);
    }

    public static PostText postJson(String url, String json) {
        return new PostText(url).paramJson(json);
    }

    public static PostMulti postMulti(String url) {
        return new PostMulti(url);
    }

    public static PostMulti postMulti(String url, Map map) {
        return new PostMulti(url).putParams(map);
    }

    public static PostMultiFile postFile(String url, Map map) {
        return new PostMultiFile(url).putParams(map);
    }

    public static PostMultiFile postFile(String url, File file) {
        return new PostMultiFile(url).addFile(file);
    }

}
