package org.wxd.boot.format.data;


import org.wxd.boot.field.ClassMapping;
import org.wxd.boot.field.ClassWrapper;
import org.wxd.boot.field.extend.FieldType;

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
