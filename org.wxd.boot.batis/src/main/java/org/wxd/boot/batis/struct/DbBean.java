package org.wxd.boot.batis.struct;

import com.alibaba.fastjson.annotation.JSONField;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.EntityTable;
import org.wxd.boot.field.extend.FieldAnn;
import org.wxd.boot.field.extend.FieldType;
import org.wxd.boot.lang.ConvertUtil;
import org.wxd.boot.str.json.FastJsonUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-19 14:12
 **/
public abstract class DbBean<T> {

    @JSONField(serialize = false, deserialize = false)
    @DbColumn(alligator = true)
    @FieldAnn(alligator = true, fieldTypes = {FieldType.DB1, FieldType.DB2})
    protected transient final Class<?> entityClass;
    @JSONField(serialize = false, deserialize = false)
    @DbColumn(alligator = true)
    @FieldAnn(alligator = true)
    protected transient EntityTable dataMapping;
    protected List<T> modelList = null;
    protected Map<Object, T> modelMap = null;

    public DbBean() {
        entityClass = ReflectContext.getTClass(this.getClass());
    }

    public List<T> getModelList() {
        return modelList;
    }

    public DbBean setModelList(List<T> modelList) {
        this.modelList = modelList;
        if (modelList != null) {
            this.modelMap = new LinkedHashMap<>();
            this.modelList.forEach((dbModel) -> {
                try {
                    Object fieldValue = getDataStruct().getDataColumnKey().getFieldValue(dbModel);
                    this.modelMap.put(fieldValue, dbModel);
                } catch (Throwable e) {
                    throw Throw.as("数据：" + FastJsonUtil.toJson(dbModel), e);
                }
            });
        }
        return this;
    }

    public Map<Object, T> getModelMap() {
        return modelMap;
    }

    /**
     * 根据key值获取参数
     *
     * @param key
     * @return
     */
    public T get(Object key) {
        return modelMap.get(key);
    }

    public boolean containsKey(Object key) {
        return modelMap.containsKey(key);
    }

    /**
     * 返回实体类
     *
     * @return
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * 实体模型
     */
    public EntityTable getDataStruct() {
        return dataMapping;
    }

    /**
     * 实体模型
     */
    public DbBean<T> setDataStruct(EntityTable dataMapping) {
        this.dataMapping = dataMapping;
        return this;
    }

    @JSONField(serialize = false, deserialize = false)
    public int dbSize() {
        return this.modelList.size();
    }

    /**
     * 初始化
     */
    @JSONField(serialize = false, deserialize = false)
    public void initDb() {
    }

    public String toDataString() {
        return toDataString(50);
    }

    public String toDataString(int len) {
        StreamBuilder streamBuilder = new StreamBuilder();
        toDataString(streamBuilder, len);
        return streamBuilder.toString();
    }

    public void toDataString(StreamBuilder streamBuilder) {
        toDataString(streamBuilder, 50);
    }

    public void toDataString(StreamBuilder streamBuilder, int len) {
        streamBuilder.append("解析：").append(dataMapping.getLogTableName()).append("\n");
        streamBuilder.append("表名：").append(dataMapping.getTableName()).append("\n");
        Collection<EntityField> columns = dataMapping.getColumns();


        streamBuilder.appendRight("-", len * dataMapping.getColumns().size(), '-').append("\n");
        for (EntityField entityField : columns) {
            streamBuilder.append("|").appendRight(entityField.getColumnName(), len, ' ');
        }

        streamBuilder.append("\n");

        for (EntityField entityField : columns) {
            streamBuilder.append("|")
                    .appendRight(entityField.typeName(), len, ' ');
        }

        streamBuilder.append("\n");

        for (EntityField entityField : columns) {
            streamBuilder.append("|")
                    .appendRight(entityField.checkColumnType().formatString(entityField.getColumnLength()), len, ' ');
        }

        streamBuilder.append("\n");

        for (EntityField entityField : columns) {
            streamBuilder.append("|").appendRight(entityField.getColumnComment(), len, ' ');
        }

        streamBuilder.append("\n");

        streamBuilder.appendRight("-", len * columns.size(), '-').append("\n");
        for (T row : modelList) {
            streamBuilder.append("\n");
            for (EntityField entityField : columns) {
                Object value = entityField.getFieldValue(row);
                if (value == null) {
                    streamBuilder.append("|").appendRight("-", len, ' ');
                } else if (ConvertUtil.isBaseType(value.getClass())) {
                    streamBuilder.append("|").appendRight(String.valueOf(value), len, ' ');
                } else {
                    streamBuilder.append("|").appendRight(FastJsonUtil.toJson(value), len, ' ');
                }
            }
        }
        streamBuilder.append("\n");
    }

}
