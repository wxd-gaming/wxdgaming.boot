package wxdgaming.boot.core.system;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-13 09:20
 **/
@Slf4j
public class MarkTimer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static MarkTimer build() {
        return new MarkTimer();
    }


    private long markTime;

    private MarkTimer() {
        markTime = System.nanoTime();
    }

    /**
     * 返回毫秒,保留两位小数的毫秒
     *
     * @return
     */
    public float execTime() {
        /*为了保留两位小数*/
        return ((System.nanoTime() - markTime) / 10000) / 100f;
    }

    /**
     * 执行耗时： xx.xxx ms
     *
     * @return
     */
    public String execTime2String() {
        return "耗时：" + execTime() + " ms";
    }


    public MarkTimer print(String str) {
        print(500, str);
        return this;
    }

    public MarkTimer print(int warn, String str) {
        final float endTime = execTime();
        if (endTime > warn) {
            log.warn(str + " 耗时：" + endTime + " ms");
        } else if (endTime > 100) {
            log.info(str + " 耗时：" + endTime + " ms");
        } else if (log.isDebugEnabled()) {
            log.debug(str + " 耗时：" + endTime + " ms");
        }
        return this;
    }

    /**
     * 打印日志
     *
     * @param log  日志组件
     * @param warn 输出级别
     * @param str  日志
     * @return
     */
    public MarkTimer print(Logger log, int warn, String str) {
        final float endTime = execTime();
        if (endTime > warn) {
            log.warn(str + " 耗时：" + endTime + " ms");
        } else if (endTime > 100) {
            log.info(str + " 耗时：" + endTime + " ms");
        } else if (log.isDebugEnabled()) {
            log.debug(str + " 耗时：" + endTime + " ms");
        }
        return this;
    }

    public void clear() {
        markTime = System.nanoTime();
    }

}
