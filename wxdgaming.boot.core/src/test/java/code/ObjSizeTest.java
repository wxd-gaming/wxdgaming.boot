package code;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.LongObjectHashMap;
import org.junit.Test;
import wxdgaming.boot.core.format.ByteFormat;
import wxdgaming.boot.core.format.data.Data2Size;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-03-31 12:29
 **/
public class ObjSizeTest {

    @Test
    public void t0() {
        short[][] sh1 = new short[8][4];
        byte[][] by1 = new byte[8][4];

        System.out.println(Data2Size.totalSizes0(sh1));
        System.out.println(Data2Size.totalSizes0(by1));
    }

    @Test
    public void of() throws Exception {
        Constructor<HashMap> constructor = HashMap.class.getConstructor();
        HashMap hashMap = constructor.newInstance();
        HashMap hashMap1 = constructor.newInstance();
        System.out.println(hashMap.hashCode() + " - " + hashMap1.hashCode());
    }

    @Test
    public void t5() throws Exception {
        Jdk jdk = new Jdk();
        Hppc hppc = new Hppc();

        for (int i = 0; i < 10000; i++) {
            long l = i * 10;
            {
                jdk.longObjectHashMap.put(l, String.valueOf(i));
                jdk.long2.put(l, l);
                jdk.int2.put(i, i);
                jdk.int1.add(i);
            }
            {
                hppc.longObjectHashMap.put(l, String.valueOf(i));
                hppc.long2.put(l, l);
                hppc.int2.put(i, i);
                hppc.int1.add(i);
            }

        }

        System.out.println("jdk: " + Data2Size.totalSizes0(jdk));
        System.out.println("hppc: " + Data2Size.totalSizes0(hppc));
        String json = FastJsonUtil.toJson(hppc.longObjectHashMap);
        LongObjectHashMap object = FastJsonUtil.parse(json, LongObjectHashMap.class);
        System.out.println(object.getClass());

    }

    public class Jdk {
        HashMap<Long, Object> longObjectHashMap = new HashMap<>();
        HashMap<Long, Long> long2 = new HashMap<>();
        HashMap<Integer, Integer> int2 = new HashMap<>();
        ArrayList<Integer> int1 = new ArrayList<>();
    }

    public class Hppc {
        LongObjectHashMap longObjectHashMap = new LongObjectHashMap(1003);
        LongLongHashMap long2 = new LongLongHashMap(1003);
        IntIntHashMap int2 = new IntIntHashMap(1003);
        IntArrayList int1 = new IntArrayList();
    }


    @Test
    public void t33() {
        List<Integer> sh0 = new ArrayList<>();
        List<Integer> sh1 = new ArrayList<>(2);
        sh0.add(1);
        // sh0.add(1);
        // sh0.add(1);
        // sh1.add(1);
        // sh1.add(1);
        sh1.add(1);
        System.out.println(Data2Size.totalSize0(sh0));
        System.out.println(Data2Size.totalSize0(sh1));
    }

    @Test
    public void t34() {
        System.out.println(Data2Size.totalSize0(new ArrayList<String>()));
        System.out.println(Data2Size.totalSize0(new ArrayList<String>(2)));
        System.out.println(Data2Size.totalSize0(new ArrayList<String>(8)));
        System.out.println(Data2Size.totalSize0(new ArrayList<String>(200)));
    }

    @Test
    public void t35() {
        A[] as = new A[1000 * 10000];
        B[] bs = new B[1000 * 10000];

        System.out.println(Data2Size.totalSizes0(as));
        System.out.println(Data2Size.totalSizes0(bs));
        for (int i = 0; i < as.length; i++) {
            as[i] = new A();
            bs[i] = new B();
        }
        System.out.println(new ByteFormat().addFlow(Data2Size.totalSize0(as)).toString(ByteFormat.FormatInfo.MB));
        System.out.println(new ByteFormat().addFlow(Data2Size.totalSize0(bs)).toString(ByteFormat.FormatInfo.MB));
    }

    public class A {
        int a = 2;
        int b = 2;
    }

    public class B {
        short a = 2;
        short b = 2;
    }

}
