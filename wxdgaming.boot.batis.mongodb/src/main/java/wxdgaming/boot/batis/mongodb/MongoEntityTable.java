package wxdgaming.boot.batis.mongodb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.EntityTable;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-16 11:26
 **/
public class MongoEntityTable extends EntityTable implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MongoEntityTable.class);

    @Override
    public EntityField getDataColumnKey() {
        if (super.getDataColumnKey() != null) {
            if (!"_id".equals(super.getDataColumnKey().getColumnName())) {
                super.getDataColumnKey().setColumnName("_id");
            }
        }
        return super.getDataColumnKey();
    }

    @Override
    public EntityTable setDataColumnKey(EntityField dataColumnKey) {
        if (!"_id".equals(dataColumnKey.getColumnName())) {
            dataColumnKey.setColumnName("_id");
        }
        return super.setDataColumnKey(dataColumnKey);
    }

}
