package org.wxd.boot.batis.text.json;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.struct.DataChecked;
import org.wxd.boot.batis.text.TextEntityTable;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * json 格式处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-21 10:05
 **/
@Slf4j
public class JsonEntityTable extends TextEntityTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String encoded(Object dbModel) {
        ObjMap jsonObject = new ObjMap();
        for (Map.Entry<String, EntityField> entry : columnMap.entrySet()) {
            Object fieldValue = entry.getValue().getFieldValue(dbModel);
            if (fieldValue != null) {
                jsonObject.put(entry.getKey(), fieldValue);
            }
        }
        return jsonObject.toString();
    }

    @Override
    public Object decoded(String text) throws Exception {
        try {
            Object dbModel = FastJsonUtil.parse(text, entityClass);
            if (dbModel instanceof DataChecked) {
                ((DataChecked) dbModel).initAndCheck();
            }
            return dbModel;
        } catch (Exception e) {
            throw Throw.as(entityClass.getName() + ", json = " + text, e);

        }
    }

}
