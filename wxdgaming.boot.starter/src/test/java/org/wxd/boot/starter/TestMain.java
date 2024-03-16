package org.wxd.boot.starter;


import wxdgaming.boot.starter.Starter;

/**
 * 启动项目
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 17:52
 **/
public class TestMain {

    public static void main(String[] args) {
        /*BootConfig bootConfig = new BootConfig();
        bootConfig.getHttp().getHeaders().add(new WebConfig.Header().setKey("1").setValue("2"));
        System.out.println(XmlUtil.toXml(bootConfig));*/
        Starter.startBoot(TestMain.class/*其实这里需要包含的包名*/);
        Starter.start(true, 1, "test");
    }

}
