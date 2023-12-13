package ${packageName}.bean;


import lombok.Getter;
import org.wxd.boot.batis.struct.DbTable;
import ${packageName}.bean.mapping.${codeClassName}Mapping;

import java.io.Serializable;


/**
* excel 构建 ${tableComment}
*
* @author: Troy.Chen(無心道, 15388152619)
* @version: ${.now?string("yyyy-MM-dd HH:mm:ss")}
**/
@Getter
@DbTable(name = "${tableName}", comment = "${tableComment}")
public class ${codeClassName}Bean extends ${codeClassName}Mapping implements Serializable {

}
