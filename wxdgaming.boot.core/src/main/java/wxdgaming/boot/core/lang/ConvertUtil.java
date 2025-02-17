package wxdgaming.boot.core.lang;

import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

/**
 * 辅助类型转换，泛型类型转换
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class ConvertUtil {

    static final ArrayList<Class<?>> BASE_TYPE_SET = new ArrayList<>();

    static {
        BASE_TYPE_SET.add(Boolean.class);
        BASE_TYPE_SET.add(boolean.class);
        BASE_TYPE_SET.add(char.class);
        BASE_TYPE_SET.add(Date.class);
        BASE_TYPE_SET.add(String.class);
        BASE_TYPE_SET.add(Byte.class);
        BASE_TYPE_SET.add(byte.class);
        BASE_TYPE_SET.add(Short.class);
        BASE_TYPE_SET.add(short.class);
        BASE_TYPE_SET.add(Integer.class);
        BASE_TYPE_SET.add(int.class);
        BASE_TYPE_SET.add(Long.class);
        BASE_TYPE_SET.add(long.class);
        BASE_TYPE_SET.add(Float.class);
        BASE_TYPE_SET.add(float.class);
        BASE_TYPE_SET.add(Double.class);
        BASE_TYPE_SET.add(double.class);
        BASE_TYPE_SET.add(BigInteger.class);
        BASE_TYPE_SET.add(BigDecimal.class);
    }

    /**
     * 验证是否是基础类型，排除了Object, 方便json格式使用
     *
     * @param clazz
     * @return
     */
    public static boolean isBaseType(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> class1 : BASE_TYPE_SET) {
                if (class1.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 常量类型
     */
    public enum TypeCode {
        /**
         * 默认值，null
         */
        Default(ConvertUtil.class),
        Boolean(java.lang.Boolean.class, boolean.class),
        Char(char.class),
        Date(java.util.Date.class),
        String(java.lang.String.class),
        Object(java.lang.Object.class),
        Byte(java.lang.Byte.class, byte.class),
        Short(java.lang.Short.class, short.class),
        Integer(java.lang.Integer.class, int.class),
        Long(java.lang.Long.class, long.class),
        Float(java.lang.Float.class, float.class),
        Double(java.lang.Double.class, double.class),
        BigInteger(java.math.BigInteger.class),
        BigDecimal(java.math.BigDecimal.class),
        ;

        private Class<?>[] clazzs;

        TypeCode(Class<?>... clazzs) {
            this.clazzs = clazzs;
        }

        public Class<?>[] getClazzs() {
            return clazzs;
        }

        /**
         * @param clazz
         * @return
         */
        public static TypeCode getTypeCode(Class<?> clazz) {
            if (clazz != null) {
                TypeCode[] values = TypeCode.values();
                for (TypeCode value : values) {
                    for (Class<?> tmpClass : value.getClazzs()) {
                        if (tmpClass.equals(clazz)) {
                            return value;
                        }
                    }
                }
            }
            return TypeCode.Default;
        }

        /**
         * @param clazz
         * @return
         */
        public static TypeCode getTypeCode(String clazz) {
            if (clazz != null) {
                TypeCode[] values = TypeCode.values();
                for (TypeCode value : values) {
                    for (Class<?> tmpClass : value.getClazzs()) {
                        if (tmpClass.getName().equalsIgnoreCase(clazz)
                                || tmpClass.getSimpleName().equalsIgnoreCase(clazz)) {
                            return value;
                        }
                    }
                }
            }
            return TypeCode.Default;
        }

    }

    /**
     * 类型转换
     *
     * @param obj
     * @param clazz
     * @return
     */
    public static Object changeType(Object obj, Class<?> clazz) {
        if (obj == null) {
            return null;
        }
        if (StringUtils.isBlank(obj.toString())) {
            return defaultValue(clazz);
        }
        if (clazz.isInstance(obj) || clazz.isAssignableFrom(obj.getClass())) {
            return obj;
        }
        final TypeCode typeCode = TypeCode.getTypeCode(clazz);
        /*如果等于，或者所与继承关系*/
        switch (typeCode) {
            case Char:
                return String.valueOf(obj).toCharArray()[0];
            case Boolean:
                return convert(obj, Boolean::valueOf, false);
            case Byte:
                return convert(obj, Byte::valueOf, true);
            case Short:
                return convert(obj, Short::valueOf, true);
            case Integer:
                return convert(obj, Integer::valueOf, true);
            case Long:
                return convert(obj, Long::valueOf, true);
            case Float:
                return convert(obj, Float::valueOf, true);
            case Double:
                return convert(obj, Double::valueOf, true);
            case String:
                return String.valueOf(obj);
            default: {
                return FastJsonUtil.parse(String.valueOf(obj), clazz);
            }
        }
    }

    private static <R> R convert(Object obj, Function<String, R> function, boolean useDouble) {
        String s = String.valueOf(obj);
        try {
            return function.apply(s);
        } catch (Exception e) {
            if (useDouble) {
                String s1 = String.valueOf(new BigDecimal(s).longValue());
                return function.apply(s1);
            }
            throw e;
        }
    }

    /**
     * 类型转换
     *
     * @param clazz
     * @return
     */
    public static <R> R defaultValue(Class<?> clazz) {
        final TypeCode typeCode = TypeCode.getTypeCode(clazz);
        /*如果等于，或者所与继承关系*/
        switch (typeCode) {
            case Char:
                throw new UnsupportedOperationException();
            case String:
                return (R) "";
            case Date:
                return (R) (new Date());
            case Boolean:
                return (R) Boolean.FALSE;
            case Byte:
                byte b = 0;
                return (R) Byte.valueOf(b);
            case Short:
                short s = 0;
                return (R) Short.valueOf(s);
            case Integer:
                return (R) Integer.valueOf(0);
            case Long:
                return (R) Long.valueOf(0);
            case Float:
                return (R) Float.valueOf(0f);
            case Double:
                return (R) Double.valueOf(0);
            case BigInteger:
                return (R) BigInteger.valueOf(0);
            case BigDecimal:
                return (R) BigDecimal.valueOf(0);
            default: {
                return null;
            }
        }
    }

    /**
     * 把对象转化成 Byte
     *
     * @param obj
     * @return
     */
    public static Byte toByte(Object obj) {
        return (Byte) changeType(obj, Byte.class);
    }

    public static byte toByteValue(Object obj) {
        if (obj == null) {
            return 0;
        }
        return (byte) changeType(obj, Byte.class);
    }

    public static Boolean toBoolean(Object obj) {
        return (Boolean) changeType(obj, Boolean.class);
    }

    public static boolean toBooleanValue(Object obj) {
        if (obj == null) {
            return false;
        }
        return (boolean) changeType(obj, boolean.class);
    }

    /**
     * 把对象转化成 Short
     *
     * @param obj
     * @return
     */
    public static Short toShort(Object obj) {
        return (Short) changeType(obj, Short.class);
    }

    public static short toShortValue(Object obj) {
        if (obj == null) {
            return 0;
        }
        return (short) changeType(obj, Short.class);
    }

    /**
     * 把对象转化成 Integer
     *
     * @param obj
     * @return
     */
    public static Integer toInteger(Object obj) {
        return (Integer) changeType(obj, Integer.class);
    }

    public static int toIntValue(Object obj) {
        if (obj == null) {
            return 0;
        }
        return (int) changeType(obj, Integer.class);
    }

    /**
     * 把对象转化成 Long
     *
     * @param obj
     * @return
     */
    public static Long toLong(Object obj) {
        return (Long) changeType(obj, Long.class);
    }

    public static long toLongValue(Object obj) {
        if (obj == null) {
            return 0;
        }
        return (long) changeType(obj, Long.class);
    }

    /**
     * 把对象转化成 Float
     *
     * @param obj
     * @return
     */
    public static Float toFloat(Object obj) {
        return (Float) changeType(obj, Float.class);
    }

    public static float toFloatValue(Object obj) {
        if (obj == null) {
            return 0f;
        }
        return (float) changeType(obj, Float.class);
    }

    /**
     * 把对象转化成 Double
     *
     * @param obj
     * @return
     */
    public static Double toDouble(Object obj) {
        return (Double) changeType(obj, Double.class);
    }

    /**
     * 返回 0
     *
     * @param obj
     * @return
     */
    public static double toDoubleValue(Object obj) {
        if (obj == null) {
            return 0d;
        }
        return (double) changeType(obj, Double.class);
    }

    /**
     * 把对象转化为字符串
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return (String) changeType(obj, String.class);
    }

    /**
     * 如果异常返回 ""
     *
     * @param obj
     * @return
     */
    public static String toStr(Object obj) {
        if (obj == null) {
            return "";
        }
        return (String) changeType(obj, String.class);
    }

    /**
     * 保留2位小数函数
     *
     * @param souse
     * @return
     */
    static public float float2(float souse) {
        return Math.round(souse * 100f) / 100f;
    }

    /**
     * 保留4位小数函数
     *
     * @param souse
     * @return
     */
    static public float float4(float souse) {
        return Math.round(souse * 10000f) / 10000f;
    }

    /**
     * 保留2位小数函数
     *
     * @param souse
     * @return
     */
    static public double double2(double souse) {
        return Math.round(souse * 100) / 100d;
    }

    /**
     * 保留4位小数函数
     *
     * @param souse
     * @return
     */
    static public double double4(double souse) {
        return Math.round(souse * 10000) / 10000d;
    }

    public static void main(String[] args) {
        Object ob = "1101217240600000009";
        Object str = changeType(ob, Long.class);
        System.out.println(str);
    }

}
