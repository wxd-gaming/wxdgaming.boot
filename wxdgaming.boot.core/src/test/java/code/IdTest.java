package code;

import org.junit.Test;
import wxdgaming.boot.core.format.UniqueID;
import wxdgaming.boot.core.str.json.FastJsonUtil;

public class IdTest {

    @Test
    public void t2() {

        UniqueID.Uid uid = new UniqueID.Uid(1);
        String json = FastJsonUtil.toJsonWriteType(uid);
        System.out.println(json);
        UniqueID.Uid parse = FastJsonUtil.parse(json, UniqueID.Uid.class);
        System.out.println(parse.getOrigin().get());
    }

}
