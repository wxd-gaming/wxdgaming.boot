package wxdgaming.boot.core.str;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class Md5Util {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 空白字符
     */
    public static final String Null = "";

    /**
     * 转换字节数组为16进制字串
     *
     * @param b 字节数组
     * @return 16进制字串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte value : b) {
            resultSb.append(byteToHexString(value));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 会把参数拼接起来，
     * <p>
     * 采用md5的 Digest 插值算法
     *
     * @param origin
     * @return
     */
    public static String md5DigestEncode(String... origin) {
        return Md5Util.md5DigestEncode0(Md5Util.Null, origin);
    }

    /**
     * 会把参数用 joinStr 间隔字符 拼接起来
     * <p>
     * 采用md5的 Digest 插值算法
     *
     * @param joinStr 链接字符串 空白字符为 '\b'
     * @param origins 需要验证的字符组合
     * @return
     */
    public static String md5DigestEncode0(String joinStr, String... origins) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (joinStr == null) {
                joinStr = Md5Util.Null;
            }
            String join = String.join(joinStr, origins);
            resultString = byteArrayToHexString(md.digest(join.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new UnsupportedOperationException(ex);
        }
        return resultString;
    }

    /**
     * 验证MD5值,忽律大小写
     * <p>
     * 采用md5的 Digest 插值算法
     *
     * @param token   token
     * @param joinStr 链接字符 空白字符为 '\b'
     * @param origins 验证组合字符串
     * @return
     */
    public static boolean verifyDigestToken(String token, String joinStr, String... origins) {
        String md5Encode = Md5Util.md5DigestEncode0(joinStr, origins);
        return md5Encode.equalsIgnoreCase(token);
    }

    public static void main(String[] args) throws Exception {

        MessageDigest md = MessageDigest.getInstance("MD5");

        String key = "B83EEDB48F1B4379AB3171D43847339F";
        String token = "F6E6DDD725C8387A26382BBDFF2F0114";

        byte[] digest = md.digest((key + token).getBytes());
        System.out.println(byteArrayToHexString(digest));
        MessageDigest md1 = MessageDigest.getInstance("MD5");
        md1.update((key + token).getBytes());
        byte[] digest1 = md1.digest();
        System.out.println(byteArrayToHexString(digest1));

    }

}
