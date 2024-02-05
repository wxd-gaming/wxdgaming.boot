package org.wxd.boot.core.format.data;


import org.wxd.boot.core.str.xml.XmlUtil;

/**
 * 序列化处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
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
