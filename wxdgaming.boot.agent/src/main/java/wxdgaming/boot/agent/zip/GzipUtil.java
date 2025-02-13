package wxdgaming.boot.agent.zip;

import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.agent.io.Objects;
import wxdgaming.boot.agent.system.Base64Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class GzipUtil {

    /**
     * 压缩后转化成base64
     *
     * @param primStr
     * @return
     */
    public static String gzip2Base64(String primStr) {
        byte[] bytes = primStr.getBytes(StandardCharsets.UTF_8);
        byte[] gzip = gzip(bytes);
        return Base64Util.encode2String(gzip);
    }

    /**
     * 使用gzip进行压缩
     *
     * @param primStr
     */
    public static String gzip2String(String primStr) {
        return gzip2String(primStr.getBytes(StandardCharsets.UTF_8));
    }

    public static String gzip2String(byte[] prim) {
        byte[] gzip = gzip(prim);
        return new String(gzip, StandardCharsets.ISO_8859_1);
    }

    public static byte[] gzip2Bytes(String primStr) {
        return gzip(primStr.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * @param prim
     * @return
     */
    public static byte[] gzip(byte[] prim) {
        if (prim == null || prim.length == 0) {
            return prim;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(prim);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw Throw.of(e);
        }
    }

    public static void gzip2File(String filePath, String primStr) {
        byte[] gzip2Bytes = gzip2Bytes(primStr);
        FileWriteUtil.writeBytes(filePath, gzip2Bytes);
    }

    public static void gzipObject2File(String filePath, Object object) {
        byte[] bytes = Objects.toBytes(object);
        byte[] gzip2Bytes = gzip(bytes);
        FileWriteUtil.writeBytes(filePath, gzip2Bytes);
    }

    /**
     * 解压base64
     *
     * @param base64
     * @return
     */
    public static String unGzipBase64(String base64) {
        byte[] bytes = Base64Util.decode2Byte(base64);
        byte[] unGZip = unGZip(bytes);
        return new String(unGZip, StandardCharsets.UTF_8);
    }

    /**
     * 从字符串解压
     *
     * @param primStr
     * @return
     */
    public static String unGzip2String(String primStr) {
        try {
            final byte[] bytes = primStr.getBytes(StandardCharsets.ISO_8859_1);
            return unGzip2String(bytes);
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    /**
     * 还原普通字符串
     *
     * @param uncompress
     * @return
     */
    public static String unGzip2String(byte[] uncompress) {
        byte[] bytes = unGZip(uncompress);
        return new String(bytes, StandardCharsets.UTF_8);
    }


    /**
     * @param uncompress
     * @return
     */
    public static byte[] unGZip(byte[] uncompress) {
        if (uncompress == null) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ByteArrayInputStream input = new ByteArrayInputStream(uncompress)) {
                try (GZIPInputStream gzip = new GZIPInputStream(input)) {
                    byte[] buffer = new byte[256];
                    int offset = -1;
                    while ((offset = gzip.read(buffer)) != -1) {
                        out.write(buffer, 0, offset);
                    }
                }
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw Throw.of(e);
        }
    }

    public static String unGzip4File(String filePath) throws Exception {
        byte[] bytes = FileReadUtil.readBytes(filePath);
        byte[] bytes1 = unGZip(bytes);
        return new String(bytes1, StandardCharsets.UTF_8);
    }

    public static <R> R unGzipObject4File(String filePath) throws Exception {
        byte[] bytes = FileReadUtil.readBytes(filePath);
        byte[] bytes1 = unGZip(bytes);
        return Objects.toObject(bytes1);
    }

    public static void main(String[] args) {
        try {
            String str = "sdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtfsdfsfsdfAgaeswgaseGds俺二哥我搜嘎上的狗sgwegtf";
            System.out.println("原字符串：" + str);
            System.out.println("原长度：" + str.length());

            String zip = ZipUtil.zip(str);
            System.out.println("zip：" + zip.length() + " -- " + zip);
            String compress = gzip2String(str);
            System.out.println("gzip：" + compress.length() + " -- " + compress);

            String gzip2Base64 = gzip2Base64(str);
            System.out.println("gzip base64：" + gzip2Base64.length() + " -- " + gzip2Base64);
            System.out.println("解压缩后字符串：" + unGzipBase64(gzip2Base64));

//            gzip2File("d:/com.test.txt.gz", str);
//            String string = unGzip2String(compress);
//            System.out.println("解压缩后字符串：" + string);
//            System.out.println("解压缩后字符串：" + unGzip4File("d:/com.test.txt.gz"));
//            System.out.println("解压缩后字符串：" + str);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.exit(0);
    }

}
