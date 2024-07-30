package wxdgaming.boot.agent.system;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class AesUtil {

    /**
     * ASE 对称加密
     * <p>
     * 先把字符串进行base64再进行加密
     *
     * @param str 需要加密的字符串
     * @param kk  对称加密顺序
     * @return
     */
    public static String convert_InBase64_ASE(String str, int... kk) {
        char[] strChars = Base64Util.encode(str).toCharArray();
        return convert_ASE_ToCharString(strChars, kk);
    }

    /**
     * ASE 对称加密
     * <p>
     * 先把字符串进行解密操作，再还原base64
     *
     * @param str 需要加密的字符串
     * @param kk  对称加密顺序
     * @return
     */
    public static String convert_UnBase64_ASE(String str, int... kk) {
        String convert_ASE = convert_ASE(str, kk);
        return Base64Util.decode(convert_ASE);
    }

    /**
     * ASE 对称加密
     *
     * @param str 需要加密的字符串
     * @param kk  对称加密顺序
     * @return
     */
    public static String convert_ASE(String str, int... kk) {
        char[] strChars = str.toCharArray();
        return convert_ASE_ToCharString(strChars, kk);
    }

    /**
     * ASE 对称加密
     *
     * @param strChars 需要加密的字符串
     * @param kk       对称加密顺序
     * @return
     */
    public static String convert_ASE_ToCharString(char[] strChars, int... kk) {
        try {
            return new String(convert_ASE_ToChar(strChars, kk));
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * ASE 对称加密
     *
     * @param strChars 需要加密的字符串
     * @param kk       对称加密顺序
     * @return
     */
    public static char[] convert_ASE_ToChar(char[] strChars, int... kk) {
        /*二分对称性*/
        int fcount = strChars.length / 2;
        /*对称性 k 值*/
        for (int k : kk) {
            for (int i = 0; i < fcount; i += k) {
                /*对称处理*/
                char tmp = strChars[i];
                strChars[i] = strChars[fcount + i];
                strChars[fcount + i] = tmp;
            }
        }
        return strChars;
    }

}
