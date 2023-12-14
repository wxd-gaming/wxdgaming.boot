package org.wxd.boot.agent.system;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class Base64Util {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder Base64_DECODER = Base64.getDecoder();

    /**
     * 编码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static String encode(String str) {
        return encode(str, StandardCharsets.UTF_8);
    }

    /**
     * @param str
     * @param charsetName
     * @return
     */
    public static String encode(String str, Charset charsetName) {
        return encode2String(str.getBytes(charsetName), charsetName);
    }

    /**
     * 编码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static byte[] encode2Bytes(String str) {
        return encode2Bytes(str, StandardCharsets.UTF_8);
    }

    /**
     * 编码64位
     *
     * @param str
     * @param charsetName
     * @return
     */
    public static byte[] encode2Bytes(String str, Charset charsetName) {
        return encodeBytes(str.getBytes(charsetName));
    }

    /**
     * 编码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static String encode2String(byte[] str) {
        return encode2String(str, StandardCharsets.UTF_8);
    }

    /**
     * @param str
     * @param charsetName
     * @return
     */
    public static String encode2String(byte[] str, Charset charsetName) {
        byte[] convertToBase64Byte = encodeBytes(str);
        return new String(convertToBase64Byte, charsetName);
    }

    /**
     * 编码64位
     *
     * @param str
     * @return
     */
    public static byte[] encodeBytes(byte[] str) {
        return BASE64_ENCODER.encode(str);
    }

    /**
     * 解码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static String decode(String str) {
        return decode(str, StandardCharsets.UTF_8);
    }

    /**
     * 解码64位
     *
     * @param str
     * @param charsetName
     * @return
     */
    public static String decode(String str, Charset charsetName) {
        if (str == null) {
            return null;
        }
        return new String(decode2Byte(str, charsetName), charsetName);
    }

    /**
     * 解码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static byte[] decode2Byte(String str) {
        return decode2Byte(str, StandardCharsets.UTF_8);
    }

    public static byte[] decode2Byte(String str, Charset charsetName) {
        return decode2Byte(str.getBytes(charsetName));
    }

    /**
     * 解码64位
     * <p>
     * 默认是utf-8
     *
     * @param str
     * @return
     */
    public static String decode(byte[] str) {
        return decode(str, StandardCharsets.UTF_8);
    }

    /**
     * @param str
     * @param charsetName
     * @return
     */
    public static String decode(byte[] str, Charset charsetName) {
        return new String(decode2Byte(str), charsetName);
    }

    /**
     * 解码64位
     *
     * @param str
     * @return
     */
    public static byte[] decode2Byte(byte[] str) {
        return Base64_DECODER.decode(str);
    }
}
