package wxdgaming.boot.agent.loader;


import jdk.internal.loader.BootLoader;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.JDKVersion;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指定 class 目录加载
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-06 14:40
 **/
@Getter
@Setter
public class ClassDirLoader extends URLClassLoader implements Serializable {

    /**
     * lib文件夹下面所有 jar 包 ClassLoader
     * <p>包附加到 SystemClassLoader 加载器里面
     *
     * @return
     */
    public static ClassDirLoader bootLib() {
        return bootLib(ClassLoader.getSystemClassLoader(), "lib");
    }

    /**
     * jar 包 ClassLoader
     *
     * @param parent  指定的类加载器
     * @param jarPath jar 路径，可以是jar包也可以是 jar 目录
     */
    public static ClassDirLoader bootLib(ClassLoader parent, String jarPath) {
        ClassDirLoader classDirLoader = new ClassDirLoader(parent);
        FileUtil.walkFiles(jarPath, ".jar")
                .forEach(lib -> {
                    try {
                        classDirLoader.addURL(lib.toURI().toURL());
                    } catch (Exception e) {
                        throw new RuntimeException("ClassLoader 附加 jar 包：" + jarPath, e);
                    }
                });
        return classDirLoader;
    }

    /**
     * 把 jar 包附加到 SystemClassLoader 加载器里面
     *
     * @param jarPath        jar包路径
     * @param checkClassName 需要验证的类名，全面，
     */
    public static Class<?> loadAClass(String jarPath, String checkClassName) {
        ClassLoader contextClassLoader = ClassLoader.getSystemClassLoader();
        return loadAClass(contextClassLoader, jarPath, checkClassName);
    }

    /**
     * 指定加载器
     * <p>请一定注意 this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()
     * <p> Thread.currentThread().getContextClassLoader() !=  ClassLoader.getSystemClassLoader();
     *
     * @param classLoader    指定的类加载器
     * @param jarPath        jar 路径，可以是jar包也可以是 jar 目录
     * @param checkClassName 需要加载的类
     * @return
     */
    public static Class<?> loadAClass(ClassLoader classLoader, String jarPath, String checkClassName) {
        try {
            final Class<?> aClass = classLoader.loadClass(checkClassName);
            System.out.println("原始加载器加载成功：" + checkClassName);
            return aClass;
        } catch (ClassNotFoundException knife) {
            ClassDirLoader jarFileLoader = bootLib(classLoader, jarPath);
            try {
                final Class<?> loadClass = jarFileLoader.loadClass(checkClassName);
                System.out.println("附加外部 jar 包，加载成功：" + checkClassName);
                return loadClass;
            } catch (Exception e) {
                throw new RuntimeException("加载 error", e);
            }
        }
    }

    /** 遇到异常继续 */
    protected boolean errorContinue = true;
    /** 已经加载的class类情况 */
    protected Map<String, Class<?>> classes_path_Map = null;
    /** 待加载的class byte 集合 */
    protected final Map<String, byte[]> classFileMap = new TreeMap<>();
    /** 已经加载的class byte 集合 */
    protected final Map<String, Class<?>> loadClassMap = new TreeMap<>();
    protected final Map<String, ClassInfo> loadClassInfoMap = new TreeMap<>();

    public ClassDirLoader() {
        this(Thread.currentThread().getContextClassLoader(), new URL[0]);
    }

    /** 存放class的目录 */
    public ClassDirLoader(String classDir) throws Exception {
        this(classDir, Thread.currentThread().getContextClassLoader());
    }

    /** 存放class的目录 */
    public ClassDirLoader(String classDir, ClassLoader parent) throws Exception {
        this(new File(classDir).toURI().toURL(), parent);
    }

    /** 存放class的目录 */
    public ClassDirLoader(URL url) {
        this(url, Thread.currentThread().getContextClassLoader());
    }

    /**
     * @param url    存放class文件的顶层目录
     * @param parent 父类加载器
     */
    public ClassDirLoader(URL url, ClassLoader parent) {
        super(new URL[]{url}, parent);
        action(url);
    }

    public ClassDirLoader(ClassLoader parent) {
        this(parent, new URL[0]);
    }

    public ClassDirLoader(ClassLoader parent, URL... urls) {
        super(urls, parent);
        for (URL url : urls) {
            action(url);
        }
        JDKVersion jdkVersion = JDKVersion.runTimeJDKVersion();
        System.out.println("class loader jdk_version：" + jdkVersion.getCurVersionString());
    }

    public ClassDirLoader(ClassLoader parent, String... urls) {
        super(new URL[0], parent);
        for (String url : urls) {
            addURL(url);
        }
        JDKVersion jdkVersion = JDKVersion.runTimeJDKVersion();
        System.out.println("class loader jdk_version：" + jdkVersion.getCurVersionString());
    }

