package ${packageName}.table;


import lombok.Getter;
import wxdgaming.boot.batis.struct.DbBean;
import wxdgaming.boot.batis.store.JsonDataRepository;
import ${packageName}.bean.${codeClassName}Bean;

import java.io.Serializable;


/**
 * excel 构建 ${tableComment}
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: ${.now?string("yyyy-MM-dd HH:mm:ss")}
 **/
@Getter
public class ${codeClassName}Table extends DbBean<${codeClassName}Bean, JsonDataRepository> implements Serializable {

    @Override public void initDb() {
        /*todo 实现一些数据分组*/

    }

}