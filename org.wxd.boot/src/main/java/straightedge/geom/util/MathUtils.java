package straightedge.geom.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * author 失足程序员<br>
 * mail 492794628@qq.com<br>
 * phone 13882122019<br>
 */
public final class MathUtils {

    static public final float nanoToSec = 1 / 1000000000f;

    // ---
    static public final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits
    static public final int FLOAT_ROUND = 100000; // 32 bits
    static public final float PI = 3.1415927f;
    static public final float PI2 = PI * 2;

    static public final float E = 2.7182818f;

    static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
    static private final int SIN_MASK = ~(-1 << SIN_BITS);
    static private final int SIN_COUNT = SIN_MASK + 1;

    static private final float radFull = PI * 2;
    static private final float degFull = 360;
    static private final float radToIndex = SIN_COUNT / radFull;
    static private final float degToIndex = SIN_COUNT / degFull;

    /**
     * multiply by this to convert from radians to degrees
     */
    static public final float radiansToDegrees = 180f / PI;
    static public final float radDeg = radiansToDegrees;
    /**
     * multiply by this to convert from degrees to radians
     */
    static public final float degreesToRadians = PI / 180;
    static public final float degRad = degreesToRadians;

    public static final String[] CHARS = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
            "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    static private class Sin {

        static final float[] table = new float[SIN_COUNT];

        static {
            for (int i = 0; i < SIN_COUNT; i++) {
                table[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            }
            for (int i = 0; i < 360; i += 90) {
                table[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * degreesToRadians);
            }
        }
    }

    /**
     * Returns the sine in radians from a lookup table.
     *
     * @param radians
     * @return
     */
    static public float sin(double radians) {
        return Sin.table[(int) (radians * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     *
     * @return
     */
    static public float cos(double radians) {
        return Sin.table[(int) ((radians + PI / 2) * radToIndex) & SIN_MASK];
    }

    static public float roundFloat(float value) {
        return (Math.round(value * FLOAT_ROUND)) / FLOAT_ROUND;
    }

    /**
     * Returns the sine in radians from a lookup table.
     *
     * @return
     */
    static public float sinDeg(float degrees) {
        return Sin.table[(int) (degrees * degToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     *
     * @return
     */
    static public float cosDeg(float degrees) {
        return Sin.table[(int) ((degrees + 90) * degToIndex) & SIN_MASK];
    }

    // ---

    /**
     * Returns atan2 in radians, faster but less accurate than Math.atan2.
     * Average error of 0.00231 radians (0.1323 degrees), largest error of
     * 0.00488 radians (0.2796 degrees).
     *
     * @return
     */
    static public float atan2(float y, float x) {
        if (x == 0f) {
            if (y > 0f) {
                return PI / 2;
            }
            if (y == 0f) {
                return 0f;
            }
            return -PI / 2;
        }
        final float atan, z = y / x;
        if (Math.abs(z) < 1f) {
            atan = z / (1f + 0.28f * z * z);
            if (x < 0f) {
                return atan + (y < 0f ? -PI : PI);
            }
            return atan;
        }
        atan = PI / 2 - z / (z * z + 0.28f);
        return y < 0f ? atan - PI : atan;
    }

    public static int floatToRawIntBits(float value) {
        return Float.floatToRawIntBits(value);
    }

    public static int floatToIntBits(float value) {
        return Float.floatToIntBits(value);
    }

    public static String getShortUUID() {
        StringBuilder shortBuffer = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(CHARS[x % CHARS.length]);
        }
        return shortBuffer.toString();
    }

    /**
     * 从集合中随机一个元素
     *
     * @param <T>
     * @param collection
     * @return
     */
    public static <T> T random(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int t = (int) (collection.size() * Math.random());
        int i = 0;
        for (Iterator<T> item = collection.iterator(); i <= t && item.hasNext(); ) {
            T next = item.next();
            if (i == t) {
                return next;
            }
            i++;
        }
        return null;
    }

    public static Integer max(Collection<Integer> collection) {
        if (collection == null) {
            return 0;
        }
        Integer i = 0;
        for (Integer in : collection) {
            i = i < in ? in : i;
        }
        return i;
    }

    /**
     * 从集合中随机移除一个元素
     *
     * @param <T>
     * @param collection
     * @return
     */
    public static <T> T randomRemove(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int t = (int) (collection.size() * Math.random());
        int i = 0;
        for (Iterator<T> item = collection.iterator(); i <= t && item.hasNext(); ) {
            T next = item.next();
            if (i == t) {
                item.remove();
                return next;
            }
            i++;
        }
        return null;
    }

    // ---

    /**
     * Returns the next power of two. Returns the specified value if the value
     * is already a power of two.
     *
     * @return
     */
    static public int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    static public boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    // ---
    static public short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public long clamp(long value, long min, long max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    // ---

    /**
     * Linearly interpolates between fromValue to toValue on progress position.
     *
     * @return
     */
    static public float lerp(float fromValue, float toValue, float progress) {
        return fromValue + (toValue - fromValue) * progress;
    }

    /**
     * Linearly interpolates between two angles in radians. Takes into account
     * that angles wrap at two pi and always takes the direction with the
     * smallest delta angle.
     *
     * @param fromRadians start angle in radians
     * @param toRadians   target angle in radians
     * @param progress    interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, PI2[
     */
    public static float lerpAngle(float fromRadians, float toRadians, float progress) {
        float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
        return (fromRadians + delta * progress + PI2) % PI2;
    }

    /**
     * Linearly interpolates between two angles in degrees. Takes into account
     * that angles wrap at 360 degrees and always takes the direction with the
     * smallest delta angle.
     *
     * @param fromDegrees start angle in degrees
     * @param toDegrees   target angle in degrees
     * @param progress    interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, 360[
     */
    public static float lerpAngleDeg(float fromDegrees, float toDegrees, float progress) {
        float delta = ((toDegrees - fromDegrees + 360 + 180) % 360) - 180;
        return (fromDegrees + delta * progress + 360) % 360;
    }

    // ---
    static private final int BIG_ENOUGH_INT = 16 * 1024;
    static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    static private final double CEIL = 0.9999999;
    static private final double BIG_ENOUGH_CEIL = 16384.999999999996;
    static private final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

    static public int floor(float value) {
        return (int) (value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    static public int floorPositive(float value) {
        return (int) value;
    }

    static public int ceil(float value) {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - value);
    }

    static public int ceilPositive(float value) {
        return (int) (value + CEIL);
    }

    static public int round(float value) {
        return (int) (value + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    static public int roundPositive(float value) {
        return (int) (value + 0.5f);
    }

    static public boolean isZero(float value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    static public boolean isZero(float value, float tolerance) {
        return Math.abs(value) <= tolerance;
    }

    static public boolean isEqual(float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }

    static public boolean isEqual(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    static public float log(float a, float value) {
        return (float) (Math.log(value) / Math.log(a));
    }

    static public float log2(float value) {
        return log(2, value);
    }
}
