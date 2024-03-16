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
public class Tuple4<E1, E2, E3, E4> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected E1 e1;
    protected E2 e2;
    protected E3 e3;
    protected E4 e4;

    public Tuple4() {
    }

    public Tuple4(E1 e1, E2 e2, E3 e3, E4 e4) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
        this.e4 = e4;
    }
}
