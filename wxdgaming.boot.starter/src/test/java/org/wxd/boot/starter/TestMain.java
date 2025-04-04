package org.wxd.boot.starter;


import wxdgaming.boot.starter.AppContext;
import wxdgaming.boot.starter.BootConfig;

/**
 * 启动项目
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-11 17:52
 **/
public class TestMain {

    public static void main(String[] args) {
        /*BootConfig bootConfig = new BootConfig();
        bootConfig.getHttp().getHeaders().add(new WebConfig.Header().setKey("1").setValue("2"));
        System.out.println(XmlUtil.toXml(bootConfig));*/
        AppContext.boot(TestMain.class/*其实这里需要包含的包名*/);
        AppContext.start(true, 1, "test");

        BootConfig instance = AppContext.context().getInstance(BootConfig.class);
        System.out.println(instance);
    }

}