    /** 可以添加资源文件夹 */
    public void addURL(String... urls) {
        try {
            for (String url : urls) {
                this.addURL(new File(url).toURI().toURL());
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /** 可以添加资源文件夹 */
    @Override public void addURL(URL url) {
        super.addURL(url);
        action(url);
    }

    protected void action(URL url) {
        try {
            File file = new File(url.toURI());
            int pathLen = file.getPath().length() + 1;
            FileReadUtil.readBytesAll(file, ".class", ".CLASS").forEach((className, bytes) -> {
                className = className.substring(pathLen, className.length() - 6);
                // 将/替换成. 得到全路径类名
                className = className.replace(File.separatorChar, '.').replace('/', '.');
                classFileMap.put(className, bytes);
            });
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> defineClass = null;
        try {
            defineClass = super.loadClass(name);
        } catch (ClassNotFoundException classNotFoundException) {
            if (classes_path_Map != null) {
                defineClass = classes_path_Map.get(name);
            }
            if (defineClass == null) {
                throw classNotFoundException;
            }
        }
        return defineClass;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> aClass = loadClassMap.get(name);
        if (aClass != null) {
            return aClass;
        }
        byte[] bytes = classFileMap.get(name);
        Class<?> defineClass;
        if (bytes != null) {
            defineClass = super.defineClass(null, bytes, 0, bytes.length);
            loadClassMap.put(name, defineClass);
            loadClassInfoMap.put(name, new ClassInfo().setLoadClass(defineClass).setLoadClassBytes(bytes));
        } else {
            defineClass = super.findClass(name);
        }
        return defineClass;
    }

    /** 所有的类名 */
    public Collection<String> allClassNames() {
        return classFileMap.keySet();
    }

    public void loadAll() {
        for (String className : allClassNames()) {
            try {
                this.findClass(className);
            } catch (Throwable e) {
                if (errorContinue) {
                    LoggerFactory.getLogger(ClassDirLoader.class).error("load class bytes error " + className, e);
                } else {
                    throw Throw.as("load class bytes error " + className, e);
                }
            }
        }
    }

    public Map<String, Class<?>> getLoadClassMap() {
        if (loadClassMap.isEmpty()) {
            loadAll();
        }
        return loadClassMap;
    }

    public Map<String, ClassInfo> getLoadClassInfoMap() {
        if (loadClassInfoMap.isEmpty()) {
            loadAll();
        }
        return loadClassInfoMap;
    }

    @Override public InputStream getResourceAsStream(String name) {
        if (loadClassInfoMap.containsKey(name)) {
            try {
                return new ByteArrayInputStream(loadClassInfoMap.get(name).getLoadClassBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.getResourceAsStream(name);
    }

    @Override public URL findResource(String name) {
        if (loadClassInfoMap.containsKey(name)) {
            try {
                return URL.of(
                        new URI(name.replace(".", "/")),
                        new ResourceURLStreamHandler(new ByteArrayInputStream(loadClassInfoMap.get(name).getLoadClassBytes()))
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.findResource(name);
    }

    @Override public Enumeration<URL> findResources(String name) throws IOException {
        List<URL> list = loadClassInfoMap
                .values()
                .stream()
                .filter(v -> v.getLoadClassClassName().startsWith(name))
                .map(v -> {
                    try {
                        String fileName = v.getLoadClassClassName().replace('.', '/') + ".class";
                        return Paths.get(fileName).toUri().toURL();
                        //return URL.of(Paths.get(fileName).toUri(), new ResourceURLStreamHandler(new ByteArrayInputStream(v.getLoadClassBytes())));
                        //return URL.of(
                        //        new URI(v.getLoadClassClassName().replace(".", "/")),
                        //        new ResourceURLStreamHandler(new ByteArrayInputStream(v.getLoadClassBytes()))
                        //);
                    } catch (Exception e) {
                        throw new RuntimeException(v.getLoadClassClassName(), e);
                    }
                })
                .toList();

        if (!list.isEmpty()) {
            Iterator<URL> collect = list.iterator();
            return new Enumeration<URL>() {
                @Override public boolean hasMoreElements() {
                    return collect.hasNext();
                }

                @Override public URL nextElement() {
                    return collect.next();
                }
            };
        }
        return super.findResources(name);
    }

    @Override protected URL findResource(String moduleName, String name) throws IOException {
        return super.findResource(moduleName, name);
    }

    @Override public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }


    protected static final class ResourceURLStreamHandler extends URLStreamHandler {

        private final InputStream inputStream;

        public ResourceURLStreamHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        protected URLConnection openConnection(URL u) {
            return new URLConnection(u) {
                @Override
                public void connect() {
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return inputStream;
                }
            };
        }
    }

}
