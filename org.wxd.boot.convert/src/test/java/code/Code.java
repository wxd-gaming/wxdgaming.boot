package code;

import org.junit.Test;
import org.wxd.boot.agent.system.AesUtil;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-02-29 20:22
 **/
public class Code {

    /** 动态激活码生成 */
    @Test
    public void t1() {
        String encrypt = encrypt(33);
        System.out.println("cdk code = " + encrypt);
        int decrypt = decrypt(encrypt);
        System.out.println("cdk id = " + decrypt);
    }

    public String encrypt(int cdkId) {
        String string = cdkId + "-" + System.nanoTime();
        System.out.println("cdk 加密串：" + string);
        return AesUtil.convert_InBase64_ASE(string, 3, 5);
    }

    public int decrypt(String cdk) {
        String string = AesUtil.convert_UnBase64_ASE(cdk, 5, 3);
        System.out.println("cdk 解密串：" + string);
        String s = string.split("-")[0];
        return Integer.parseInt(s);
    }

}
