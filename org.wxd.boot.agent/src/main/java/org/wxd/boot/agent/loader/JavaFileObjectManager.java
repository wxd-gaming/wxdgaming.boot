package org.wxd.boot.agent.loader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-10 11:05
 **/
@Slf4j
@Getter
public class JavaFileObjectManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final StandardJavaFileManager superFileManager;
    private final Map<String, JavaFileObject> javaFileObjectMap = new ConcurrentHashMap<>();
    private final ClassFileObjectLoader classFileObjectLoader;

    public JavaFileObjectManager(StandardJavaFileManager fileManager, ClassLoader parentClassLoader) {
        super(fileManager);
        this.superFileManager = fileManager;
        this.classFileObjectLoader = new ClassFileObjectLoader(parentClassLoader);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return classFileObjectLoader;
    }

    /**
     * 这里是编译器返回的同(源)Java文件对象,替换为CharSequenceJavaFileObject实现
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("compiler class name " + className);
        }
        final URI uri = URI.create(className.replaceAll("\\.", "/") + kind.extension);
        JavaFileObject4ClassStream byteClassFileObject = new JavaFileObject4ClassStream(uri, kind);
        classFileObjectLoader.addJavaFileObject(className, byteClassFileObject);
        return byteClassFileObject;
    }

}
