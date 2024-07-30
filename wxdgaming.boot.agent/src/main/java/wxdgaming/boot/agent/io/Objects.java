package wxdgaming.boot.agent.io;


import wxdgaming.boot.agent.exception.Throw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

/**
 * 数据对象合并
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-07-29 10:33
 */
public class Objects {

    public static boolean compare(Object o1, Object o2) {
        long hashCode1 = getHashCode(o1);
        long hashCode2 = getHashCode(o2);

        return hashCode1 == hashCode2;
    }

    public static long getHashCode(Object object) {
        long hashcode = 0;
        try {
            Class clazz = object.getClass();// 根据类名获得其对应的Class对象 写上你想要的类名就是了 注意是全名 如果有包的话要加上 比如java.Lang.String
            Field[] fields = clazz.getDeclaredFields();// 根据Class对象获得属性 私有的也可以获得
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isTransient(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())) {
//                    System.out.println(" 类：" + clazz.getName() + " 字段：" + field.getName() + " is transient or static or final;");
                    continue;
                }
                Object get = field.get(object);
                if (get != null) {
                    hashcode += java.util.Objects.hashCode(get);
                }
//                System.out.println(field.getName() + " " + field.getType().getName() + "," + get);//打印每个属性的类型名字
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return hashcode;
    }

    public static byte[] toBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(obj);
                oos.flush();
                return baos.toByteArray();
            }
        } catch (Throwable ex) {
            throw Throw.as("对象转 byte[] 出现错误", ex);
        }
    }

    /**
     * @param buf
     * @return
     */
    public static <R> R toObject(byte[] buf) {
        if (buf == null || buf.length < 1) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buf)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (R) ois.readObject();
            }
        } catch (Throwable ex) {
            throw Throw.as("byte[] 转 对象 出现错误", ex);
        }
    }

    /**
     * 深拷贝 效率,高于Fastjson以及BeanUtils
     *
     * @param <R>
     * @param obj
     * @return
     */
    public static <R> R deepCopy(Object obj) {
        return (R) toObject(toBytes(obj));
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }


    /**
     * @param str 格式 1:1:1,2:2:2
     */
    public static int[][] asInts2(String str) {
        return asSplit2(str, ",|，", ":|：");
    }

    /**
     * @param str 格式 1（splitRegex2）1（splitRegex2）1（splitRegex1）2（splitRegex2）2（splitRegex2）2
     */
    public static int[][] asSplit2(String str, String splitRegex1, String splitRegex2) {
        String[] split = str.split(splitRegex1);
        int[][] ints2 = new int[split.length][3];
        for (int i = 0; i < split.length; i++) {
            int[] ints = new int[3];
            String[] split1 = split[i].split(splitRegex2);
            ints[0] = Integer.parseInt(split1[0]);
            ints[1] = Integer.parseInt(split1[1]);
            ints[2] = Integer.parseInt(split1[2]);
            ints2[i] = ints;
        }
        return ints2;
    }

    /**
     * 合并value
     * <p>如果存在key，就map.get(key) + value 存入 map中
     * <p>如果不存在key，value 存入 map中
     */
    public static int mergeValue(Map<Integer, Integer> map, int key, int mergeValue) {
        return map.merge(key, mergeValue, Integer::sum);
    }

    /**
     * 合并value
     * <p>如果存在key，就map.get(key) + value 存入 map中
     * <p>如果不存在key，value 存入 map中
     */
    public static long mergeValue(Map<Integer, Long> map, int key, long mergeValue) {
        return map.merge(key, mergeValue, Long::sum);
    }


    /** 数组追加合并 */
    public static <T> T[] merge(T[] ts, T... ats) {
        T[] ts1 = Arrays.copyOf(ts, ts.length + ats.length);
        for (int i = ts.length; i < ts1.length; i++) {
            ts1[i] = ats[i - ts.length];
        }
        return ts1;
    }

    /**
     * 合并数组
     *
     * @param <T>
     * @param first
     * @param rest
     * @param length
     * @return
     */
    public static <T> T[] merge(T[] first, T[] rest, int length) {
        int totalLength = first.length + length;
        final T[] result = (T[]) java.lang.reflect.Array.newInstance(first.getClass().getComponentType(), totalLength);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(rest, 0, result, first.length, length);
        return result;
    }

    /**
     * 合并数组
     *
     * @param first
     * @param rest
     * @param length
     * @return
     */
    public static byte[] mergeBytes(byte[] first, byte[] rest, int length) {
        int totalLength = first.length + length;
        byte[] result = new byte[totalLength];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(rest, 0, result, first.length, length);
        return result;
    }

    /** 数组追加合并 */
    public static int[] merge(int[] ts, int... ats) {
        int[] ts1 = Arrays.copyOf(ts, ts.length + ats.length);
        for (int i = ts.length; i < ts1.length; i++) {
            ts1[i] = ats[i - ts.length];
        }
        return ts1;
    }

    /** 数组追加合并 */
    public static long[] merge(long[] ts, long... ats) {
        long[] ts1 = Arrays.copyOf(ts, ts.length + ats.length);
        for (int i = ts.length; i < ts1.length; i++) {
            ts1[i] = ats[i - ts.length];
        }
        return ts1;
    }
}
