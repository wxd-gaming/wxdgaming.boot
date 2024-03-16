package wxdgaming.boot.batis;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * 避免线程并发带来的数据异常
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-10-12 20:48
 **/
@Getter
public final class DataBuilder {

    private final Object data;
    private final EntityTable entityTable;
    private final Map<EntityField, Object> dataMap;

    public DataBuilder(Object data, EntityTable entityTable, Map<EntityField, Object> dataMap) {
        this.data = data;
        this.entityTable = entityTable;
        this.dataMap = dataMap;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataBuilder that = (DataBuilder) o;

        return Objects.equals(data, that.data);
    }

    @Override public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override public String toString() {
        return entityTable.tableName + " - " + entityTable.tableComment;
    }

}
