package code;

import org.junit.Test;
import wxdgaming.boot.agent.system.AesUtil;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-02-29 20:22
 **/
public class GiftCode {

    /** 动态 激活码 礼包码 生成 */
    @Test
    public void t1() {
        String encrypt = encrypt(333666, 3, 5);
        System.out.println("cdk code = " + encrypt + ", len=" + encrypt.length());
        System.out.println("cdk id = " + decrypt(encrypt, 5, 3));
    }

    public String encrypt(Object cdkId, int... kk) {
        String string = cdkId + "-" + System.nanoTime();
        System.out.println("cdk 加密串：" + string);
        return AesUtil.convert_InBase64_ASE(string, kk);
    }

    public String decrypt(String cdk, int... kk) {
        String string = AesUtil.convert_UnBase64_ASE(cdk, kk);
        System.out.println("cdk 解密串：" + string);
        return string.split("-")[0];
    }

}
