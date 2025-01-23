package wxdgaming.boot.net.controller.cmd;


import com.alibaba.fastjson.JSONObject;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.controller.ann.TextMapping;

import java.util.Date;

/**
 * 重置时间
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:11
 **/
public interface ResetDate {

    /**
     * 重设系统运行当前时间
     */
    @TextMapping
    default void resetCurrentDate(StreamWriter out, JSONObject putData) throws Exception {
        String curtime = putData.getString("curtime");
        Date zdt = MyClock.parseDate(MyClock.SDF_YYYYMMDDHHMMSS_2, curtime);
        if (zdt.getTime() < System.currentTimeMillis() || zdt.getTime() < MyClock.millis()) {
            out.write("只能设置未来时间，不能设置过去时间, 如果要还原请重启程序！ 程序运行当前时间：" + MyClock.nowString());
            return;
        }
        MyClock.TimeOffset.set(zdt.getTime() - System.currentTimeMillis());
        out.write("程序运行当前时间：" + MyClock.nowString());
    }

}
