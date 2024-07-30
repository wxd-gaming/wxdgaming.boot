package wxdgaming.boot.core.system;

/**
 * 字节格式化方案
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-11-05 10:34
 **/
public enum BytesUnit {
    B() {
        @Override public long bytes() {
            return 1;
        }

        /**传入kb*/
        @Override public long toBytes(long uint) {
            return uint;
        }

        /**传入kb*/
        @Override public long toKb(long uint) {
            return uint / 1024;
        }

        /**传入kb*/
        @Override public long toMb(long uint) {
            return uint / 1024 / 1024;
        }

        /**传入kb*/
        @Override public long toGb(long uint) {
            return uint / 1024 / 1024 / 1024;
        }

        /**传入kb*/
        @Override public float toKbf(long uint) {
            return uint * 100 / 1024 / 100f;
        }

        /**传入kb*/
        @Override public float toMbf(long uint) {
            return uint * 100 / 1024 / 1024 / 100f;
        }

        /**传入kb*/
        @Override public float toGbf(long uint) {
            return uint * 100 / 1024 / 1024 / 1024 / 100f;
        }
    },
    Kb() {
        @Override public long bytes() {
            return 1024;
        }

        /**传入kb*/
        @Override public long toBytes(long uint) {
            return uint * 1024;
        }

        /**传入kb*/
        @Override public long toKb(long uint) {
            return uint;
        }

        /**传入kb*/
        @Override public long toMb(long uint) {
            return uint / 1024;
        }

        /**传入kb*/
        @Override public long toGb(long uint) {
            return uint / 1024 / 1024;
        }

        /**传入kb*/
        @Override public float toKbf(long uint) {
            return uint;
        }

        /**传入kb*/
        @Override public float toMbf(long uint) {
            return uint * 100 / 1024 / 100f;
        }

        /**传入kb*/
        @Override public float toGbf(long uint) {
            return uint * 100 / 1024 / 1024 / 100f;
        }
    },

    Mb() {
        @Override public long bytes() {
            return 1024 * 1024;
        }

        /**传入 Mb */
        @Override public long toBytes(long uint) {
            return uint * 1024 * 1024;
        }

        /**传入 Mb */
        @Override public long toKb(long uint) {
            return uint * 1024;
        }

        /**传入kb*/
        @Override public long toMb(long uint) {
            return uint;
        }

        /**传入 Mb */
        @Override public long toGb(long uint) {
            return uint / 1024;
        }

        /**传入 Mb */
        @Override public float toKbf(long uint) {
            return uint * 1024;
        }

        /**传入 Mb */
        @Override public float toMbf(long uint) {
            return uint;
        }

        /**传入 Mb */
        @Override public float toGbf(long uint) {
            return uint * 100 / 1024 / 100f;
        }
    },

    Gb() {
        @Override public long bytes() {
            return 1024 * 1024 * 1024;
        }

        /**传入 Mb */
        @Override public long toBytes(long uint) {
            return uint * 1024 * 1024 * 1024;
        }

        /**传入 Mb */
        @Override public long toKb(long uint) {
            return uint * 1024 * 1024;
        }

        /**传入kb*/
        @Override public long toMb(long uint) {
            return uint * 1024;
        }

        /**传入 Mb */
        @Override public long toGb(long uint) {
            return uint;
        }

        /**传入 Mb */
        @Override public float toKbf(long uint) {
            return uint * 1024 * 1024;
        }

        /**传入 Mb */
        @Override public float toMbf(long uint) {
            return uint * 1024;
        }

        /**传入 Mb */
        @Override public float toGbf(long uint) {
            return uint;
        }
    },
    ;

    /** 返回常量 */
    public abstract long bytes();

    public abstract long toBytes(long uint);

    public abstract long toKb(long uint);

    public abstract long toMb(long uint);

    public abstract long toGb(long uint);

    public abstract float toKbf(long uint);

    public abstract float toMbf(long uint);

    public abstract float toGbf(long uint);
}
