package org.wxd.boot.agent.loader;


import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.JDKVersion;

import javax.tools.JavaFileObject;
import java.io.File;
import java.util.Map;

/**
 * class byte 加载器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-29 09:36
 **/
@Slf4j
public class ClassBytesLoader extends ClassDirLoader {

    public ClassBytesLoader(Map<String, byte[]> classFileMap) {
        this(classFileMap, null);
    }

    public ClassBytesLoader(Map<String, byte[]> classFileMap, ClassLoader parent) {
        super(parent);
        for (Map.Entry<String, byte[]> stringEntry : classFileMap.entrySet()) {
            this.classFileMap.put(qualifiedClassName(stringEntry.getKey()), stringEntry.getValue());
        }
        if (log.isDebugEnabled()) {
            JDKVersion jdkVersion = JDKVersion.runTimeJDKVersion();
            log.debug("load class file jdk_version：" + jdkVersion.getCurVersionString());
        }
    }

    public String qualifiedClassName(String name) {
        if (name.endsWith(JavaFileObject.Kind.CLASS.extension)) {
            name = name
                    .substring(0, name.length() - JavaFileObject.Kind.CLASS.extension.length());
        }
        name = name.replace(File.separatorChar, '.').replace('/', '.');
        return name;
    }

}
