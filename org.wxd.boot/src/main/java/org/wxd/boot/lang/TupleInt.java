package org.wxd.boot.lang;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.str.json.FastJsonUtil;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-11-18 13:46
 **/
@Getter
@Setter
@Accessors(chain = true)
public class TupleInt extends ObjectBase implements Serializable {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        TupleInt tuple2 = new TupleInt(1, 1);
        String s = FastJsonUtil.toJsonFmt(tuple2);
        System.out.println(s);
        TupleInt object = FastJsonUtil.parse(s, TupleInt.class);
        System.out.println(object);
    }

    protected int left;
    protected int right;

    public TupleInt() {
    }

    public TupleInt(int left, int right) {
        this.left = left;
        this.right = right;
    }
}
