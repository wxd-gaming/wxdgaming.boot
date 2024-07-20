package code;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Test;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.logbus.ILog;
import wxdgaming.boot.logbus.LogService;

import java.io.IOException;

/**
 * 日志入库测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-07-20 16:05
 **/
public class LogMain {

    @Test
    public void l1() throws IOException {

        LogService logService = new LogService();
        logService.getMysqlDataHelper().createTable(LoginLog.class);

        System.out.println("准备好了可以敲入回车测试");
        System.in.read();
        for (int i = 0; i < 10000; i++) {

            logService.push(new LoginLog()
                    .setUid(System.nanoTime())
                    .setAccount("test")
                    .setTime(MyClock.millis())
                    .setIp("127.0.0.2")
            );
        }

        System.in.read();

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class LoginLog extends ObjectBase implements ILog {

        @DbColumn(key = true)
        private long uid;
        private String account;
        @DbColumn(index = true)
        private long time;
        private String ip;

    }

}
