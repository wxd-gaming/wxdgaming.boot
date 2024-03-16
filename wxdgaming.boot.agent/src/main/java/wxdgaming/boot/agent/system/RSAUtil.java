package wxdgaming.boot.agent.system;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class RSAUtil {

    /**
     * 从字符串加载公钥
     *
     * @param public_key
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKeyByStr(String public_key) throws Exception {
        try {
//            StringBuilder stringBuilder = new StringBuilder();
//            int count = 0;
//            for (int i = 0; i < public_key.length(); ++i) {
//                if (count < 64) {
//                    stringBuilder.append(public_key.charAt(i));
//                    count++;
//                } else {
//                    stringBuilder.append(public_key.charAt(i)).append("\r\n");
//                    count = 0;
//                }
//            }
//            public_key = stringBuilder.toString();
            byte[] buffer = Base64Util.decode2Byte(public_key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            System.out.println(publicKey);
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * @param public_key
     * @param base64String
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(String public_key, String base64String) throws Exception {
        return publicKeyDecryptToString(public_key, base64String, 128);
    }

    /**
     * @param publicKey
     * @param base64String
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(PublicKey publicKey, String base64String) throws Exception {
        return publicKeyDecryptToString(publicKey, base64String, 128);
    }

    /**
     * @param public_key
     * @param base64String
     * @param MAX_DECRYPT_BLOCK
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(String public_key, String base64String, int MAX_DECRYPT_BLOCK) throws Exception {
        PublicKey publicKey = loadPublicKeyByStr(public_key);
        return publicKeyDecryptToString(publicKey, base64String, MAX_DECRYPT_BLOCK);
    }

    public static String publicKeyDecryptToString(PublicKey publicKey, String base64String, int MAX_DECRYPT_BLOCK) throws Exception {
        byte[] convertFromBase64Byte = Base64Util.decode2Byte(base64String);
        return publicKeyDecryptToString(publicKey, convertFromBase64Byte, MAX_DECRYPT_BLOCK);
    }

    /**
     * @param public_key
     * @param cipherData
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(String public_key, byte[] cipherData) throws Exception {
        return publicKeyDecryptToString(public_key, cipherData, 128);
    }

    /**
     * rsa 解密
     *
     * @param public_key
     * @param cipherData
     * @param MAX_DECRYPT_BLOCK
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(String public_key, byte[] cipherData, int MAX_DECRYPT_BLOCK) throws Exception {
        PublicKey publicKey = loadPublicKeyByStr(public_key);
        return publicKeyDecryptToString(publicKey, cipherData, MAX_DECRYPT_BLOCK);
    }

    /**
     * rsa 解密
     *
     * @param publicKey
     * @param cipherData
     * @param MAX_DECRYPT_BLOCK
     * @return
     * @throws Exception
     */
    public static String publicKeyDecryptToString(PublicKey publicKey, byte[] cipherData, int MAX_DECRYPT_BLOCK) throws Exception {
        byte[] publicKeyDecrypt = publicKeyDecrypt(publicKey, cipherData, MAX_DECRYPT_BLOCK);
        return new String(publicKeyDecrypt);
    }

    /**
     * 公钥解密
     *
     * @param public_key
     * @param cipherData
     * @return
     * @throws Exception
     */
    public static byte[] publicKeyDecrypt(String public_key, byte[] cipherData) throws Exception {
        return publicKeyDecrypt(public_key, cipherData, 128);
    }

    /**
     * 公钥解密
     *
     * @param public_key
     * @param cipherData
     * @param MAX_DECRYPT_BLOCK 解密位宽
     * @return
     * @throws Exception
     */
    public static byte[] publicKeyDecrypt(String public_key, byte[] cipherData, int MAX_DECRYPT_BLOCK) throws Exception {
        PublicKey publicKey = loadPublicKeyByStr(public_key);
        return publicKeyDecrypt(publicKey, cipherData, MAX_DECRYPT_BLOCK);
    }

    public static byte[] publicKeyDecrypt(PublicKey publicKey, byte[] cipherData, int MAX_DECRYPT_BLOCK) throws Exception {
        if (publicKey == null) {
            throw new Exception("解密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            int inputLen = cipherData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(cipherData, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(cipherData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();
            return decryptedData;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * @param content
     * @param sign
     * @param public_key
     * @return
     * @throws Exception
     */
    public static boolean rsaVerifySign(String content, String sign, String public_key) throws Exception {
        PublicKey publicKey = loadPublicKeyByStr(public_key);
        return rsaVerifySign(content, sign, publicKey);
    }

    /**
     * @param content
     * @param sign
     * @param public_key
     * @return
     * @throws Exception
     */
    public static boolean rsaVerifySign(String content, String sign, PublicKey public_key) throws Exception {
        return rsaVerifySign(content, sign, public_key, StandardCharsets.UTF_8, "SHA1WithRSA");
    }

    /**
     * RSA验签名检查
     *
     * @param content         内容
     * @param sign            签名
     * @param public_key      公钥字符串
     * @param charsetName
     * @param SIGN_ALGORITHMS rsa 算法
     * @return
     * @throws Exception
     */
    public static boolean rsaVerifySign(String content, String sign, String public_key, Charset charsetName, String SIGN_ALGORITHMS) throws Exception {
        PublicKey publicKey = loadPublicKeyByStr(public_key);
        return rsaVerifySign(content, sign, publicKey, charsetName, SIGN_ALGORITHMS);
    }

    /**
     * @param content
     * @param sign
     * @param publicKey
     * @param charsetName
     * @param SIGN_ALGORITHMS
     * @return
     * @throws Exception
     */
    public static boolean rsaVerifySign(String content, String sign, PublicKey publicKey, Charset charsetName, String SIGN_ALGORITHMS) throws Exception {
        java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
        signature.initVerify(publicKey);
        signature.update(content.getBytes(charsetName));
        return signature.verify(Base64Util.decode2Byte(sign));
    }

}
