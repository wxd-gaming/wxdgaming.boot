package org.wxd.agent.loader;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * class byte 加载器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-29 09:36
 **/
@Slf4j
@Getter
public class ClassFileObjectLoader extends ClassLoader {

    private final Map<String, JavaFileObject4ClassStream> classFileObjectMap = new ConcurrentHashMap<>();

    public ClassFileObjectLoader(ClassLoader parent) {
        super(parent == null ? Thread.currentThread().getContextClassLoader() : parent);
    }

    public Collection<ClassInfo> allClass() {
        TreeMap<String, ClassInfo> classMap = new TreeMap<>();
        for (String className : this.classFileObjectMap.keySet()) {
            try {
                Class<?> aClass = loadClass(className);
                final JavaFileObject4ClassStream bytes = classFileObjectMap.get(className);
                classMap.put(className, new ClassInfo().setLoadClass(aClass).setLoadClassBytes(bytes.getCompiledBytes()));
            } catch (Throwable e) {
                log.warn("加载 class bytes " + className);
                e.printStackTrace(System.out);
            }
        }
        return classMap.values();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        final JavaFileObject4ClassStream javaFileObject4ClassStream = classFileObjectMap.get(name);
        if (javaFileObject4ClassStream != null) {
            final byte[] compiledBytes = javaFileObject4ClassStream.getCompiledBytes();
            return super.defineClass(null, compiledBytes, 0, compiledBytes.length);
        }
        return super.findClass(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (name.endsWith(JavaFileObject.Kind.CLASS.extension)) {
            String qualifiedClassName = name.substring(0, name.length() - JavaFileObject.Kind.CLASS.extension.length()).replace('/', '.');
            final JavaFileObject4ClassStream javaFileObject4ClassStream = classFileObjectMap.get(qualifiedClassName);
            if (null != javaFileObject4ClassStream) {
                return new ByteArrayInputStream(javaFileObject4ClassStream.getCompiledBytes());
            }
        }
        return super.getResourceAsStream(name);
    }

    /**
     * 暂时存放编译的源文件对象,key为全类名的别名（非URI模式）,如club.throwable.compile.HelloService
     */
    public void addJavaFileObject(String qualifiedClassName, JavaFileObject4ClassStream javaFileObject) {
        classFileObjectMap.put(qualifiedClassName, javaFileObject);
    }

    public Collection<JavaFileObject> listJavaFileObject() {
        return Collections.unmodifiableCollection(classFileObjectMap.values());
    }

    public Map<String, byte[]> toBytesMap() {
        Map<String, byte[]> tmpClassBytesMap = new TreeMap<>();
        for (Map.Entry<String, JavaFileObject4ClassStream> objectEntry : classFileObjectMap.entrySet()) {
            tmpClassBytesMap.put(objectEntry.getKey(), objectEntry.getValue().getCompiledBytes());
        }
        return tmpClassBytesMap;
    }

}
