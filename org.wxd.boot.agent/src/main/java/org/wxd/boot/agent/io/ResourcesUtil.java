package org.wxd.boot.agent.io;

import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.function.ConsumerE2;
import org.wxd.boot.agent.zip.ReadZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * 资源读取
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-14 16:22
 **/
public class ResourcesUtil {

    public static void resourceStream(String path, ConsumerE2<String, InputStream> call) {
        resourceStream(Thread.currentThread().getContextClassLoader(), path, call);
    }

    public static void resourceStream(ClassLoader classLoader, final String path, ConsumerE2<String, InputStream> call) {
        try {
            URL resource = classLoader.getResource(path);
            String findPath = resource.getPath();
            if (findPath.contains(".zip!") || findPath.contains(".jar!")) {
                findPath = findPath.substring(5, findPath.indexOf("!/"));
                try (ReadZipFile zipFile = new ReadZipFile(findPath)) {
                    zipFile.forEachStream((name, inputStream) -> {
                        if (name.startsWith(path)) {
                            call.accept(name, inputStream);
                        }
                    });
                }
            } else {
                File file = new File(findPath);
                if (file.exists() && file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File file1 : files) {
                        if (!file1.isDirectory()) {
                            try (FileInputStream fileInputStream = new FileInputStream(file1)) {
                                call.accept(file1.getName(), fileInputStream);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw Throw.as("resources:" + path, e);
        }
    }

    public static void main(String[] args) {
        resourceStream(".", (name, inputStream) -> {
            System.out.println(name);
        });

        resourceStream("META-INF/LICENSE", (name, inputStream) -> {
            System.out.println(name);
        });

        resourceStream("META-INF/LICENSE", (name, inputStream) -> {
            System.out.println(name);
        });
    }

}
