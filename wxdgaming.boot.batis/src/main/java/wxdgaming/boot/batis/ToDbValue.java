package wxdgaming.boot.batis;

import com.google.protobuf.Message;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.core.lang.ConvertUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.str.json.ProtobufSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-24 11:49
 **/
interface ToDbValue {

    default Object toDbValue(EntityField columnMapping, Object source) {
        return toDbValue(columnMapping.getColumnType(), source);
    }

    default Object toDbValue(ColumnType columnType, Object source) {
        switch (columnType) {
            case None:
                throw new RuntimeException("不支持的类型 none");
            case Bool:
                source = toDbBoolValue(source);
                break;
            case Byte:
                source = toDbByteValue(source);
                break;
            case Short:
                source = toDbShortValue(source);
                break;
            case Int:
                source = toDbIntValue(source);
                break;
            case Long:
                source = toDbLongValue(source);
                break;
            case Float:
                source = toDbFloatValue(source);
                break;
            case Double:
                source = toDbDoubleValue(source);
                break;
            case BigInteger:
                source = toDbBigIntegerValue(source);
                break;
            case BigDecimal:
                source = toDbBigDecimalValue(source);
                break;
            case Date:
                source = toDbDateValue(source);
                break;
            case Varchar:
            case Text:
            case LongText:
            case Json:
                source = toDbStringValue(source);
                break;
            case JsonGzip: {
                source = toDbStringValue(source);
                if (source != null) {
                    source = GzipUtil.gzip2Base64(source.toString());
                }
            }
            break;
            case Blob:
            case LongBlob:
                source = toDbBlobValue(source);
                break;
            default: {
                source = toDbBlobValue(source);
            }
            break;
        }
        return source;
    }

    default Object toDbBoolValue(Object source) {
        if (source == null) {
            source = Boolean.FALSE;
        } else {
            source = ConvertUtil.changeType(source, Boolean.class);
        }
        return source;
    }

    default Object toDbByteValue(Object source) {
        if (source == null) {
            source = (byte) 0;
        } else {
            source = ConvertUtil.changeType(source, Byte.class);
        }
        return source;
    }

    default Object toDbShortValue(Object source) {
        if (source == null) {
            source = (short) 0;
        } else {
            source = ConvertUtil.changeType(source, Short.class);
        }
        return source;
    }

    default Object toDbIntValue(Object source) {
        if (source == null) {
            source = 0;
        } else {
            source = ConvertUtil.changeType(source, Integer.class);
        }
        return source;
    }

    default Object toDbLongValue(Object source) {
        if (source == null) {
            source = 0L;
        } else {
            source = ConvertUtil.changeType(source, Long.class);
        }
        return source;
    }

    default Object toDbFloatValue(Object source) {
        if (source == null) {
            source = 0f;
        } else {
            source = ConvertUtil.changeType(source, Float.class);
        }
        return source;
    }

    default Object toDbDoubleValue(Object source) {
        if (source == null) {
            source = 0d;
        } else {
            source = ConvertUtil.changeType(source, Double.class);
        }
        return source;
    }

    default Object toDbBigIntegerValue(Object source) {
        if (source == null) {
            source = FastJsonUtil.toJson(new BigInteger("0"));
        } else {
            source = FastJsonUtil.toJson(source);
        }
        return source;
    }

    default Object toDbBigDecimalValue(Object source) {
        if (source == null) {
            source = FastJsonUtil.toJson(new BigDecimal("0"));
        } else {
            source = FastJsonUtil.toJson(source);
        }
        return source;
    }

    default Object toDbDateValue(Object source) {
        if (source == null) {
            source = System.currentTimeMillis();
        }
        if (source instanceof Date) {
            source = ((Date) source).getTime();
        }
        return source;
    }

    default Object toDbStringValue(Object source) {
        if (source == null) {
            source = "";
        } else if (source instanceof Message) {
            source = ProtobufSerializer.toJson((Message) source);
        } else if (source instanceof Message.Builder) {
            source = ProtobufSerializer.toJson((Message.Builder) source);
        } else if (!(source instanceof String)) {
            source = FastJsonUtil.toJsonWriteType(source);
        }
        return source;
    }

    default Object toDbBlobValue(Object source) {
        if (source != null) {
            if (source instanceof Message) {
                source = ((Message) source).toByteArray();
            } else if (source instanceof Message.Builder) {
                source = ((Message.Builder) source).build().toByteArray();
            } else if (!(source instanceof byte[])) {
                source = FastJsonUtil.toBytesWriteType(source);
            }
            source = GzipUtil.gzip((byte[]) source);
        }
        return source;
    }

}
