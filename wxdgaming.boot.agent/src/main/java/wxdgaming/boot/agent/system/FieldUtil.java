package wxdgaming.boot.agent.system;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字段读取
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-10-20 15:35
 **/
@Slf4j
public class FieldUtil {

    /**
     * 获取所有字段，排除静态属性
     *
     * @param clazz
     * @return
     */
    public static Map<String, Field> getFields(boolean readStatic, Class<?> clazz) {
        Map<String, Field> mapf = new LinkedHashMap<>();
        getFields(readStatic, clazz, mapf);
        return mapf;
    }

    /**
     * 获取所有字段，排除静态属性
     *
     * @param clazz
     * @param mapf
     */
    public static void getFields(boolean readStatic, Class<?> clazz, Map<String, Field> mapf) {
        Class<?> scls = clazz.getSuperclass();
        if (scls != null && !Object.class.equals(scls)) {
            getFields(readStatic, scls, mapf);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!readStatic && Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            mapf.put(field.getName(), field);
        }
    }

    /**
     * 获取一个属性字段，包含父类的属性字段查找
     * 排除静态属性和最终属性字段
     *
     * @param clazz
     * @param name
     * @return
     */
    public static Field getField(Class<?> clazz, String name) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        Class<?> scls = clazz.getSuperclass();
        if (scls != null && !Object.class.equals(scls)) {
            return getField(scls, name);
        }
        return null;
    }

}
