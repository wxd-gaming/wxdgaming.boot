package ${packageName}.factory;


import lombok.Getter;
import org.wxd.boot.batis.struct.DbBean;
import ${packageName}.bean.${codeClassName}Bean;

import java.io.Serializable;


/**
 * excel 构建 ${tableComment}
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: ${.now?string("yyyy-MM-dd HH:mm:ss")}
 **/
@Getter
public class ${codeClassName}Factory extends DbBean<${codeClassName}Bean> implements Serializable {

}