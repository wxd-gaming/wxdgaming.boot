package wxdgaming.boot.agent.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.loader.ClassDirLoader;
import wxdgaming.boot.agent.loader.RemoteClassLoader;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 资源处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-10-16 10:11
 **/
@Getter
public class ReflectContext {

    public static <R> R newInstance(Class<R> cls, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            Constructor<R> constructor = cls.getConstructor(parameterTypes);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    /** 判定 接口, 枚举, 注解, 抽象类 返回 false */
    public static boolean checked(Class<?> aClass) {
        /* 判定 是否可用 */
        return !(
                Object.class.equals(aClass)
                || aClass.isInterface()
                || aClass.isEnum()
                || aClass.isAnnotation()
                || Modifier.isAbstract(aClass.getModifiers())
        );
    }

    /** 获取类型实现的接口 */
    public static Stream<Class<?>> getInterfaces(Class<?> cls) {
        TreeMap<String, Class<?>> classCollection = new TreeMap<>();
        getInterfaces(classCollection, cls);
        return classCollection.values().stream();
    }

    /** 获取类实现的接口 */
    public static void getInterfaces(TreeMap<String, Class<?>> classCollection, Class<?> cls) {
        if (cls == null || Object.class.equals(cls)) {
            return;
        }
        /*查找父类*/
        getInterfaces(classCollection, cls.getSuperclass());
        Class<?>[] interfaces = cls.getInterfaces();
        for (Class<?> aInterface : interfaces) {
            /*查找接口，实现的接口*/
            getInterfaces(classCollection, aInterface);
            /*查找父类*/
            getInterfaces(classCollection, aInterface.getSuperclass());
            if (aInterface.isInterface()) {
                classCollection.put(aInterface.getName(), aInterface);
            }
        }
    }

    /** 获取泛型的第一个 */
    public static <T> Class<T> getTClass(Class<?> source) {
        return getTClass(source, 0);
    }

    /** 获取泛型的类型 */
    public static <T> Class<T> getTClass(Class<?> source, int index) {
        Type genType = source.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return (Class) params[index];
    }

    /** 所有的类 */
    private final List<Class<?>> classList;

    public ReflectContext(Collection<Class<?>> classList) {
        this.classList = new ArrayList<>(classList);
    }

    /** 所有的类 */
    public Stream<Class<?>> classStream() {
        return classList.stream();
    }

    /** 父类或者接口 */
    public <U> Stream<Class<U>> classWithSuper(Class<U> cls) {
        return classWithSuper(cls, null);
    }

    /** 父类或者接口 */
    public <U> Stream<Class<U>> classWithSuper(Class<U> cls, Predicate<Class<U>> predicate) {
        @SuppressWarnings("unchecked")
        Stream<Class<U>> tmp = classStream().filter(cls::isAssignableFrom).map(c -> (Class<U>) c);
        if (predicate != null) tmp = tmp.filter(predicate);
        return tmp;
    }

    /** 所有添加了这个注解的类 */
    public Stream<Class<?>> classWithAnnotated(Class<? extends Annotation> annotation) {
        return classWithAnnotated(annotation, null);
    }

    /** 所有添加了这个注解的类 */
    public Stream<Class<?>> classWithAnnotated(Class<? extends Annotation> annotation, Predicate<Class<?>> predicate) {
        Stream<Class<?>> tmp = classStream().filter(c -> AnnUtil.ann(c, annotation) != null);
        if (predicate != null) tmp = tmp.filter(predicate);
        return tmp;
    }

    public Stream<Content<?>> stream() {
        return classList.stream().map(Content::new);
    }

    /** 父类或者接口 */
    public <U> Stream<Content<U>> withSuper(Class<U> cls) {
        return withSuper(cls, null);
    }

    /** 父类或者接口 */
    public <U> Stream<Content<U>> withSuper(Class<U> cls, Predicate<Class<U>> predicate) {
        return classWithSuper(cls, predicate).map(Content::new);
    }

    /** 所有添加了这个注解的类 */
    public Stream<Content<?>> withAnnotated(Class<? extends Annotation> annotation) {
        return withAnnotated(annotation, null);
    }

    /** 所有添加了这个注解的类 */
    public Stream<Content<?>> withAnnotated(Class<? extends Annotation> annotation, Predicate<Class<?>> predicate) {
        return classWithAnnotated(annotation, predicate).map(Content::new);
    }

    /** 所有bean里面的方法，添加了注解的 */
    public Stream<Method> withMethodAnnotated(Class<? extends Annotation> annotation) {
        return withMethodAnnotated(annotation, null);
    }

    /** 所有添加了这个注解的类 */
    public Stream<Method> withMethodAnnotated(Class<? extends Annotation> annotation, Predicate<Method> predicate) {
        Stream<Method> methodStream = stream().flatMap(info -> info.methodsWithAnnotated(annotation));
        if (predicate != null) {
            methodStream = methodStream.filter(predicate);
        }
        return methodStream;
    }

    @Getter
    public static class Content<T> {

        private final Class<T> cls;

        public static <U> Content<U> of(Class<U> cls) {
            return new Content<>(cls);
        }

        Content(Class<T> cls) {
            this.cls = cls;
        }

        /** 是否添加了注解 */
        public boolean withAnnotated(Class<? extends Annotation> annotation) {
            return AnnUtil.ann(cls, annotation) != null;
        }

        /** 所有的方法 */
        public Collection<Method> getMethods() {
            return MethodUtil.readAllMethod(cls).values();
        }

        /** 所有的方法 */
        public Stream<Method> methodStream() {
            return getMethods().stream();
        }

        /** 所有添加了这个注解的方法 */
        public Stream<Method> methodsWithAnnotated(Class<? extends Annotation> annotation) {
            return methodStream().filter(m -> AnnUtil.ann(m, annotation) != null);
        }

        /** 所有的字段 */
        public Collection<Field> getFields() {
            return FieldUtil.getFields(false, cls).values();
        }

        /** 所有的字段 */
        public Stream<Field> fieldStream() {
            return getFields().stream();
        }

        /** 所有添加了这个注解的字段 */
        public Stream<Field> fieldWithAnnotated(Class<? extends Annotation> annotation) {
            return fieldStream().filter(f -> AnnUtil.ann(f, annotation) != null);
        }

    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {

        public static Builder of(String... packageNames) {
            return of(Thread.currentThread().getContextClassLoader(), packageNames);
        }

        public static Builder of(ClassLoader classLoader, String... packageNames) {
            return new Builder(classLoader, packageNames);
        }

        private final ClassLoader classLoader;
        private final String[] packageNames;
        /** 是否读取子包 */
        private boolean findChild = true;
        /** 查找类的时候忽略接口 */
        private boolean filterInterface = true;
        /** 过滤掉抽象类 */
        private boolean filterAbstract = true;
        /** 过滤掉枚举类 */
        private boolean filterEnum = true;
        /** graalvm 打包需要 resources.json */
        private ArrayList<String> resources = null;

        /** graalvm 打包需要 resources.json */
        public ArrayList<String> getResources() {
            if (resources == null) {
                InputStream resourceAsStream = classLoader.getResourceAsStream("resources.json");
                if (resourceAsStream != null) {
                    byte[] bytes = FileReadUtil.readBytes(resourceAsStream);
                    String string = new String(bytes, StandardCharsets.UTF_8);
                    resources = JSON.parseObject(string, new TypeReference<ArrayList<String>>() {});
                }
            }
            if (resources == null) {
                resources = new ArrayList<>();
            }
            return resources;
        }

        private Builder(ClassLoader classLoader, String[] packageNames) {
            this.classLoader = classLoader;
            this.packageNames = packageNames;
        }

        /** 所有的类 */
        public ReflectContext build() {
            TreeMap<String, Class<?>> classCollection = new TreeMap<>();
            for (String packageName : packageNames) {
                findClasses(packageName, aClass -> classCollection.put(aClass.getName(), aClass));
            }
            List<Class<?>> list = classCollection.values()
                    .stream()
                    .filter(v -> !Object.class.equals(v))
                    .filter(v -> !filterInterface || !v.isInterface())
                    .filter(v -> !filterAbstract || !Modifier.isAbstract(v.getModifiers()))
                    .filter(v -> !filterEnum || !v.isEnum())
                    .filter(v -> !v.isAnnotation())
                    .toList();
            return new ReflectContext(list);
        }

        private void findClasses(String packageName, Consumer<Class<?>> consumer) {
            String packagePath = packageName;
            if (packageName.endsWith(".jar") || packageName.endsWith(".war")) {
                packagePath = packageName;
            } else if (!".".equals(packageName)) {
                packagePath = packageName.replace(".", "/");
            }
            try {
                if (classLoader instanceof ClassDirLoader dirLoader) {
                    final Collection<Class<?>> classes = dirLoader.getLoadClassMap().values();
                    if (!classes.isEmpty()) {
                        for (Class<?> aClass : classes) {
                            if (aClass.getName().startsWith(packageName)) {
                                consumer.accept(aClass);
                            }
                        }
                    }
                }

                if (classLoader instanceof RemoteClassLoader remoteClassLoader) {
                    remoteClassLoader
                            .classStream(v -> v.startsWith(packageName))
                            .forEach(consumer);
                }

                Enumeration<URL> resources = classLoader.getResources(packagePath);
                if (resources != null) {
                    URL url = null;
                    while (resources.hasMoreElements()) {
                        url = resources.nextElement();
                        if (url != null) {
                            String type = url.getProtocol();
                            String urlPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
                            switch (type) {
                                case "file" -> {
                                    String dir = urlPath.substring(0, urlPath.lastIndexOf(packagePath));
                                    findClassByFile(dir, urlPath, consumer);
                                }
                                case "jar", "zip" -> findClassByJar(urlPath, consumer);
                                case "resource" -> {
                                    /* graalvm 打包需要 */
                                    getResources()
                                            .stream()
                                            .filter(v -> v.startsWith(packageName))
                                            .forEach(v -> {
                                                loadClass(v, consumer);
                                            });
                                }
                                case null, default -> System.out.println("未知类型：" + type + " - " + urlPath);
                            }
                        } else {
                            findClassByJars(
                                    ((URLClassLoader) classLoader).getURLs(),
                                    packagePath,
                                    consumer
                            );
                        }
                    }
                }
            } catch (Throwable e) {
                throw Throw.of(e);
            }
        }

        /**
         * 从项目文件获取某包下所有类
         *
         * @param dir      父级文件夹
         * @param filePath 文件路径
         */
        private void findClassByFile(String dir, String filePath, Consumer<Class<?>> consumer) {
            File file = new File(filePath);
            File[] childFiles = file.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        if (findChild) {
                            findClassByFile(dir, childFile.getPath(), consumer);
                        }
                    } else {
                        String childFilePath = childFile.getPath();
                        if (childFilePath.endsWith(".class")) {
                            childFilePath = childFilePath.substring(dir.length() - 1, childFilePath.lastIndexOf("."));
                            childFilePath = childFilePath.replace("\\", ".");

                            loadClass(childFilePath, consumer);

                        }
                    }
                }
            }
        }

