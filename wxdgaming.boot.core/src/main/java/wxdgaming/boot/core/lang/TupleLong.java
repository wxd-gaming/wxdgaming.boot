package wxdgaming.boot.core.lang;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-11-18 13:46
 **/
@Getter
@Setter
@Accessors(chain = true)
public class TupleLong extends ObjectBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        TupleLong tuple2 = new TupleLong(1, 1);
        String s = FastJsonUtil.toJsonFmt(tuple2);
        System.out.println(s);
        TupleLong object = FastJsonUtil.parse(s, TupleLong.class);
        System.out.println(object);
    }

    protected long left;
    protected long right;

    public TupleLong() {
    }

    public TupleLong(long left, long right) {
        this.left = left;
        this.right = right;
    }
}
