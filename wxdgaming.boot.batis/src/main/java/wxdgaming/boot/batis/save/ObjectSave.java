package wxdgaming.boot.batis.save;

import com.alibaba.fastjson.annotation.JSONField;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.core.field.extend.FieldAnn;
import wxdgaming.boot.core.lang.ObjectBaseLock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-09-12 15:39
 **/
public abstract class ObjectSave extends ObjectBaseLock implements CheckSaveCode {

    /*保存数据的 临时hashcode值*/
    @FieldAnn(alligator = true)
    @DbColumn(alligator = true)
    @JSONField(serialize = false, deserialize = false)
    private transient Map<String, Integer> saveCodeMap;

    public ObjectSave() {
    }

    @Override
    public Map<String, Integer> getSaveCodeMap() {
        if (saveCodeMap == null) {
            saveCodeMap = new HashMap<>();
        }
        return saveCodeMap;
    }

}
