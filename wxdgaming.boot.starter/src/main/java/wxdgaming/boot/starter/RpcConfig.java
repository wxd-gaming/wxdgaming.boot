package wxdgaming.boot.starter;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Element;
import wxdgaming.boot.core.lang.ObjectBase;

import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-09-30 09:33
 **/
@Getter
@Setter
@Accessors(chain = true)
public class RpcConfig extends ObjectBase implements Serializable {

    @Element(required = false)
    @JSONField(ordinal = 1)
    private String name = "";
    /** 外网ip */
    @Element(required = false)
    @JSONField(ordinal = 2)
    private String wanIp = "0.0.0.0";
    /** 内网ip */
    @Element(required = false)
    @JSONField(ordinal = 3)
    private String host = "";
    /** 监听端口 */
    @Element(required = false)
    @JSONField(ordinal = 4)
    private int port = 0;
    /** 设置默认的链接数量 */
    @Element(required = false)
    @JSONField(ordinal = 5)
    private int sessionSize = 1;
    /** 链接超时设置 */
    @Element(required = false)
    @JSONField(ordinal = 6)
    private int connectTimeout = 2000;
    /** 使用的类 */
    @Element(required = false)
    @JSONField(ordinal = 81)
    private String clientClassName = null;

}
