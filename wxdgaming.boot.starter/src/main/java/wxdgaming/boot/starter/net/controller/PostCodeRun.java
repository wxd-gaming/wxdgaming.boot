package wxdgaming.boot.starter.net.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.starter.IocContext;

/**
 * 远程执行
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-18 17:06
 **/
@Slf4j
public abstract class PostCodeRun extends PostCode {

    protected String runCodeUrl = "gm/runCode";
    @Getter @Setter protected IocContext iocInjector;

    public void postMainCode() {
        postMainCode(null);
    }

    public void postMainCode(Object params) {
        String codePath = codeMainPath(this.getClass().getName());
        postCodeRun(codePath, params);
    }

    public void postTestCode() throws Exception {
        postTestCode(null);
    }

    /**
     * 执行的是test文件夹下面的代码
     *
     * @param params
     */
    public final RunResult postTestCode(Object params) {
        String codePath = codeTestPath(this.getClass().getName());
        return postCodeRun(codePath, params);
    }

    public final RunResult postCodeRun(String codePath, Object params) {
        return postCode(runCodeUrl, codePath, params);
    }

    public abstract RunResult run(String putData) throws Exception;

}
