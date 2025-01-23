package wxdgaming.boot.batis.text.json;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.struct.DataChecked;
import wxdgaming.boot.batis.text.TextEntityTable;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * json 格式处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-21 10:05
 **/
@Slf4j
public class JsonEntityTable extends TextEntityTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String encoded(Object dbModel) {
        JSONObject jsonObject = MapOf.newJSONObject();
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
