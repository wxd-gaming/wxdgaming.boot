package wxdgaming.boot.batis;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.xml.XmlUtil;

import java.io.InputStream;
import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Getter
@Setter
@Accessors(chain = true)
public class DbConfig extends ObjectBase implements Serializable, Cloneable {

    /** 先从外部配置config 目录读取 然后再从resources 读取 */
    public static DbConfig loadConfigXml(String fileName) throws Exception {
        Record2<String, InputStream> inputStream = FileUtil.findInputStream(fileName);
        if (inputStream == null) {
            throw new RuntimeException("文件不存在 " + fileName);
        }
        return loadConfigXml(inputStream.t2());
    }

    /** 先从外部配置config 目录读取 然后再从resources 读取 */
    public static DbConfig loadConfigXml(InputStream stream) throws Exception {
        if (stream != null) {
            try {
                return XmlUtil.fromXml(stream, DbConfig.class);
            } finally {
                stream.close();
            }
        }
        return null;
    }

    /** 命名 */
    @JSONField(ordinal = 1)
    private String name;
    /** 显示sql执行语句 */
    @JSONField(ordinal = 2)
    private boolean show_sql = false;
    /** 链接池 */
    @JSONField(ordinal = 3)
    private boolean connectionPool = false;
    /** 创建数据库 */
    @JSONField(ordinal = 4)
    private boolean createDbBase = true;
    /** 批处理线程数 */
    @JSONField(ordinal = 5)
    private int batchSizeThread;
    /** 数据库ip */
    @JSONField(ordinal = 6)
    private String dbHost;
    /** 数据库端口 */
    @JSONField(ordinal = 7)
    private int dbPort;
    /** 数据库名字 */
    @JSONField(ordinal = 8)
    private String dbBase;
    /** 用户 */
    @JSONField(ordinal = 9)
    private String dbUser;
    /** 密码 */
    @JSONField(ordinal = 10)
    private String dbPwd;
    @JSONField(ordinal = 11)
    private String scanPackage;

    public DbConfig clone(String dbName) throws CloneNotSupportedException {
        return clone().setDbBase(dbName);
    }

    @Override
    public DbConfig clone() throws CloneNotSupportedException {
        return (DbConfig) super.clone();
    }

    @Override
    public String toString() {
        return "DbConfig{name='%s', show_sql=%s, connectionPool=%s, batchSizeThread=%d, dbHost=%s:%d/%s, dbUser='%s'}"
                .formatted(name, show_sql, connectionPool, batchSizeThread, dbHost, dbPort, dbBase, dbUser);
    }
    //    @Override
    //    public String toString() {
    //        return new StringJoiner(", ", DbConfig.class.getSimpleName() + "[", "]")
    //                .add("name='" + name + "'")
    //                .add("show_sql=" + show_sql)
    //                .add("connectionPool=" + connectionPool)
    //                .add("batchSizeThread=" + batchSizeThread)
    //                .add("dbHost=" + dbHost + ":" + dbPort + "/" + dbBase)
    //                .add("dbUser='" + dbUser + "'")
    //                .toString();
    //    }

}
