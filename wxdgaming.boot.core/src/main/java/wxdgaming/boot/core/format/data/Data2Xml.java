package wxdgaming.boot.core.format.data;


import wxdgaming.boot.core.str.xml.XmlUtil;

/**
 * 序列化处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-06-16 16:55
 **/
public interface Data2Xml extends DataSerialize {

    /**
     * xml string
     */
    default String toXml() throws Exception {
        return XmlUtil.toXml(this);
    }

}
