package code;

import org.junit.Test;
import org.wxd.boot.core.format.UniqueID;
import org.wxd.boot.core.str.json.FastJsonUtil;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * id生成测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-11-23 17:18
 **/
public class UidTest {

    @Test
    public void t1() {

        UniqueID.Uid uid = new UniqueID.Uid(new AtomicLong(2));
        String json = uid.toJson();
        System.out.println(json);
        UniqueID.Uid parse = FastJsonUtil.parse(json, UniqueID.Uid.class);
        System.out.println(parse.toJson());

        HashSet<Long> longs = new HashSet<>();
        UniqueID.TimeUid idFormat = new UniqueID.TimeUid();

        while (longs.add(idFormat.next(1))) {}
        System.out.println(longs.size());
    }

    @Test
    public void sid() {

        /* 公式 (long)type << (63-5) | (long)sid >> (63-5-16) | uid */

        long smx = 0X3FFFC0000000000L;
        long uidmax = 0X3FFFFFFFFFFL;
        long type = 30;
        long sid = 10001;
        /*得到头部*/
        long hid = type << (63 - 5);
        System.out.println("头部：" + hid);
        /*融合sid*/
        hid = hid | (sid << (63 - 5 - 16));
        System.out.println("头部：" + hid);

        /*融合sid*/
        hid = hid | 1/*自增id*/;
        System.out.println("自增id：" + hid);

        /*还原type*/
        long be_type = hid >> (63 - 5);
        System.out.println(be_type);

        /*还原sid*/
        long be_sid = (hid & smx);
        System.out.println(be_sid >> (63 - 5 - 16));

        /*还原自增id*/
        long be_uid = (hid & uidmax);
        System.out.println(be_uid);
    }

}
