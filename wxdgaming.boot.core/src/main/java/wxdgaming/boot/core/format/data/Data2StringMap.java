package wxdgaming.boot.core.format.data;


import wxdgaming.boot.core.field.ClassMapping;
import wxdgaming.boot.core.field.extend.FieldAnn;
import wxdgaming.boot.core.field.extend.FieldType;

import java.util.Map;

/**
 * 序列化处理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-16 16:55
 **/
public interface Data2StringMap extends DataMapping {

    /**
     * 把对象转化成map对象
     */
    default Map<String, String> toStringMap() throws Exception {
        return toStringMap(null);
    }

    /**
     * 把对象转化成map对象
     * {@link ClassMapping#toStringMap(Object)}
     *
     * @param fieldType 字段标注 {@link FieldAnn}
     */
    default Map<String, String> toStringMap(FieldType fieldType) throws Exception {
        return classMapping(fieldType).toStringMap(this);
    }

}
