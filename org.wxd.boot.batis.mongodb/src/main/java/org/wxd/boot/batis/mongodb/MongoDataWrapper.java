package org.wxd.boot.batis.mongodb;

import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.wxd.boot.batis.DataWrapper;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.struct.DbColumn;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 数据映射装填器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-21 10:15
 **/
public class MongoDataWrapper extends DataWrapper<MongoEntityTable> implements Serializable {

    public static MongoDataWrapper Default = new MongoDataWrapper();

    @Override
    public MongoEntityTable createEntityTable() {
        return new MongoEntityTable();
    }

    public MongoQueryBuilder queryWrapper() {
        return new MongoQueryBuilder(this);
    }

    @Override
    public String columnName(Field field, DbColumn dbColumn) {
        if (dbColumn != null && dbColumn.key()) return "_id";
        return super.columnName(field, dbColumn);
    }

    public Document document(Object source) {
        MongoEntityTable entityTable = asEntityTable(source);
        return document(entityTable, source);
    }

    public Document document(MongoEntityTable entityTable, Object source) {
        Document d = new Document();
        document(d, entityTable, source);
        return d;
    }

    public void document(Document d, MongoEntityTable entityTable, Object source) {
        Collection<EntityField> columns = entityTable.getColumns();
        for (EntityField entityField : columns) {
            Object ov1 = entityField.getFieldValue(source);
            if (ov1 != null) {
                ov1 = toDbValue(entityField, ov1);
            }
            if (ov1 != null) {
                d.append(entityField.getColumnName(), ov1);
            }
        }
    }

    /**
     * 构建主键查询语句
     */
    public Document buildWhere(Object source) {
        MongoEntityTable entityTable = asEntityTable(source);
        return buildWhere(entityTable, source);
    }

    /**
     * 构建主键查询语句
     */
    public Document buildWhere(MongoEntityTable entityTable, Object source) {
        EntityField entityFieldKey = entityTable.getDataColumnKey();
        Object keyParam = entityFieldKey.getFieldValue(source);
        return build(entityFieldKey, keyParam);
    }

    public Document build(EntityField mapping, Object source) {
        Document bson = new Document();
        build(bson, mapping, source);
        return bson;
    }

    public void build(Document bson, EntityField mapping, Object source) {
        source = toDbValue(mapping, source);
        bson.append(mapping.getColumnName(), source);
    }

    @Override
    public Object toDbValue(EntityField columnMapping, Object source) {
        if (source instanceof Bson) {
            return source;
        }
        if (source instanceof BSONObject) {
            return source;
        }
        if (source instanceof String) {
            String str = (String) source;
            /*传入的参数模糊表达式*/
            if (str.startsWith("^.*") && str.endsWith(".*$")) {
                return Pattern.compile(str);
            }
        }
        return super.toDbValue(columnMapping, source);
    }

    @Override
    public Object toDbBoolValue(Object source) {
        return new BsonBoolean((Boolean) super.toDbBoolValue(source));
    }

    @Override
    public Object toDbByteValue(Object source) {
        return new BsonInt32((Byte) super.toDbByteValue(source));
    }

    @Override
    public Object toDbShortValue(Object source) {
        return new BsonInt32((Short) super.toDbShortValue(source));
    }

    @Override
    public Object toDbIntValue(Object source) {
        return new BsonInt32((Integer) super.toDbIntValue(source));
    }

    @Override
    public Object toDbLongValue(Object source) {
        return new BsonInt64((Long) super.toDbLongValue(source));
    }

    @Override
    public Object toDbFloatValue(Object source) {
        return new BsonDouble((Float) super.toDbFloatValue(source));
    }

    @Override
    public Object toDbDoubleValue(Object source) {
        return new BsonDouble((Double) super.toDbDoubleValue(source));
    }

    @Override
    public Object toDbDateValue(Object source) {
        return super.toDbDateValue(source);
    }

    @Override
    public Object toDbBlobValue(Object source) {
        return new BsonBinary((byte[]) super.toDbBlobValue(source));
    }

    @Override
    public Object fromDbBlobValue(EntityField columnMapping, Object source) {
        Binary binary = (Binary) source;
        return super.fromDbBlobValue(columnMapping, binary.getData());
    }
}
