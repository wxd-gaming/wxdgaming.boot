package wxdgaming.boot.core.str;


import wxdgaming.boot.core.str.json.FastJsonUtil;

/**
 * 计算字符串的hashcode值
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-06-02 11:29
 **/
public interface StringHashCode {

    /**
     * 把当前对象转换成json字符串，在计算hashcode
     *
     * @return
     */
    default int hashcode() {
        return hashcode(FastJsonUtil.toJson(this));
    }

    /**
     * 计算字符串的hashcode值
     */
    default int hashcode(String source) {
        return StringUtils.hashcode(source);
    }

}
