package org.wxd.boot.batis.save;

import com.alibaba.fastjson.annotation.JSONField;
import org.wxd.boot.batis.struct.DbColumn;
import org.wxd.boot.core.field.extend.FieldAnn;
import org.wxd.boot.core.lang.ObjectBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-09-12 15:39
 **/
public abstract class ObjectSave extends ObjectBase implements CheckSaveCode {

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
