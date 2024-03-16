package wxdgaming.boot.starter.service;

import wxdgaming.boot.core.timer.TimerJobPool;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.i.IStart;

/**
 * ex
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:00
 **/
public class ScheduledService extends TimerJobPool implements IStart {

    @Override public void start(IocContext iocInjector) throws Exception {
        open();
    }

}
