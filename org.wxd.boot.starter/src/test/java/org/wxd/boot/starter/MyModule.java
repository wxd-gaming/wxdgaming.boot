package org.wxd.boot.starter;

import org.wxd.agent.system.ReflectContext;

/**
 * 自定义模块
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-13 18:53
 **/
public class MyModule extends UserModule {

    public MyModule(ReflectContext reflectContext) {
        super(reflectContext);
    }

    @Override protected MyModule bind() throws Exception {

        return this;
    }

}
