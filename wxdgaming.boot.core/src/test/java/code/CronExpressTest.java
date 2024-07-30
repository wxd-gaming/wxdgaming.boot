package code;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.timer.CronExpress;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-16 22:57
 **/
public class CronExpressTest {

    @Test
    public void t1() {
        LinkedHashMap objMap = new LinkedHashMap();
        objMap.put("cron", "0 0");
        objMap.put("timeUnit", TimeUnit.SECONDS);
        objMap.put("duration", 500);
        String json = JSON.toJSONString(objMap);
        System.out.println(json);
        CronExpress parse = FastJsonUtil.parse(json, CronExpress.class);
        System.out.println(parse.validateDateAfter());

        System.out.println(parse.validateDateBefore());
    }

}
