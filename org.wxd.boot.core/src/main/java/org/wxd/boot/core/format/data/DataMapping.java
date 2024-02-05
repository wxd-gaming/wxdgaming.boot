package org.wxd.boot.core.format.data;


import org.wxd.boot.core.field.ClassMapping;
import org.wxd.boot.core.field.ClassWrapper;
import org.wxd.boot.core.field.extend.FieldType;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-02 09:45
 **/
public interface DataMapping {

    /**
     * 返回类的解析
     *
     * @param fieldType
     * @return
     */
    default ClassMapping classMapping(FieldType fieldType) {
        return ClassWrapper.wrapper(this.getClass(), fieldType);
    }

}
