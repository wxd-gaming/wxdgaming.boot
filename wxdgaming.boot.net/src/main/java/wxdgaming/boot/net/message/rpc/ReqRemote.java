package wxdgaming.boot.net.message.rpc;

import io.protostuff.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.net.pojo.PojoBase;


/** 执行同步等待消息 */
@Getter
@Setter
@Accessors(chain = true)
public class ReqRemote extends PojoBase {

    /** 反序列化 */
    public static ReqRemote parseFrom(byte[] bytes) {
        ReqRemote s = new ReqRemote();
        s.decode(bytes);
        return s;
    }

    /**  */
    @Tag(1) private long rpcId;
    /** 1表示压缩过 */
    @Tag(2) private int gzip;
    /** 执行的命令 */
    @Tag(3) private String cmd;
    /** 用JsonObject来解析 */
    @Tag(4) private String params;
    /** 用于验证的消息 */
    @Tag(5) private String rpcToken;

}
