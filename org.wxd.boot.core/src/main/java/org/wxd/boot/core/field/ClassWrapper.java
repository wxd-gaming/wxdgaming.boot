package org.wxd.boot.core.field;


import org.wxd.boot.core.field.extend.FieldType;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * class解析
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-14 12:00
 **/
public class ClassWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ConcurrentMap<String, ClassMapping> classMappingMap = new ConcurrentHashMap<>();

    /**
     * @return
     */
    public static ConcurrentMap<String, ClassMapping> getClassMappingMap() {
        return classMappingMap;
    }

    /**
     * 把字符串值反射 类赋值
     *
     * @param clazz 需要辨识的类型
     * @return
     */
    public static ClassMapping wrapper(Class<?> clazz) {
        return wrapper(clazz, null);
    }

    /**
     * @param clazz     需要辨识的类型
     * @param fieldType 必须包含指定的类型
     * @return
     */
    public static ClassMapping wrapper(Class<?> clazz, FieldType fieldType) {
        return wrapper(clazz, true, true, null);
    }

    public static ClassMapping wrapper(Class<?> clazz,
                                       boolean filterFinal,
                                       boolean filterTransient,
                                       FieldType fieldType) {
        String clazzName = clazz.getName();
        if (fieldType != null) {
            /*避免验证需求不一致结果不一致*/
            clazzName += "_" + fieldType.name();
        }
        return classMappingMap.computeIfAbsent(clazzName,
                k -> new ClassMapping(clazz, filterFinal, filterTransient, fieldType)
        );
    }

}
