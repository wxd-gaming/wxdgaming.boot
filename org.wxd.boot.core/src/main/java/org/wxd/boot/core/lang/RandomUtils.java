package org.wxd.boot.core.lang;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class RandomUtils {

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println(randomItem(list));
        System.out.println(randomRemove(list));

        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        System.out.println(randomItem(set));
        System.out.println(randomRemove(set));
        System.exit(0);
    }

    public static float next(Float min, Float max) {
        return next(null, min, max);
    }

    /**
     * 自定义随机算法
     *
     * @param seed
     * @param min
     * @param max
     * @return
     */
    public static float next(Long seed, Float min, Float max) {
        if (seed == null) {
            seed = System.currentTimeMillis() % 999999999L;
        }
        seed = (seed * 9301 + 49297) % 233280L;
        if (min == null) {
            min = 0F;
        }
        if (max == null) {
            max = 10F;
        }
        float val = seed / 233280.0f;
        return (float) (min + Math.floor(val * max));
    }

    /**
     * 返回 0  ~  1 之间的数字
     */
    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    /**
     * 返回 0  ~  1 之间的数字
     */
    public static double randomDoubleValue() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * 随机产生min到max之间的整数值 包括min max
     *
     * @param min
     * @param max
     * @return
     */
    public static float randomFloatValue(float min, float max) {
        return (float) (ThreadLocalRandom.current().nextDouble() * (max - min)) + min;
    }

    /**
     * 随机产生min到max之间的整数值 包括min max
     *
     * @param min
     * @param max
     * @return
     */
    public static double randomDoubleValue(double min, double max) {
        return (ThreadLocalRandom.current().nextDouble() * (max - min)) + min;
    }

    /**
     * 返回 0 - (max-1)
     *
     * @param max 如果10 返回 0-9
     * @return
     */
    public static int random(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    /**
     * 从 min 和 max 中间随机一个值
     *
     * @param min
     * @param max
     * @return 包含min max
     */
    public static int random(int min, int max) {
        if (max - min <= 0) {
            return min;
        }
        return min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

    /**
     * 从 min 和 max 中间随机一个值
     *
     * @param min
     * @param max
     * @return 包含min max
     */
    public static int random(float min, float max) {
        if (max - min <= 0) {
            return (int) min;
        }
        return (int) (min + ThreadLocalRandom.current().nextInt((int) (max - min + 1)));
    }

    /**
     * 根据几率 计算是否生成
     *
     * @param probability
     * @param gailv
     * @return
     */
    public static boolean isGenerate(int probability, int gailv) {
        if (gailv == 0) {
            gailv = 1000;
        }
        int random_seed = ThreadLocalRandom.current().nextInt(gailv + 1);
        return probability >= random_seed;
    }

    /**
     * gailv/probability 比率形式
     *
     * @param probability 通过这个数取一个随机数
     * @param gailv       对比随机数和他的大小
     * @return
     */
    public static boolean isGenerate2(int probability, int gailv) {
        if (probability == gailv) {
            return true;
        }
        if (gailv == 0) {
            return false;
        }
        int random_seed = ThreadLocalRandom.current().nextInt(probability);
        return random_seed + 1 <= gailv;
    }

    /**
     * 返回在0-maxcout之间产生的随机数时候小于num
     *
     * @param num
     * @param maxcout
     * @return
     */
    public static boolean isGenerateToBoolean(float num, int maxcout) {
        return Math.random() * maxcout < num;
    }

    /**
     * 返回在0-maxcout之间产生的随机数时候小于num
     *
     * @param num
     * @param maxcout
     * @return
     */
    public static boolean isGenerateToBoolean(int num, int maxcout) {
        return Math.random() * maxcout < num;
    }

    /** 从数组中随机一个元素 */
    public static int random(int[] args) {
        if (args == null || args.length < 1) {
            return -1;
        }
        int index = random(args.length);
        return args[index];
    }

    /** 从数组中随机一个元素 */
    public static long random(long[] args) {
        if (args == null || args.length < 1) {
            return -1;
        }
        int index = random(args.length);
        return args[index];
    }

    /**
     * 从数组中随机一个元素
     *
     * @param <T>
     * @param args
     * @return
     */
    public static <T> T random(T[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        int index = random(args.length);
        return args[index];
    }

    /**
     * 最大值
     *
     * @param collection
     * @return
     */
    public static Integer max(Collection<Integer> collection) {
        if (collection == null) {
            return 0;
        }
        Integer i = Integer.MIN_VALUE;
        for (Integer in : collection) {
            i = i < in ? in : i;
        }
        return i;
    }

    /**
     * 最小值
     *
     * @param collection
     * @return
     */
    public static Integer min(Collection<Integer> collection) {
        if (collection == null) {
            return 0;
        }
        Integer i = Integer.MAX_VALUE;
        for (Integer in : collection) {
            i = i > in ? in : i;
        }
        return i;
    }

    /**
     * 随机获取一个数据
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> T randomItem(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = random(collection.size());
        if (collection instanceof List) {
            List list = (List) collection;
            return (T) list.get(index);
        } else {
            int i = 0;
            for (Iterator<T> item = collection.iterator(); i <= index && item.hasNext(); ) {
                T next = item.next();
                if (i == index) {
                    return next;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * 随机移除一个对象，并且返回结果
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> T randomRemove(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = random(collection.size());
        if (collection instanceof List) {
            List list = (List) collection;
            return (T) list.remove(index);
        } else {
            int i = 0;
            for (Iterator<T> item = collection.iterator(); i <= index && item.hasNext(); ) {
                T next = item.next();
                if (i == index) {
                    item.remove();
                    return next;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * Returns a random boolean value.
     *
     * @return
     */
    static public boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * 根据几率 计算是否生成，种子数为10000
     *
     * @param probability 概率 用 10000 种子 取随机数
     * @return
     */
    public static boolean randomBoolean(int probability) {
        return randomBoolean(probability, 10000);
    }

    /**
     * 根据几率 计算是否生成
     *
     * @param probability 概率
     * @param bound       基数
     * @return
     */
    public static boolean randomBoolean(int probability, int bound) {
        if (probability == 0) {
            return false;
        }
        if (bound == 0) {
            bound = 10000;
        }
        int random_seed = ThreadLocalRandom.current().nextInt(bound + 1);
        return probability >= random_seed;
    }


}
