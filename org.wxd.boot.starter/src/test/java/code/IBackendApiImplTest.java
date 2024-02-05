package code;

import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.wxd.boot.core.str.Md5Util;
import org.wxd.boot.core.str.StringUtil;
import org.wxd.boot.core.str.json.FastJsonUtil;
import org.wxd.boot.net.http.HttpHeadValueType;
import org.wxd.boot.net.http.client.url.HttpBuilder;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class IBackendApiImplTest {

    @Test
    public void testAsyncBackRegister() {
        TreeMap<String, String> params = new TreeMap<>();
        String game = "TLCQ";//同登陆接口参数game(大写)
        params.put("uid", String.valueOf(System.currentTimeMillis()));
        params.put("pwd", "pwd");
        params.put("mot", "15388152619");
        params.put("email", "15388152619@mail.com");
        params.put("nc", "nc");
        params.put("game", game);
        params.put("platform", "TLCQ");
        params.put("server", String.valueOf(1));
        params.put("rolename", "1");
        params.put("time", String.valueOf(1706857362));
        String collect = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        {
            String key = "ED8fdafd89x54a8eadf4a";
            String md5Str = collect + "&" + key;
            String sign = Md5Util.md5DigestEncode(md5Str).toUpperCase();
            params.put("sign", sign);
            log.info("加密源串：{} - {}", md5Str, sign);
        }
        String result = HttpBuilder
                .postMulti("http://test99.yj99.cn/api/game/register/index.php")
                .contentType(HttpHeadValueType.Multipart)
                .putParams(params)
                .setUrlEncoder(false)
                .request()
                .bodyUnicodeDecodeString();

        log.info("{} {}：{}", "dd", "接口测试", FastJsonUtil.toJson(result));
        if (StringUtil.notEmptyOrNull(result)) {
            HashMap<String, String> resMap = FastJsonUtil.parse(result, new TypeReference<HashMap<String, String>>() {});
        }
    }

}