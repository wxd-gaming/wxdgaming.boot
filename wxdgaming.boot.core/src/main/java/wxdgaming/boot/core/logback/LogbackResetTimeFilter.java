package wxdgaming.boot.core.logback;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import wxdgaming.boot.core.timer.MyClock;

import java.io.Serializable;

/**
 * 为了同步修改日志记录的时间
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-08-31 21:11
 **/
public class LogbackResetTimeFilter extends Filter<LoggingEvent> implements Serializable {

    /**
     * <!-- 过滤重设时间，一般是当调试服务器或者调试活动的时候手动改时间需要触发 -->
     * <filter class="wxdgaming.boot.core.logback.LogbackResetTimeFilter">
     * <onMatch>ACCEPT</onMatch>
     * <onMismatch>DENY</onMismatch>
     * </filter>
     */
    @Override public FilterReply decide(LoggingEvent event) {
        event.setTimeStamp(MyClock.millis());
        return FilterReply.NEUTRAL;
    }

}
