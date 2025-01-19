package wxdgaming.boot.batis.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-13 17:14
 **/
@Getter
public enum ColumnType implements Serializable {
    None("null", "null"),
    Bool("bit", "boo"),
    Byte("tinyint(2)", "tinyint(2)"),
    Short("tinyint(2)", "smallint"),
    Int("int", "integer"),
    Long("bigint", "bigint"),
    Float("float", "float"),
    Double("double", "double"),
    BigDecimal("varchar(255)", "varchar(255)"),
    BigInteger("varchar(255)", "varchar(255)"),
    /** gzip方式压缩存入数据 */
    Blob("blob", "blob"),
    /** gzip方式压缩存入数据 */
    LongBlob("longblob", "longblob"),
    /** bigint 实际存储的是 utc 毫秒 */
    Date("bigint", "bigint"),
    /** varchar(%s) */
    Varchar(true, "varchar(%S)", "varchar(%S)"),
    /** text */
    Text("text", "text"),
    /** longtext */
    LongText("longtext", "longtext"),
    /** 数据库类型 longtext， 用 fastjson 的方式转化成字符串 */
    Json("json", "json"),
    /** 数据库类型 longtext， 用 fastjson 的方式转化成字符串 */
    Jsonb("json", "jsonb"),
    /** 数据库类型 longtext 用 fastjson 的方式转化成字符串，然后 gzip 压缩成 base64 字符串 */
    JsonGzip("longtext", "longtext"),
    ;
    private final boolean format;
    private final String mysqlTypeName;
    private final String pgsqlTypeName;

    ColumnType(String mysqlTypeName, String pgsqlTypeName) {
        this(false, mysqlTypeName, pgsqlTypeName);
    }

    ColumnType(boolean format, String mysqlTypeName, String pgsqlTypeName) {
        this.format = format;
        this.mysqlTypeName = mysqlTypeName;
        this.pgsqlTypeName = pgsqlTypeName;
    }

    public String formatString(int len) {
        if (format) {
            if (len == 0) {
                len = 255;
            }
            return String.format(mysqlTypeName, len);
        }
        return mysqlTypeName;
    }

    public String pgsqlFormatString(int len) {
        if (format) {
            if (len == 0) {
                len = 255;
            }
            return String.format(pgsqlTypeName, len);
        }
        return pgsqlTypeName;
    }

    @Override
    public String toString() {
        return mysqlTypeName;
    }

}

