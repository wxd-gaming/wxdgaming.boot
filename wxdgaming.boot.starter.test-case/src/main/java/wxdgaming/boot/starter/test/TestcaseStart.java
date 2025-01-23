package wxdgaming.boot.starter.test;

import wxdgaming.boot.starter.AppContext;

/**
 * 测试案例启动类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 19:41
 **/
public class TestcaseStart {

    public static void main(String[] args) {
        AppContext.boot(TestcaseStart.class);
        AppContext.start(true, 1, "testcase", "testcase");
    }

}
