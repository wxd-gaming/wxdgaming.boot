package wxdgaming.boot.starter.net.controller;

import lombok.extern.slf4j.Slf4j;

/**
 * 远程执行
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-18 17:06
 **/
@Slf4j
public abstract class PostCodeHot extends PostCode {


    protected String hotCodeUrl = "gm/hotLoadClass";

    public void postCodeHot(String codePath) {
        postCode(hotCodeUrl, codePath, null);
    }

}
