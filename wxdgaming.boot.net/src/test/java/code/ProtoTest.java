package code;

import io.protostuff.Tag;
import org.junit.Test;
import wxdgaming.boot.net.message.Rpc;

import java.util.Arrays;

/**
 * protobuf篡改
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-27 13:15
 **/
public class ProtoTest {

    @Test
    public void t0() {
        Rpc.ReqRemote.Builder builder = Rpc.ReqRemote.newBuilder();
        builder.setRpcId(1);
        builder.setGzip(1);
        builder.setCmd("ss");
        builder.setParams("1");
        builder.setRpcToken("1");
        byte[] byteArray = builder.build().toByteArray();
        Tq tq = new Tq();
        byte[] encode = SerializerUtil.encode(tq);
        System.out.println(Arrays.toString(byteArray));
        System.out.println(Arrays.toString(encode));
        System.out.println("1");
    }

    public static class Tq {
        @Tag(1)
        private long rpcId = 1;
        @Tag(2)
        private int gzip = 1;
        @Tag(3)
        private String cmd = "ss";
        @Tag(4)
        private String params = "1";
        @Tag(5)
        private String rpcToken = "1";

    }

}
