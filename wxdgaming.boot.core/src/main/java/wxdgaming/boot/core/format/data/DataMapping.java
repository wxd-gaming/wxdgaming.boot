package wxdgaming.boot.core.format.data;


import wxdgaming.boot.core.field.ClassMapping;
import wxdgaming.boot.core.field.ClassWrapper;
import wxdgaming.boot.core.field.extend.FieldType;

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
