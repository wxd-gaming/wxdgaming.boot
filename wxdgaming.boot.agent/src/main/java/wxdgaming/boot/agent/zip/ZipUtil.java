package wxdgaming.boot.agent.zip;


import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.Objects;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class ZipUtil {
    /**
     * 使用zip进行压缩
     *
     * @param object 压缩前
     * @return 返回压缩后
     */
    public static byte[] zipObject(Object object) {
        if (object == null) {
            return null;
        }
        return zip(Objects.toBytes(object));
    }

    /**
     * 使用zip进行压缩
     *
     * @param str 压缩前的文本
     * @return 返回压缩后的文本
     */
    public static String zip(String str) {
        if (str == null) {
            return null;
        }
        final byte[] zip = zip(str.getBytes(StandardCharsets.UTF_8));
        return new String(zip, StandardCharsets.ISO_8859_1);
    }

    /**
     * 使用zip进行压缩
     *
     * @param str 压缩前的文本
     * @return 返回压缩后的文本
     */
    public static byte[] zip2Bytes(String str) {
        if (str == null) {
            return null;
        }
        return zip(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用zip进行压缩
     *
     * @param bytes 压缩前的文本
     * @return 返回压缩后的文本
     */
    public static String zip2String(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    /**
     * 使用zip进行压缩
     *
     * @param bytes 压缩前
     * @return 返回压缩后
     */
    public static byte[] zip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ZipOutputStream zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("0"));
                zout.write(bytes);
                zout.closeEntry();
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 使用zip进行解压缩
     *
     * @param zipStr 压缩后文本
     * @return 解压后的字符串
     */
    public static String unzip(String zipStr) {
        if (zipStr == null) {
            return null;
        }
        byte[] bytes = unzip(zipStr.getBytes(StandardCharsets.ISO_8859_1));
        return unzip2String(bytes);
    }

    /**
     * 使用zip进行解压缩
     *
     * @param bytes 压缩后
     * @return
     */
    public static <R> R unzipObject(byte[] bytes) {
        byte[] unzip = unzip(bytes);
        return Objects.toObject(unzip);
    }

    public static String unzip2String(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        bytes = unzip(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用zip进行解压缩
     *
     * @param bytes 压缩后
     * @return
     */
    public static byte[] unzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                try (ZipInputStream zin = new ZipInputStream(in)) {
                    zin.getNextEntry();
                    byte[] buffer = new byte[1024];
                    int offset = -1;
                    while ((offset = zin.read(buffer)) != -1) {
                        out.write(buffer, 0, offset);
                    }
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 覆盖文件
     *
     * @param zipPath  zip文件名和路径，
     * @param fileName 存入zip文件的文件名 建议文件名是 .db 或者 .tmp
     * @param source   默认会是utf-8编码
     */
    public static void zipObject2File(String zipPath, String fileName, Object source) {
        byte[] bytes = Objects.toBytes(source);
        zip2File(zipPath, fileName, bytes);
    }

    /**
     * 覆盖文件
     *
     * @param zipPath  zip文件名和路径，
     * @param fileName 存入zip文件的文件名
     * @param source   默认会是utf-8编码
     */
    public static void zip2File(String zipPath, String fileName, String source) {
        zip2File(zipPath, fileName, source, StandardCharsets.UTF_8);
    }

    /**
     * 覆盖文件
     *
     * @param zipPath     zip文件名和路径，
     * @param fileName    存入zip文件的文件名
     * @param source      写入文件内容
     * @param charsetName 编码字符集
     */
    public static void zip2File(String zipPath, String fileName, String source, Charset charsetName) {
        zip2File(zipPath, fileName, source.getBytes(charsetName));
    }

    /**
     * 覆盖文件
     *
     * @param zipPath  zip文件名和路径，
     * @param fileName 存入zip文件的文件名
     * @param bytes
     */
    public static void zip2File(String zipPath, String fileName, byte[] bytes) {
        File file = FileUtil.createFile(zipPath);
        try (FileOutputStream fos = new FileOutputStream(zipPath, false)) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                try (BufferedOutputStream out = new BufferedOutputStream(zos)) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    out.write(bytes);
                    out.flush();
                }
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 默认 字符集 utf-8 读取内容
     *
     * @param zipPath  zip文件路径和名字
     * @param filename zip文件中的文件名字
     * @return
     */
    public static String unzipStringFile(String zipPath, String filename) {
        return unzipStringFile(zipPath, filename, StandardCharsets.UTF_8);
    }

    /**
     * @param zipPath     zip文件路径和名字
     * @param filename    zip文件中的文件名字
     * @param charsetName 字符集 utf-8
     * @return
     */
    public static String unzipStringFile(String zipPath, String filename, Charset charsetName) {
        byte[] bytes = unzipFile(zipPath, filename);
        return new String(bytes, charsetName);
    }

    /**
     * @param zipPath  zip文件路径和名字
     * @param fileName zip文件中的文件名字
     * @return
     */
    public static <T> T unzipObjectFile(String zipPath, String fileName) {
        byte[] bytes = unzipFile(zipPath, fileName);
        return Objects.toObject(bytes);
    }

    /** 读取文件内容 */
    public static byte[] unzipFile(String zipPath, String fileName) {
        try (ReadZipFile readZipFile = new ReadZipFile(zipPath)) {
            return readZipFile.find(fileName);
        }
    }


    public static void main(String[] args) throws IOException {
        String str = "sdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtf";
        String zip = zip(str);
        System.out.println(zip.length() + ", " + zip);
        final String unzip = unzip(zip);
        System.out.println(unzip.length() + ", " + unzip);
        zip2File("d:/com.test.zip", "com.test.txt", "gegtegeg");
        System.exit(0);
    }
}
