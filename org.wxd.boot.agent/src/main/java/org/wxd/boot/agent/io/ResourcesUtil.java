package org.wxd.boot.agent.io;

/**
 * 资源读取
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-14 16:22
 **/
public class ResourcesUtil {


    public static void main(String[] args) {
        FileUtil.resourceStream(".", (name, inputStream) -> {
            System.out.println(name);
        });

        FileUtil.resourceStream("META-INF/LICENSE", (name, inputStream) -> {
            System.out.println(name);
        });

        FileUtil.resourceStream("META-INF/LICENSE", (name, inputStream) -> {
            System.out.println(name);
        });
    }

}
