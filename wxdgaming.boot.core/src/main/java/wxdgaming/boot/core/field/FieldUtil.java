package wxdgaming.boot.core.field;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.field.extend.FieldType;
import wxdgaming.boot.core.lang.ConvertUtil;

import java.lang.reflect.Field;

/**
 * 属性辅助类
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
@Slf4j
public class FieldUtil {

    /**
     * @param fieldTypes
     * @param fieldType
     * @return
     */
    public static final boolean checkFieldType(FieldType[] fieldTypes, FieldType fieldType) {
        if (fieldType == null) {
            return true;
        }
        if (fieldTypes != null) {
            for (FieldType ft : fieldTypes) {
                if (fieldType.equals(ft)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置属性值
     *
     * @param source
     * @param field
     * @param value
     */
    public static void setFieldValue(Object source, FieldMapping field, Object value) {
        try {
            field.setFieldValue(source, ConvertUtil.changeType(value, field.getField().getType()));
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    /**
     * 设置数字类型字段
     *
     * @param source
     * @param field
     * @param value
     */
    public static void setFieldValueByDouble(Object source, FieldMapping field, Double value) {
        try {
            if (field.getField().getType() == Integer.class || field.getField().getType() == int.class) {
                setFieldValue(source, field, value.intValue());
            } else if (field.getField().getType() == Float.class || field.getField().getType() == float.class) {
                setFieldValue(source, field, value.floatValue());
            } else if (field.getField().getType() == Double.class || field.getField().getType() == double.class) {
                setFieldValue(source, field, value);
            } else if (field.getField().getType() == Long.class || field.getField().getType() == long.class) {
                setFieldValue(source, field, value.longValue());
            } else if (field.getField().getType() == Byte.class || field.getField().getType() == byte.class) {
                setFieldValue(source, field, value.byteValue());
            } else if (field.getField().getType() == Short.class || field.getField().getType() == short.class) {
                setFieldValue(source, field, value.shortValue());
            }
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    /**
     * 获取数字类型字段
     *
     * @param source
     * @param field
     * @return
     */
    public static Double getFieldValueByDouble(Object source, FieldMapping field) {
        try {
            if (isNumberFrom(field.getField())) {
                return getFieldValue(source, field, Double.class);
            }
        } catch (Exception e) {
            throw Throw.of(e);
        }
        return 0D;
    }

    /**
     * 获取属性值
     *
     * @param source
     * @param field
     * @return
     */
    public static <T> T getFieldValue(Object source, FieldMapping field) {
        return getFieldValue(source, field, field.getField().getType());
    }

    /**
     * @param <T>
     * @param source
     * @param field
     * @param clazz
     * @return
     */
    public static <T> T getFieldValue(Object source, FieldMapping field, Class<?> clazz) {
        try {
            Object object = field.getFieldValue(source);
            return (T) ConvertUtil.changeType(object, clazz);
        } catch (Exception e) {
            log.error("获取参数错误" + field.toString(), e);
        }
        return null;
    }

    /**
     * 返回三个数字 -1 表示数字小于零，0 等于0，1 大于零表示
     *
     * @param field
     * @param source
     * @return
     */
    public static int isZore(FieldMapping field, Object source) {
        try {
            if (isNumberFrom(field.getField())) {
                Double a = field.getDouble(source);
                if (a == 0) {
                    return 0;
                } else if (a > 0) {
                    return 1;
                }
                return -1;
            }
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString(), e);
        }
        return 0;
    }


    /**
     * 是数值类型
     *
     * @param field
     * @return
     */
    public static boolean isNumberFrom(Field field) {
        Class<?> type = field.getType();
        return isNumberFrom(type);
    }

    /**
     * 是数值类型
     *
     * @param field
     * @return
     */
    public static boolean isNumberFrom(Class<?> field) {
        switch (field.getSimpleName().toLowerCase()) {
            case "number":
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
                return true;
        }
//        if (field.isAssignableFrom(Number.class)
//                || field.isAssignableFrom(byte.class)
//                || field.isAssignableFrom(short.class)
//                || field.isAssignableFrom(int.class)
//                || field.isAssignableFrom(long.class)
//                || field.isAssignableFrom(float.class)
//                || field.isAssignableFrom(double.class)) {
//            return true;
//        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(double.class.getSimpleName().toLowerCase());
        System.out.println(isNumberFrom(double.class));
        System.out.println(Double.class.getSimpleName().toLowerCase());
        System.out.println(isNumberFrom(Double.class));
    }

    /**
     * 属性字段做相加操作
     * 非基础类型直接替换
     *
     * @param source
     * @param field
     * @param value
     */
    public static void sumFieldValue(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (isNumberFrom(field.getField())) {
                Double toDouble = ConvertUtil.toDouble(value);
                if (toDouble == null || toDouble == 0) {
                    return;
                }
                Double a = field.getDouble(source);
                a += toDouble;
                setFieldValueByDouble(source, field, a);
            }
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * 属性字段做减法操作
     * 非数值类型直接替换
     *
     * @param source
     * @param field
     * @param value
     */
    public static void subtractFieldValue(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (isNumberFrom(field.getField())) {
                Double toDouble = ConvertUtil.toDouble(value);
                if (toDouble == null || toDouble == 0) {
                    return;
                }
                Double a = field.getDouble(source);
                a -= toDouble;
                setFieldValueByDouble(source, field, a);
            }
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     *
     * @param source
     * @param field
     * @param value
     */
    public static void multiplyFieldValue(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }

        try {
            if (isNumberFrom(field.getField())) {
                Double toDouble = ConvertUtil.toDouble(value);
                if (toDouble == null || toDouble == 0) {
                    return;
                }
                Double a = field.getDouble(source);
                a *= toDouble;
                setFieldValueByDouble(source, field, a);
            }
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * s1.属性 * ratio
     *
     * @param source
     * @param field
     * @param value
     */
    public static void multiplyValue(Object source, FieldMapping field, float value) {
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            if (value == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a *= value;
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * 除，如果是非数值类型，忽律
     *
     * @param source
     * @param field
     * @param value
     */
    public static void divideFieldValue(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            Double toDouble = ConvertUtil.toDouble(value);
            if (toDouble == null || toDouble == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a /= toDouble;
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * s1.属性 / ratio
     *
     * @param source
     * @param field
     * @param ratio
     */
    public static void divideValue(Object source, FieldMapping field, float ratio) {
        if (field == null || ratio == 0) {
            return;
        }
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            if (ratio == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a /= ratio;
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + ratio, e);
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     * <p>
     * s1.属性 *= 1+value/10000;
     *
     * @param source
     * @param field
     * @param value
     */
    public static void multiplyFieldValueByRatio1(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            Double toDouble = ConvertUtil.toDouble(value);
            if (toDouble == null || toDouble == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a *= 1 + toDouble / 10000;
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     * <p>
     * s1.属性 *= 1+value/100;
     *
     * @param source
     * @param field
     * @param value
     */
    public static void multiplyFieldValueByRatio2(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            Double toDouble = ConvertUtil.toDouble(value);
            if (toDouble == null || toDouble == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a *= 1 + toDouble / 100;
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     * <p>
     * s1.属性 *= 1+value;
     *
     * @param source
     * @param field
     * @param value
     */
    public static void multiplyFieldValueByRatio3(Object source, FieldMapping field, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            if (!isNumberFrom(field.getField())) {
                return;
            }
            Double toDouble = ConvertUtil.toDouble(value);
            if (toDouble == null || toDouble == 0) {
                return;
            }
            Double a = field.getDouble(source);
            a *= (1 + toDouble);
            setFieldValueByDouble(source, field, a);
        } catch (Exception e) {
            log.error("类型转换错误" + field.toString() + "," + value, e);
        }
    }

}
