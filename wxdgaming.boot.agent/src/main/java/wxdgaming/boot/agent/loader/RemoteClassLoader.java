package wxdgaming.boot.agent.loader;

import lombok.Getter;

import javax.tools.JavaFileObject;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

    public static RemoteClassLoader build(ClassLoader parent, String... urls) {
        try {
            URL[] _ruls = new URL[urls.length];
            ArrayList<String> resources = new ArrayList<>();
            ArrayList<String> classResources = new ArrayList<>();
            for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
                String url = urls[i];
                URI uri = URI.create(url);
                _ruls[i] = uri.toURL();
                try (ZipInputStream zipInputStream = new ZipInputStream(uri.toURL().openStream())) {
                    ZipEntry nextEntry = null;
                    while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                        resources.add(nextEntry.getName());
                        // System.out.println("resource：" + nextEntry.getName());
                        if (!nextEntry.isDirectory() && nextEntry.getName().endsWith(JavaFileObject.Kind.CLASS.extension)) {
                            String replace = nextEntry.getName()
                                    .replace("\\", "/")
                                    .replace("/", ".");
                            replace = replace.substring(0, replace.length() - JavaFileObject.Kind.CLASS.extension.length());
                            classResources.add(replace);
                            // System.out.println("class：" + replace);
                        }
                    }
                }
            }

            return new RemoteClassLoader(
                    _ruls,
                    parent,
                    Collections.unmodifiableList(resources),
                    Collections.unmodifiableList(classResources)
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final List<String> classResources;
    private final List<String> resources;

    public RemoteClassLoader(URL[] urls, ClassLoader parent, List<String> resources, List<String> classResources) {
        super(urls, parent);
        this.resources = resources;
        this.classResources = classResources;
    }

    public Stream<Class<?>> classStream() {
        return classStream(null);
    }

    public Stream<Class<?>> classStream(Predicate<String> test) {
        Stream<String> stream = classResources.stream();
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

    public List<Class<?>> classes() {
        return classes(null);
    }

    public List<Class<?>> classes(Predicate<String> test) {
        return classStream(test).collect(Collectors.toUnmodifiableList());
    }

}
