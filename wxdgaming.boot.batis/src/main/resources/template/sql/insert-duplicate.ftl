insert into `${tableName}` (<#list columns as column><#if column_index gt 0>,</#if>`${column}`</#list>)
values (<#list columns as column><#if column_index gt 0>,</#if>?</#list>)
ON DUPLICATE KEY UPDATE
<#list columns as column><#if column_index gt 0>,</#if>`${column}`=?</#list>