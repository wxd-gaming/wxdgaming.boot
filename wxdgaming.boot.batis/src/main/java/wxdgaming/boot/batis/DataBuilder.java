package wxdgaming.boot.batis;

import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.agent.GlobalUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.timer.MyClock;

import java.util.Map;
import java.util.Objects;

/**
 * 避免线程并发带来的数据异常
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-10-12 20:48
 **/
@Getter
public final class DataBuilder {

    private final int index;
    private final String tableName;
    @Setter private String sql;
    private final Object data;
    private final EntityTable entityTable;
    private final Map<EntityField, Object> dataMap;

    public DataBuilder(int index, String tableName, Object data, EntityTable entityTable, Map<EntityField, Object> dataMap) {
        this.index = index;
        this.tableName = tableName;
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

    public void outErrorFile(String sqlStr, Throwable throwable) {
        EntityField dataColumnKey = entityTable.getDataColumnKey();
        Object object = dataMap.get(dataColumnKey);
        String string = MyClock.formatDate("yyyy/MM/dd");
        String msg = """
                数据库操作异常
                sql：%s
                数据：%s
                
                error：%s
                """.formatted(sqlStr, FastJsonUtil.toJsonWriteType(data), throwable);
        FileWriteUtil.writeString(
                "target/data_error/" + string + "/" + tableName + "_" + object + ".json",
                msg
        );
        GlobalUtil.exception(msg, throwable);
    }

}
