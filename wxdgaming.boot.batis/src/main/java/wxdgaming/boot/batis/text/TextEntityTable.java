package wxdgaming.boot.batis.text;

import wxdgaming.boot.batis.EntityTable;

import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-19 15:34
 **/
public abstract class TextEntityTable extends EntityTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编码
     *
     * @return
     */
    public abstract String encoded(Object dbModel) throws Exception;

    /**
     * 解码
     *
     * @param text
     * @return
     */
    public abstract Object decoded(String text) throws Exception;

}
