package wxdgaming.boot.starter.net.controller;

import wxdgaming.boot.agent.LogbackUtil;
import wxdgaming.boot.agent.loader.ClassBytesLoader;
import wxdgaming.boot.agent.system.Base64Util;
import wxdgaming.boot.agent.zip.ZipUtil;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.web.hs.HttpSession;
import wxdgaming.boot.starter.Starter;

import java.util.Collection;
import java.util.Map;

/**
 * 执行源码
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-21 19:08
 **/
public interface RunCode {

    /**
     * 运行动态代码
     */
    @TextMapping(remarks = "动态执行代码", needAuth = 9/*必须验证权限*/)
    default void runCode(HttpSession httpSession, ObjMap putData) throws Exception {
        String codebase64 = putData.getString("codebase64");
        byte[] decode = Base64Util.decode2Byte(codebase64);
        String javaClasses = ZipUtil.unzip2String(decode);

        putData.remove("codebase64");

        Map<String, byte[]> stringHashMap = FastJsonUtil.parseMap(javaClasses, String.class, byte[].class);

        try (ClassBytesLoader classBytesLoader = new ClassBytesLoader(stringHashMap, this.getClass().getClassLoader())) {
            Collection<Class<?>> classes = classBytesLoader.getLoadClassMap().values();
            for (Class<?> aClass : classes) {
                if (PostCodeRun.class.isAssignableFrom(aClass)) {
                    String params = putData.getString("params");
                    LogbackUtil.logger().info("run code " + aClass.getName() + "， params " + params);
                    PostCodeRun newInstance = (PostCodeRun) aClass.getDeclaredConstructor().newInstance();
                    newInstance.setIocInjector(Starter.curIocInjector());
                    RunResult result = newInstance.run(params);
                    httpSession.responseText(result.toJson());
                    return;
                }
            }
        }
    }

}
