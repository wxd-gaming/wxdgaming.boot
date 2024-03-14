package ${packageName}.bean.mapping;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.batis.struct.DataChecked;
import org.wxd.boot.batis.struct.DbColumn;
import org.wxd.boot.batis.struct.DbTable;

import java.io.Serializable;


/**
 * excel 构建 ${tableComment}
 *
 **/
@Getter
@Setter
@Accessors(chain = true)
@DbTable(mappedSuperclass = true, name = "${tableName}", comment = "${tableComment}")
public abstract class ${codeClassName}Mapping implements DataChecked, Serializable {

<#list columns as column>
    /** ${column.columnComment} */
    @DbColumn(name = "${column.columnName}"<#if column.columnKey==true>, key = true</#if><#if column.columnIndex==true>, index = true</#if>, comment = "${column.columnComment}")
    protected ${column.fieldTypeString} ${column.fieldName};
</#list>

}
