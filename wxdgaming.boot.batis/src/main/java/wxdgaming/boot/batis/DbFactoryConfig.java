package wxdgaming.boot.batis;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.xml.XmlUtil;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-03-24 11:06
 **/
@Getter
@Setter
@Accessors(chain = true)
@Root
public class DbFactoryConfig extends ObjectBase implements Serializable {

    public static DbFactoryConfig build(String fileName) throws Exception {
        Record2<String, InputStream> inputStream = FileUtil.findInputStream(fileName);
        if (inputStream == null) {
            throw new RuntimeException("文件不存在 " + fileName);
        }
        return build(inputStream.t2());
    }

    public static DbFactoryConfig build(InputStream stream) throws Exception {
        if (stream != null) {
            try {
                return XmlUtil.fromXml(stream, DbFactoryConfig.class);
            } finally {
                stream.close();
            }
        }
        return null;
    }

    @ElementList(inline = true)
    List<DbConfig> configs = new ArrayList<>();

}
