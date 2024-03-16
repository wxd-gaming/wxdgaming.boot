package wxdgaming.boot.core.lang;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-11-18 13:46
 **/
@Getter
@Setter
@Accessors(chain = true)
public class Tuple3<L, C, R> extends Tuple2<L, R> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected C center;

    public Tuple3() {
    }

    public Tuple3(L left, C center, R right) {
        super(left, right);
        this.center = center;
    }

    @Override
    public Tuple3<L, C, R> setRight(R right) {
        super.setRight(right);
        return this;
    }

    @Override
    public Tuple3<L, C, R> setLeft(L left) {
        super.setLeft(left);
        return this;
    }

}
