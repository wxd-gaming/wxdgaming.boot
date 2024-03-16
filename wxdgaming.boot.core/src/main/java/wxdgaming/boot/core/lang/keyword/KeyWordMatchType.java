package wxdgaming.boot.core.lang.keyword;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-07 18:20
 **/
public enum KeyWordMatchType {
    /**
     * 最小匹配规则，敏感字中长度最“小”的匹配替换
     */
    MIN(1),
    /**
     * 最大匹配规则,敏感字中长度最“大” 的匹配替换
     */
    MAX(2);

    final int mtype;

    KeyWordMatchType(int mtype) {
        this.mtype = mtype;
    }

    public int getMtype() {
        return mtype;
    }
}
