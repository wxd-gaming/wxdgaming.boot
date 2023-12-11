package org.wxd.boot.batis;

import lombok.Getter;

import java.util.Map;

/**
 * 避免线程并发带来的数据异常
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-12 20:48
 **/
@Getter
public final class DataBuilder {

    private Object data;
    private EntityTable entityTable;
    private Map<EntityField, Object> dataMap;

    public DataBuilder(Object data, EntityTable entityTable, Map<EntityField, Object> dataMap) {
        this.data = data;
        this.entityTable = entityTable;
        this.dataMap = dataMap;
    }

    @Override public String toString() {
        return entityTable.tableName + " - " + entityTable.tableComment;
    }

}
