package ${packageName}.bean;


import lombok.Getter;
import wxdgaming.boot.batis.struct.DbTable;
import ${packageName}.bean.mapping.${codeClassName}Mapping;

import java.io.Serializable;


/**
* excel 构建 ${tableComment}
*
* @author: Troy.Chen(無心道, 15388152619)
* @version: ${date}
**/
@Getter
@DbTable(name = "${tableName}", comment = "${tableComment}")
public class ${codeClassName}Bean extends ${codeClassName}Mapping implements Serializable {

}
