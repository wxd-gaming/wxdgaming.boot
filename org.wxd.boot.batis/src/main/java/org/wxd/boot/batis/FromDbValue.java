package org.wxd.boot.batis;

import com.google.protobuf.Message;
import org.wxd.boot.agent.zip.GzipUtil;
import org.wxd.boot.lang.ConvertUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.str.json.ProtobufSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-24 11:49
 **/
interface FromDbValue {

    default Object fromDbValue(EntityField columnMapping, Object source) throws Exception {
        switch (columnMapping.getColumnType()) {
            case None:
                throw new RuntimeException("不支持的类型 none");
            case Bool:
                source = fromDbBoolValue(columnMapping, source);
                break;
            case Byte:
                source = fromDbByteValue(columnMapping, source);
                break;
            case Short:
                source = fromDbShortValue(columnMapping, source);
                break;
            case Int:
                source = fromDbIntValue(columnMapping, source);
                break;
            case Long:
                source = fromDbLongValue(columnMapping, source);
                break;
            case Float:
                source = fromDbFloatValue(columnMapping, source);
                break;
            case Double:
                source = fromDbDoubleValue(columnMapping, source);
                break;
            case BigInteger:
                source = fromDbBigIntegerValue(columnMapping, source);
                break;
            case BigDecimal:
                source = fromDbBigDecimalValue(columnMapping, source);
                break;
            case Date:
                source = fromDbDateValue(columnMapping, source);
                break;
            case Varchar:
            case Text:
            case LongText:
            case Json:
                source = fromDbStringValue(columnMapping, source);
                break;
            case JsonGzip: {
                if (source != null) {
                    String gzipString = source.toString();
                    source = fromDbStringValue(columnMapping, GzipUtil.unGzipBase64(gzipString));
                }
            }
            break;
            case Blob:
            case LongBlob:
                source = fromDbBlobValue(columnMapping, source);
                break;
            default: {
                source = fromDbBlobValue(columnMapping, source);
            }
            break;
        }
        return source;
    }

    default Object fromDbBoolValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = Boolean.FALSE;
        } else {
            source = ConvertUtil.changeType(source, Boolean.class);
        }
        return source;
    }

    default Object fromDbByteValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = (byte) 0;
        } else {
            source = ConvertUtil.changeType(source, Byte.class);
        }
        return source;
    }

    default Object fromDbShortValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = (short) 0;
        } else {
            source = ConvertUtil.changeType(source, Short.class);
        }
        return source;
    }

    default Object fromDbIntValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = 0;
        } else {
            source = ConvertUtil.changeType(source, Integer.class);
        }
        return source;
    }

    default Object fromDbLongValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = 0L;
        } else {
            source = ConvertUtil.changeType(source, Long.class);
        }
        return source;
    }

    default Object fromDbFloatValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = 0f;
        } else {
            source = ConvertUtil.changeType(source, Float.class);
        }
        return source;
    }

    default Object fromDbDoubleValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = 0d;
        } else {
            source = ConvertUtil.changeType(source, Double.class);
        }
        return source;
    }

    default Object fromDbBigIntegerValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = new BigInteger("0");
        } else {
            source = FastJsonUtil.parse(source.toString(), BigInteger.class);
        }
        return source;
    }

    default Object fromDbBigDecimalValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = new BigDecimal("0");
        } else {
            source = FastJsonUtil.parse(source.toString(), BigDecimal.class);
        }
        return source;
    }

    default Object fromDbDateValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = null;
        } else {
            source = new Date((long) source);
        }
        return source;
    }

    default Object fromDbStringValue(EntityField columnMapping, Object source) {
        if (source == null) {
            source = null;
        } else if (columnMapping.getFieldType() == String.class && source instanceof String) {
            source = source.toString();
        } else if (Message.class.isAssignableFrom(columnMapping.getFieldType())) {
            source = ProtobufSerializer.parse4Json(source.toString(), columnMapping.getFieldType());
        } else if (Message.Builder.class.isAssignableFrom(columnMapping.getFieldType())) {
            source = ProtobufSerializer.parse4Json(source.toString(), columnMapping.getFieldType());
        } else {
            source = FastJsonUtil.parse(source.toString(), columnMapping.jsonFieldType());
        }
        return source;
    }

    default Object fromDbBlobValue(EntityField columnMapping, Object source) {
        if (source == null) {
            return null;
        } else {
            source = GzipUtil.unGZip((byte[]) source);
        }

        if (Message.class.isAssignableFrom(columnMapping.getFieldType())) {
            source = ProtobufSerializer.parse4Bytes(columnMapping.getFieldType(), (byte[]) source);
        } else if (Message.Builder.class.isAssignableFrom(columnMapping.getFieldType())) {
            source = ProtobufSerializer.parse4Bytes(columnMapping.getFieldType(), (byte[]) source);
        } else {
            source = FastJsonUtil.parse((byte[]) source, columnMapping.jsonFieldType());
        }

        return source;
    }

}
