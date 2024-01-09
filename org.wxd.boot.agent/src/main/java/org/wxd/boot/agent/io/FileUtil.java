package org.wxd.boot.agent.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.function.ConsumerE2;
import org.wxd.boot.agent.lang.Tuple2;
import org.wxd.boot.agent.zip.ReadZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 文件辅助
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-18 14:40
 **/
public class FileUtil implements Serializable {

    private static Logger log() {
        return LoggerFactory.getLogger(FileUtil.class);
    }

    private static final String[] empty = new String[0];

    /** 返回绝对路径 */
    public static String getCanonicalPath(String fileName) {
        return getCanonicalPath(file(fileName));
    }

    /** 返回绝对路径 */
    public static String getCanonicalPath(File fileName) {
        try {
            return fileName.getCanonicalPath();
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /** 根据传入的类，获取类的jar包路径 */
    public static String clazzJarPath(Class clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    /**
     * 优先读取 config 目录下面
     * <p> 如果没有，从resources 文件夹下面读取
     */
    public static File findFile(String fileName) {
        return findFile(fileName, Thread.currentThread().getContextClassLoader());
    }

    public static File findFile(String fileName, ClassLoader classLoader) {

        fileName = fileName.replace("\\", "/");

        File file = new File(fileName);
        if (exists(file)) {
            return file;
        }

        if (!fileName.startsWith("config")) {
            file = new File("config/" + fileName);
            if (exists(file)) {
                return file;
            }
        }

        {
            /*todo 获取resource 不能是/开始的*/
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }
            URL resource = classLoader.getResource(fileName);
            if (resource != null) {
                return new File(resource.getFile());
            }

            if (!fileName.startsWith("config")) {
                resource = classLoader.getResource("config/" + fileName);
                if (resource != null) {
                    return new File(resource.getFile());
                }
            }

        }
        return null;
    }

    public static InputStream findInputStream(String fileName) {
        return findInputStream(fileName, Thread.currentThread().getContextClassLoader());
    }

    public static InputStream findInputStream(String fileName, ClassLoader classLoader) {
        try {
            fileName = fileName.replace("\\", "/");

            File file = new File(fileName);
            if (exists(file) && file.isFile()) {
                return new FileInputStream(file);
            }

            if (!fileName.startsWith("config")) {
                file = new File("config/" + fileName);
                if (exists(file) && file.isFile()) {
                    return new FileInputStream(file);
                }
            }

            {
                if (fileName.startsWith("/")) {
                    fileName = fileName.substring(1);
                }
                /*todo 获取resource 不能是/开始的*/
                InputStream resource = classLoader.getResourceAsStream(fileName);
                if (resource != null) {
                    return resource;
                }

                if (!fileName.startsWith("config")) {
                    resource = classLoader.getResourceAsStream("config/" + fileName);
                    if (resource != null) {
                        return resource;
                    }
                }

            }
            return null;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /** InputStream 需要自己关闭 */
    public static void resource(String path, ConsumerE2<String, InputStream> call) {
        resource(Thread.currentThread().getContextClassLoader(), path, call);
    }

    /** InputStream 需要自己关闭 */
    public static void resource(ClassLoader classLoader, final String path, ConsumerE2<String, InputStream> call) {
        resourceStreams(classLoader, path).forEach(entry -> {
            try {
                call.accept(entry.getLeft(), entry.getRight());
            } catch (Exception e) {
                throw Throw.as("resources:" + path, e);
            }
        });
    }

    /** 获取所有资源 */
    public static Stream<Tuple2<String, InputStream>> resourceStreams(final String path) {
        return resourceStreams(Thread.currentThread().getContextClassLoader(), path);
    }

    /** 获取所有资源 */
    public static Stream<Tuple2<String, InputStream>> resourceStreams(ClassLoader classLoader, final String path) {
        try {
            URL resource = classLoader.getResource(path);

            String findPath = path;
            if (findPath.startsWith("/")) {
                findPath = findPath.substring(1);
            }

            if (resource != null) {
                findPath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                if (findPath.contains(".zip!") || findPath.contains(".jar!")) {
                    findPath = findPath.substring(5, findPath.indexOf("!/"));
                    try (ReadZipFile zipFile = new ReadZipFile(findPath)) {
                        return zipFile.stream()
                                .filter(z -> !z.isDirectory())
                                .filter(p -> p.getName().startsWith(path))
                                .map(z -> new Tuple2<>(z.getName(), zipFile.unzipFileStream(z)));
                    }
                }
            }
            return walkFiles(findPath).map(file -> {
                try {
                    return new Tuple2<>(file.getPath(), new FileInputStream(file));
                } catch (Exception e) {
                    throw Throw.as("resources:" + file, e);
                }
            });
        } catch (Exception e) {
            throw Throw.as("resources:" + path, e);
        }
    }

    public static File file(String fileName) {
        return new File(fileName);
    }

    public static File file(URI uri) {
        return new File(uri);
    }

    /** 获取文件名 */
    public static String fileName(File file) {
        String fileName = file.getName();
        int indexOf = fileName.indexOf(".");
        return fileName.substring(0, indexOf);
    }

    /** 获取扩展名 */
    public static String extendName(File file) {
        return extendName(file.getName());
    }

    /** 获取扩展名 */
    public static String extendName(String fileName) {
        int indexOf = fileName.indexOf("?");
        if (indexOf >= 0) {
            fileName = fileName.substring(0, indexOf);
        }
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
        return fileName;
    }

    public static boolean exists(String fileName) {
        return file(fileName).exists();
    }

    public static boolean exists(File file) {
        return file.exists();
    }

    /** 下载网络资源 */
    public static void downloadFile(String url, String saveFileName) {
        try {
            long millis = System.currentTimeMillis();
            URL url1 = new URL(url);
            try (InputStream inputStream = url1.openStream()) {
                File file = new File(saveFileName);
                FileWriteUtil.fileOutputStream(
                        file,
                        false,
                        outputStream -> FileReadUtil.readBytes(outputStream, inputStream)
                );
                long costTime = System.currentTimeMillis() - millis;
                log().info("下载文件：{}, 保存：{}, 大小：{} kb, 耗时：{} ms", url, saveFileName, file.length() / 1024f, costTime);
            }
        } catch (Exception e) {
            throw Throw.as(url, e);
        }
    }

    public static void ftl2File(ClassLoader classLoader, String dir, String ftl, Map<String, Object> obj, String outFile) {
        TemplatePack.build(classLoader, dir).ftl2File(ftl, obj, outFile);
    }

    public static String ftl2String(ClassLoader classLoader, String dir, String ftl, Map<String, Object> obj) {
        return TemplatePack.build(classLoader, dir).ftl2String(ftl, obj);
    }

    public static byte[] ftl2Bytes(ClassLoader classLoader, String dir, String ftl, Map<String, Object> obj) {
        return TemplatePack.build(classLoader, dir).ftl2Bytes(ftl, obj);
    }

    public static File createFile(String fileName) {
        return createFile(file(fileName));
    }

    public static File createFile(File file) {
        return createFile(file, false);
    }

    /**
     * 创建文件
     *
     * @param file  路径
     * @param fugai 覆盖文件
     * @return
     */
    public static File createFile(File file, boolean fugai) {
        try {
            if (!fugai) {
                if (file.exists()) {
                    /*如果文件已经存在，无需创建*/
                    return file;
                }
            }
            mkdirs(file);
            file.createNewFile();
            return file;
        } catch (Exception e) {
            throw Throw.as(file.getPath(), e);
        }
    }

    /**
     * 删除文件 或者 文件夹
     *
     * @param fileName
     */
    public static void del(String fileName) {
        del(file(fileName));
    }

    /**
     * 删除文件 或者 文件夹
     *
     * @param file
     */
    public static void del(File file) {
        walk(file).forEach(f -> {
            boolean delete = f.delete();
            log().info("删除{}：{}, {}", file.isFile() ? "文件" : "文件夹", file.getName(), delete);
        });
    }

    /**
     * 创建文件夹
     *
     * @param fileName 需要创建文件夹的名字
     */
    public static void mkdirs(String fileName) {
        mkdirs(file(fileName));
    }

    /**
     * 创建文件夹
     *
     * @param file 需要创建文件夹
     */
    public static void mkdirs(File file) {
        final File absoluteFile = file.getAbsoluteFile();
        if (absoluteFile.isDirectory()) {
            absoluteFile.mkdirs();
        } else {
            absoluteFile.getParentFile().mkdirs();
        }
    }

    /** 所有的文件夹 */
    public static Stream<File> walkDirs(String path, String... extendNames) {
        return walkDirs(path, Integer.MAX_VALUE, extendNames);
    }

    /** 所有的文件 */
    public static Stream<File> walkDirs(String path, int maxDepth, String... extendNames) {
        return walk(path, maxDepth, extendNames).filter(File::isDirectory);
    }

    /** 所有的文件 */
    public static Stream<File> walkFiles(String path, String... extendNames) {
        return walkFiles(path, Integer.MAX_VALUE, extendNames);
    }

    /** 所有的文件 */
    public static Stream<File> walkFiles(File path, String... extendNames) {
        return walkFiles(path, Integer.MAX_VALUE, extendNames);
    }

    /** 所有的文件 */
    public static Stream<File> walkFiles(String path, int maxDepth, String... extendNames) {
        return walk(path, maxDepth, extendNames).filter(File::isFile);
    }

    public static Stream<File> walkFiles(File path, int maxDepth, String... extendNames) {
        return walk(path, maxDepth, extendNames).filter(File::isFile);
    }

    /** 查找所有文件, 文件夹 */
    public static Stream<File> walk(String path, String... extendNames) {
        return walk(path, Integer.MAX_VALUE, extendNames);
    }

    /**
     * 查找文件, 文件夹
     *
     * @param path     路径
     * @param maxDepth 深度 当前目录是1
     * @return
     */
    public static Stream<File> walk(String path, int maxDepth, String... extendNames) {
        File file = new File(path);
        return walk(file, maxDepth, extendNames);
    }

    /** 查找所有文件, 文件夹 */
    public static Stream<File> walk(File path, String... extendNames) {
        return walk(path, Integer.MAX_VALUE, extendNames);
    }

    /**
     * 查找文件, 文件夹
     *
     * @param path     路径
     * @param maxDepth 深度 当前目录是1
     * @return
     */
    public static Stream<File> walk(File path, int maxDepth, String... extendNames) {
        return walk(path.toPath(), maxDepth, extendNames);
    }

    /**
     * 查找文件, 文件夹
     *
     * @param path        路径
     * @param maxDepth    深度 当前目录是1
     * @param extendNames 查找的扩展名
     * @return
     */
    public static Stream<File> walk(Path path, int maxDepth, String... extendNames) {
        try {
            Stream<File> walk = Files.walk(path, maxDepth).map(Path::toFile);
            if (extendNames.length > 0) {
                walk = walk.filter(f -> {
                    boolean check = false;
                    for (String extendName : extendNames) {
                        if (f.getName().endsWith(extendName)) {
                            check = true;
                            break;
                        }
                    }
                    return check;
                });
            }
            return walk;
        } catch (Exception e) {
            throw Throw.as(path.toString(), e);
        }
    }

}
