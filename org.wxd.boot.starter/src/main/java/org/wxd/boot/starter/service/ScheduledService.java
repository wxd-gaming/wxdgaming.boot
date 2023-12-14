package org.wxd.boot.starter.service;

import org.wxd.boot.starter.IocContext;
import org.wxd.boot.starter.i.IStart;
import org.wxd.boot.timer.TimerJobPool;

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
