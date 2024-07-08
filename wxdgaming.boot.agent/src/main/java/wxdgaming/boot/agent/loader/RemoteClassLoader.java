package wxdgaming.boot.agent.loader;

import lombok.Getter;
import wxdgaming.boot.agent.system.ReflectContext;

import javax.tools.JavaFileObject;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 远程loader
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-07-05 14:08
 **/
@Getter
public class RemoteClassLoader extends URLClassLoader {

    public static void main(String[] args) {
        RemoteClassLoader remoteClassLoader = RemoteClassLoader.build(
                RemoteClassLoader.class.getClassLoader(),
                "http://localhost/qj5/a.jar",
                "http://localhost/qj5/b.jar"
        );

        ReflectContext.Builder
                .of(remoteClassLoader, "gvm").build()
                .classStream()
                .forEach(c -> System.out.println("读取资源：" + c));

    }

    public static RemoteClassLoader build(ClassLoader parent, String... urls) {
        try {
            URL[] _ruls = new URL[urls.length];
            HashMap<String, byte[]> resources = new HashMap<>();
            HashMap<String, byte[]> classResources = new HashMap<>();
            for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
                String url = urls[i];
                URI uri = URI.create(url);
                _ruls[i] = uri.toURL();

                try (InputStream in = uri.toURL().openStream(); ZipInputStream zipInputStream = new ZipInputStream(in)) {
                    ZipEntry nextEntry = null;
                    while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                        /* todo 读取的资源字节可以做解密操作 */
                        byte[] extra = zipInputStream.readNBytes((int) nextEntry.getSize());
                        resources.put(nextEntry.getName(), extra);
                        // System.out.println("resource：" + nextEntry.getName());
                        if (!nextEntry.isDirectory() && nextEntry.getName().endsWith(JavaFileObject.Kind.CLASS.extension)) {
                            String replace = nextEntry.getName()
                                    .replace("\\", "/")
                                    .replace("/", ".");
                            replace = replace.substring(0, replace.length() - JavaFileObject.Kind.CLASS.extension.length());
                            classResources.put(replace, extra);
                            System.out.println("class：" + replace + " - " + extra.length);
                        }
                    }
                    int available1 = in.available();
                    System.out.println("剩余可以用：" + available1);
                }
            }

            return new RemoteClassLoader(
                    _ruls,
                    parent,
                    Collections.unmodifiableMap(resources),
                    Collections.unmodifiableMap(classResources)
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 读取资源文件的字节流 */
    private final Map<String, byte[]> classResources;
    /** 读取资源文件的字节流 */
    private final Map<String, byte[]> resources;

    public RemoteClassLoader(URL[] urls, ClassLoader parent, Map<String, byte[]> resources, Map<String, byte[]> classResources) {
        super(urls, parent);
        this.resources = resources;
        this.classResources = classResources;
    }

    public Stream<Class<?>> classStream() {
        return classStream(null);
    }

    public Stream<Class<?>> classStream(Predicate<String> test) {
        Stream<String> stream = classResources.keySet().stream();
        if (test != null) {
            stream = stream.filter(test);
        }
        return stream.map(v -> {
            try {
                return this.loadClass(v);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(v, e);
            }
        });
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("loadClass：" + name);
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ignore) {
            return findClass(name);
        }
    }

    @Override public Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("findClass：" + name);
        if (classResources.containsKey(name)) {
            byte[] bytes = classResources.get(name);
            System.out.println(name + " - " + bytes.length);
            Class<?> defineClass = super.defineClass(null, bytes, 0, bytes.length);
            return defineClass;
        }
        return super.findClass(name);
    }

    public List<Class<?>> classes() {
        return classes(null);
    }

    public List<Class<?>> classes(Predicate<String> test) {
        return classStream(test).toList();
    }

}
