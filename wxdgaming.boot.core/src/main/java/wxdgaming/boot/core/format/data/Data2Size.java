package wxdgaming.boot.core.format.data;

import org.openjdk.jol.info.GraphLayout;

/**
 * 数据大小
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-08-29 15:39
 **/
public interface Data2Size {

    default String totalSizes() {
        return totalSizes0(this);
    }

    /** 内存大小 */
    default long totalSize() {
        return totalSize0(this);
    }

    public static String totalSizes0(Object obj) {
        long totalSize = totalSize0(obj) * 100 / 1024;
        float k = totalSize / 100f;
        return k + " kb";
    }

    public static long totalSize0(Object obj) {
        GraphLayout graphLayout = GraphLayout.parseInstance(obj);
        long totalSize = graphLayout.totalSize();
        return totalSize;
    }
}
