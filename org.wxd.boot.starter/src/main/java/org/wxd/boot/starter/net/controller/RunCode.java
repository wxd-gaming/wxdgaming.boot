package org.wxd.boot.starter.net.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.loader.ClassBytesLoader;
import org.wxd.boot.agent.system.Base64Util;
import org.wxd.boot.agent.zip.ZipUtil;
import org.wxd.boot.append.StreamWriter;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.lang.RunResult;
import org.wxd.boot.net.controller.ann.TextMapping;
import org.wxd.boot.starter.Starter;
import org.wxd.boot.str.json.FastJsonUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 执行源码
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-21 19:08
 **/
public interface RunCode {

    final Logger log = LoggerFactory.getLogger(RunCode.class);

    /**
     * 运行动态代码
     */
    @TextMapping(remarks = "动态执行代码")
    default void runCode(StreamWriter out, ObjMap putData) throws Exception {
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
                    log.info("run code " + aClass.getName() + "， params " + params);
                    PostCodeRun newInstance = (PostCodeRun) aClass.getDeclaredConstructor().newInstance();
                    newInstance.setIocInjector(Starter.curIocInjector());
                    RunResult result = newInstance.run(params);
                    out.write(result.toJson());
                }
            }
        }
    }

}
