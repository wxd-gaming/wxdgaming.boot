package wxdgaming.boot.batis.enums;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-13 17:14
 **/
public enum ColumnType implements Serializable {
    None("null"),
    Bool("bit"),
    Byte("tinyint(2)"),
    Short("tinyint(2)"),
    Int("int"),
    Long("bigint"),
    Float("float"),
    Double("double"),
    BigDecimal("varchar(255)"),
    BigInteger("varchar(255)"),
    /** gzip方式压缩存入数据 */
    Blob("blob"),
    /** gzip方式压缩存入数据 */
    LongBlob("longblob"),
    /** bigint 实际存储的是 utc 毫秒 */
    Date("bigint"),
    /** varchar(%s) */
    Varchar(true, "varchar(%S)"),
    /** text */
    Text("text"),
    /** longtext */
    LongText("longtext"),
    /** 数据库类型 longtext， 用 fastjson 的方式转化成字符串 */
    Json("longtext"),
    /** 数据库类型 longtext 用 fastjson 的方式转化成字符串，然后 gzip 压缩成 base64 字符串 */
    JsonGzip("longtext"),
    ;
    private boolean format;
    private String typeName;

    ColumnType(String typeName) {
        this(false, typeName);
    }

    ColumnType(boolean format, String typeName) {
        this.format = format;
        this.typeName = typeName;
    }

    public String formatString(int len) {
        if (format) {
            if (len == 0) {
                len = 255;
            }
            return String.format(typeName, len);
        }
        return typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }

}

