package org.wxd.boot.batis;


import com.alibaba.fastjson.annotation.JSONField;
import org.wxd.boot.batis.enums.ColumnType;
import org.wxd.boot.batis.enums.SortType;
import org.wxd.boot.core.field.FieldMapping;
import org.wxd.boot.core.str.StringUtil;

import java.util.Objects;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
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
    /** 如果有第二格式判断 */
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

    @Override
    @JSONField(serialize = true)
    public String getFieldName() {
        String tmp = getColumnName();
        String[] s = tmp.split("_|-");
        if (s.length > 1) {
            for (int i = 1; i < s.length; i++) {
                s[i] = StringUtil.upperFirst(s[i]);
            }
        }
        return String.join("", s);
    }

    /**
     * 数据库映射名字
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 数据库映射名字
     */
    public EntityField setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 数据库自动描述
     */
    public String getColumnComment() {
        return columnComment;
    }

    /**
     * 数据库自动描述
     */
    public EntityField setColumnComment(String columnComment) {
        columnComment = columnComment.replace("'", "\'");
        this.columnComment = columnComment;
        return this;
    }

    /**
     * 默认值
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * 默认值
     */
    public EntityField setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    /**
     * 与数据库挂钩的类型，比如json
     */
    public ColumnType getColumnType() {
        return columnType;
    }

    /**
     * 与数据库挂钩的类型，比如json
     */
    public EntityField setColumnType(ColumnType columnType) {
        this.columnType = columnType;
        return this;
    }

    @Override
    public EntityField setFieldType(Class<?> fieldType) {
        super.setFieldType(fieldType);
        if (fieldType != null) {
            setFieldTypeString(fieldType.getSimpleName());
        }
        return this;
    }

    /**
     * 用来防止比如本身是int 类型 但是excel获取的时候变成了double类型的
     */
    public String getFieldTypeString() {
        return fieldTypeString;
    }

    /**
     * 用来防止比如本身是int 类型 但是excel获取的时候变成了double类型的
     */
    public EntityField setFieldTypeString(String fieldTypeString) {
        this.fieldTypeString = fieldTypeString;
        return this;
    }

    @Override public String typeName() {
        if (getField() == null) {
            return getFieldTypeString();
        }
        return super.typeName();
    }

    public String getColumnExtend() {
        return columnExtend;
    }

    public EntityField setColumnExtend(String columnExtend) {
        this.columnExtend = columnExtend;
        return this;
    }

    /**
     * 字段长度
     */
    public int getColumnLength() {
        return columnLength;
    }

    public EntityField setColumnLength(int columnLength) {
        this.columnLength = columnLength;
        return this;
    }

    /**
     * 索引
     */
    public boolean isColumnIndex() {
        return columnIndex;
    }

    /**
     * 索引
     */
    public EntityField setColumnIndex(boolean columnIndex) {
        this.columnIndex = columnIndex;
        return this;
    }

    /**
     * mysql 索引类型 {@code USING HASH} or {@code USING BTREE}
     *
     * @return
     */
    public String getMysqlIndexType() {
        if (mysqlIndexType == null) {
            mysqlIndexType = "";
        }
        return mysqlIndexType;
    }

    /**
     * mysql 索引类型 {@code USING HASH} or {@code USING BTREE}
     *
     * @return
     */
    public EntityField setMysqlIndexType(String mysqlIndexType) {
        this.mysqlIndexType = mysqlIndexType;
        return this;
    }

    /**
     * 主键列
     */
    public boolean isColumnKey() {
        return columnKey;
    }

    /**
     * 主键
     */
    public EntityField setColumnKey(boolean columnKey) {
        this.columnKey = columnKey;
        return this;
    }

    /**
     * 自动是否可以null， true 可以为null
     */
    public boolean isColumnNullAble() {
        return columnNullAble;
    }

    /**
     * true 可以为null
     */
    public EntityField setColumnNullAble(boolean columnNullAble) {
        this.columnNullAble = columnNullAble;
        return this;
    }

    /**
     * 读取数据默认排序
     */
    public SortType getSortType() {
        return sortType;
    }

    /**
     * 读取数据默认排序
     */
    public EntityField setSortType(SortType sortType) {
        this.sortType = sortType;
        return this;
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
