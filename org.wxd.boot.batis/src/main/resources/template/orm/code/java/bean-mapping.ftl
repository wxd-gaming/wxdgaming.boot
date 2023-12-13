package ${packageName}.bean.mapping;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.batis.struct.DataChecked;
import org.wxd.boot.batis.struct.DbColumn;
import org.wxd.boot.batis.struct.DbTable;
import org.wxd.boot.lang.ObjectBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * excel 构建 ${tableComment}
 **/
@Getter
@Setter
@Accessors(chain = true)
@DbTable(mappedSuperclass = true, name = "${tableName}", comment = "${tableComment}")
public abstract class ${codeClassName}Mapping extends ObjectBase implements DataChecked, Serializable {

<#list columns as column>
    /** ${column.columnComment} */
    @DbColumn(name = "${column.columnName}"<#if column.columnKey==true>, key = true</#if><#if column.columnIndex==true>, index = true</#if>, comment = "${column.columnComment}")
    <#if column.fieldTypeString?starts_with("List<")>
    protected final ${column.fieldTypeString} ${column.fieldNameLower} = new ArrayList<>();
    <#else>
    protected ${column.fieldTypeString} ${column.fieldNameLower};
    </#if>
</#list>

}
