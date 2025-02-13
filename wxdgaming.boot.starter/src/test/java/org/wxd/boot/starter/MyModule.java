package org.wxd.boot.starter;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.UserModule;

/**
 * 自定义模块
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-13 18:53
 **/
public class MyModule extends UserModule {

    public MyModule(ReflectContext reflectContext) {
        super(reflectContext);
    }

    @Override protected void bind() throws Exception {

    }


}
