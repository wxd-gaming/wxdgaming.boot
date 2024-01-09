package org.wxd.boot.agent.lang;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-11-18 13:46
 **/
@Getter
@Setter
@Accessors(chain = true)
public class Tuple2<L, R> {

    protected L left;
    protected R right;

    public Tuple2() {
    }

    public Tuple2(L left, R right) {
        this.left = left;
        this.right = right;
    }
}
