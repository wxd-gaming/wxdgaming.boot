package wxdgaming.boot.core.format.data;

import wxdgaming.boot.agent.io.Objects;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.str.xml.XmlUtil;

import java.util.Map;

/**
 * 序列化处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-06-16 16:55
 **/
public interface DataSerialize extends DataMapping {


    /**
     * xml 格式化
     */
    default String toXml() throws Exception {
        return XmlUtil.toXml(this);
    }

    /**
     * 返回当前对象的字节流
     */
    default byte[] toBytes() {
        return Objects.toBytes(this);
    }

    /**
     * 把数据设置到当前对象
     *
     * @param json 数据 应该来源于{@link Data2StringMap#toStringMap()}
     */
    default <R> R reflect(String json) {
        reflect(FastJsonUtil.parseStringMap(json));
        return (R) this;
    }

    /**
     * 把数据设置到当前对象
     *
     * @param ov 数据 应该来源于{@link Data2StringMap#toStringMap()}
     */
    default <R> R reflect(Map<String, String> ov) {
        if (ov == null || ov.isEmpty()) {
            return (R) this;
        }
        classMapping(null).fromMap(this, ov);
        return (R) this;
    }

}
