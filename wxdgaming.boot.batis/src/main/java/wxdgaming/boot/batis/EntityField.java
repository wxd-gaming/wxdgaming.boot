package wxdgaming.boot.batis;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.enums.SortType;
import wxdgaming.boot.core.field.FieldMapping;
import wxdgaming.boot.core.str.StringUtil;

import java.util.Objects;

/**
 * 字段映射
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-21 11:31
 */
@Getter
@Setter
public class EntityField extends FieldMapping {

    /** 数据库映射名字 */
    private String columnName;
    /** 数据库对应的类型 */
    private ColumnType columnType = ColumnType.None;
    /** 字段描述 */
    private String columnComment;
    /** 字段默认值 */
    private String defaultValue = "null";
    /** 扩展用的 */
    private String columnExtend;
    /** 用来防止比如本身是int 类型 但是excel获取的时候变成了double类型的 */
    private String fieldTypeString = null;
    /** 排序 */
    private SortType sortType;
    /** 字段长度 */
    private int columnLength;
    /** 主键列 */
    private boolean columnKey;
    /** 索引 */
    private boolean columnIndex;
    /** mysql 才有效的索引类型 */
    private String mysqlIndexType = "";
    // 字段是否为空
    private boolean columnNullAble;

    public EntityField() {
        this.columnName = "";
        this.columnLength = 0;
        this.sortType = SortType.ASC;
        this.columnKey = false;
        this.columnNullAble = true;
        this.columnComment = "";
    }

    public String getColumnName() {
        return columnName.toLowerCase();
    }

    @Override
    @JSONField(serialize = true)
    public String getFieldName() {
        String tmp = getColumnName();
        String[] s = tmp.split("[_-]");
        if (s.length > 1) {
            for (int i = 1; i < s.length; i++) {
                s[i] = StringUtil.upperFirst(s[i]);
            }
        }
        return String.join("", s);
    }


    /** 数据库自动描述 */
    public EntityField setColumnComment(String columnComment) {
        columnComment = columnComment.replace("'", "\\'");
        this.columnComment = columnComment;
        return this;
    }


    /**
     * 与数据库挂钩的类型，比如json
     */
    public ColumnType checkColumnType() {
        if (ColumnType.None.equals(columnType)) {
            throw new RuntimeException(getColumnName() + ", " + getField() + ", 字段数据库类型异常：" + columnType);
        }
        return columnType;
    }


    @Override
    public EntityField setFieldType(Class<?> fieldType) {
        super.setFieldType(fieldType);
        if (fieldType != null) {
            setFieldTypeString(fieldType.getSimpleName());
        }
        return this;
    }

    @Override public String typeName() {
        if (getField() == null) {
            return getFieldTypeString();
        }
        return super.typeName();
    }


    /** mysql 索引类型 {@code USING HASH} or {@code USING BTREE} */
    public String getMysqlIndexType() {
        if (mysqlIndexType == null) {
            mysqlIndexType = "";
        }
        return mysqlIndexType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityField entityField = (EntityField) o;

        return Objects.equals(columnName, entityField.columnName);
    }

    @Override
    public int hashCode() {
        return columnName != null ? columnName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "columnName='" + columnName + '\'' + ", columnType=" + columnType + ", " + super.toString();
    }
}
