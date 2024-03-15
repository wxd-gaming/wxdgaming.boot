package org.wxd.boot.batis.struct;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.EntityTable;
import org.wxd.boot.batis.store.DataRepository;
import org.wxd.boot.batis.store.ann.Keys;
import org.wxd.boot.core.append.StreamWriter;
import org.wxd.boot.core.field.extend.FieldAnn;
import org.wxd.boot.core.field.extend.FieldType;
import org.wxd.boot.core.lang.ConvertUtil;
import org.wxd.boot.core.str.json.FastJsonUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-19 14:12
 **/
@Getter
@Setter
@Accessors(chain = true)
public abstract class DbBean<T> {

    /** 返回实体类 */
    @DbColumn(alligator = true)
    @JSONField(serialize = false, deserialize = false)
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

    public DbBean setModelList(List<T> modelList) {
        this.modelList = modelList;
        if (modelList != null) {
            this.modelMap = new LinkedHashMap<>();
            this.modelList.forEach((dbModel) -> {
                try {
                    Object fieldValue = getDataMapping().getDataColumnKey().getFieldValue(dbModel);
                    if (this.modelMap.put(fieldValue, dbModel) != null) {
                        throw new RuntimeException("数据 主键 【" + fieldValue + "】 重复");
                    }
                    Keys keys = AnnUtil.ann(DbBean.this.getClass(), Keys.class);
                    if (keys != null) {
                        for (String s : keys.value()) {
                            String index = "";
                            String[] split = s.split(keys.split());
                            for (String string : split) {
                                EntityField entityField = getDataMapping().getColumnMap().get(string);
                                Object fv = entityField.getFieldValue(dbModel);
                                if (!index.isEmpty()) index += keys.split();
                                index += fv;
                            }
                            if (this.modelMap.put(index, dbModel) != null) {
                                throw new Throw("数据 自定义索引 【" + s + "】 【" + fieldValue + "】 重复 ");
                            }
                        }
                    }
                } catch (Throwable e) {
                    throw Throw.as("数据：" + FastJsonUtil.toJson(dbModel), e);
                }
            });
        }
        return this;
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

    @JSONField(serialize = false, deserialize = false)
    public void checkDb(DataRepository dataRepository) {
    }

    public String toDataString() {
        return toDataString(50);
    }

    public String toDataString(int len) {
        StreamWriter streamWriter = new StreamWriter();
        toDataString(streamWriter, len);
        return streamWriter.toString();
    }

    public void toDataString(StreamWriter streamWriter) {
        toDataString(streamWriter, 50);
    }

    public void toDataString(StreamWriter streamWriter, int len) {
        streamWriter.write("解析：").write(dataMapping.getLogTableName()).write("\n");
        streamWriter.write("表名：").write(dataMapping.getTableName()).write("\n");
        Collection<EntityField> columns = dataMapping.getColumns();


        streamWriter.writeRight("-", len * dataMapping.getColumns().size(), '-').write("\n");
        for (EntityField entityField : columns) {
            streamWriter.write("|").writeRight(entityField.getColumnName(), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|")
                    .writeRight(entityField.typeName(), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|")
                    .writeRight(entityField.checkColumnType().formatString(entityField.getColumnLength()), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|").writeRight(entityField.getColumnComment(), len, ' ');
        }

        streamWriter.write("\n");

        streamWriter.writeRight("-", len * columns.size(), '-').write("\n");
        for (T row : modelList) {
            streamWriter.write("\n");
            for (EntityField entityField : columns) {
                Object value = entityField.getFieldValue(row);
                if (value == null) {
                    streamWriter.write("|").writeRight("-", len, ' ');
                } else if (ConvertUtil.isBaseType(value.getClass())) {
                    streamWriter.write("|").writeRight(String.valueOf(value), len, ' ');
                } else {
                    streamWriter.write("|").writeRight(FastJsonUtil.toJson(value), len, ' ');
                }
            }
        }
        streamWriter.write("\n");
    }

}
