package ${packageName}.table;


import lombok.Getter;
import org.wxd.boot.batis.struct.DbBean;
import ${packageName}.bean.${codeClassName}Row;

import java.io.Serializable;


/**
 * excel 构建 ${tableComment}
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: ${.now?string("yyyy-MM-dd HH:mm:ss")}
 **/
@Getter
public class ${codeClassName}Table extends DbBean<${codeClassName}Row> implements Serializable {

    @Override public void initDb() {
        /*todo 实现一些数据分组*/

    }

}