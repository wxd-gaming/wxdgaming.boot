package wxdgaming.boot.agent.loader;


import lombok.Getter;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class byte 加载器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-29 09:36
 **/
@Getter
public class ClassFileObjectLoader extends ClassDirLoader {

    private final Map<String, JavaFileObject4ClassStream> classFileObjectMap = new ConcurrentHashMap<>();

    public ClassFileObjectLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClassFileObjectLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * 暂时存放编译的源文件对象,key为全类名的别名（非URI模式）,如club.throwable.compile.HelloService
     */
    public void addJavaFileObject(String qualifiedClassName, JavaFileObject4ClassStream javaFileObject) throws MalformedURLException {
        classFileObjectMap.put(qualifiedClassName, javaFileObject);
    }

    @Override public void loadAll() {
        getClassFileMap();
        super.loadAll();
    }

    @Override public Map<String, byte[]> getClassFileMap() {
        if (classFileMap.size() != classFileObjectMap.size()) {
            classFileMap.clear();
            for (Map.Entry<String, JavaFileObject4ClassStream> objectEntry : classFileObjectMap.entrySet()) {
                this.classFileMap.put(objectEntry.getKey(), objectEntry.getValue().getCompiledBytes());
            }
        }
        return super.getClassFileMap();
    }
}
