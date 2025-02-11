package code;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import wxdgaming.boot.core.timer.CronExpress;
import wxdgaming.boot.core.timer.MyClock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-03-16 22:57
 **/
public class CronExpressTest {

    @Test
    public void t1() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cron", "0 0");
        jsonObject.put("timeUnit", TimeUnit.SECONDS);
        jsonObject.put("duration", 500);
        System.out.println(jsonObject);
        CronExpress parse = jsonObject.toJavaObject(CronExpress.class);

        long[] longs = parse.validateTimeAfter(MyClock.millis());
        System.out.println(Arrays.toString(longs));
        System.out.println(parse.validateDateAfter());

        System.out.println(parse.validateDateBefore());
    }

}