        /**
         * 从所有jar中搜索该包，并获取该包下所有类
         *
         * @param urls        URL集合
         * @param packagePath 包路径
         */
        private void findClassByJars(URL[] urls, String packagePath, Consumer<Class<?>> consumer) {

            if (urls != null) {
                for (URL url : urls) {
                    String urlPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
                    // 不必搜索classes文件夹
                    if (urlPath.endsWith("classes/")) {
                        continue;
                    }
                    String jarPath = urlPath + "!/" + packagePath;
                    findClassByJar(jarPath, consumer);
                }
            }
        }

        /**
         * 从jar获取某包下所有类
         *
         * @param jarPath jar文件路径
         */
        private void findClassByJar(String jarPath, Consumer<Class<?>> consumer) {
            if (jarPath.startsWith("http://") || jarPath.startsWith("https://")) return;
            String[] jarInfo = jarPath.split("!");
            String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
            String packagePath = jarInfo[1].substring(1);
            String entryName;
            try (JarFile jarFile = new JarFile(jarFilePath)) {
                Enumeration<JarEntry> entrys = jarFile.entries();
                while (entrys.hasMoreElements()) {
                    JarEntry jarEntry = entrys.nextElement();
                    entryName = jarEntry.getName();
                    if (entryName.endsWith(".class")) {
                        if (findChild) {
                            if (!entryName.startsWith(packagePath)) {
                                continue;
                            }
                        } else {
                            int index = entryName.lastIndexOf("/");
                            String myPackagePath;
                            if (index != -1) {
                                myPackagePath = entryName.substring(0, index);
                            } else {
                                myPackagePath = entryName;
                            }
                            if (!myPackagePath.equals(packagePath)) {
                                continue;
                            }
                        }
                        entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                        loadClass(entryName, consumer);
                    }
                }
            } catch (Throwable e) {
                throw Throw.of(jarPath, e);
            }
        }

        private void loadClass(String childFilePath, Consumer<Class<?>> consumer) {
            try {
                Class<?> clazz = classLoader.loadClass(childFilePath);
                consumer.accept(clazz);
            } catch (Throwable e) {
                LoggerFactory.getLogger(ReflectContext.class).error(childFilePath, e);
            }
        }

    }

}
