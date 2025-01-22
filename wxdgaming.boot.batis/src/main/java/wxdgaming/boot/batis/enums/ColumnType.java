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
    Bool("bit", "bool"),
    Byte("tinyint(2)", "int2"),
    Short("tinyint(2)", "int2"),
    Int("int", "int4"),
    Long("bigint", "int8"),
    Float("float", "float4"),
    Double("double", "float8"),
    BigDecimal(true, "varchar", "varchar"),
    BigInteger(true, "varchar", "varchar"),
    /** gzip方式压缩存入数据 */
    Blob("blob", "blob"),
    /** gzip方式压缩存入数据 */
    LongBlob("longblob", "longblob"),
    /** bigint 实际存储的是 utc 毫秒 */
    Date("bigint", "int8"),
    /** varchar(%s) */
    Varchar(true, "varchar", "varchar"),
    /** text */
    Text("text", "text"),
    /** longtext */
    LongText("longtext", "text"),
    /** 数据库类型 longtext， 用 fastjson 的方式转化成字符串 */
    Json("json", "json"),
    /** 数据库类型 longtext， 用 fastjson 的方式转化成字符串 */
    Jsonb("json", "jsonb"),
    /** 数据库类型 longtext 用 fastjson 的方式转化成字符串，然后 gzip 压缩成 base64 字符串 */
    JsonGzip("longtext", "text"),
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
            return mysqlTypeName + "(%s)".formatted(len);
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

