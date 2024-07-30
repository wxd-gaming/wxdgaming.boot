package wxdgaming.boot.agent.loader;


import javax.tools.JavaFileObject;
import java.io.File;
import java.util.Map;

/**
 * class byte 加载器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-29 09:36
 **/
public class ClassBytesLoader extends ClassDirLoader {

    public ClassBytesLoader(Map<String, byte[]> classFileMap) {
        this(classFileMap, Thread.currentThread().getContextClassLoader());
    }

    public ClassBytesLoader(Map<String, byte[]> classFileMap, ClassLoader parent) {
        super(parent);
        for (Map.Entry<String, byte[]> stringEntry : classFileMap.entrySet()) {
            this.classFileMap.put(qualifiedClassName(stringEntry.getKey()), stringEntry.getValue());
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
