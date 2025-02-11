package json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import org.junit.Test;
import wxdgaming.boot.agent.io.Objects;
import wxdgaming.boot.core.collection.concurrent.ConcurrentHashSet;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * json测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-07-12 09:31
 **/
public class JsonTest {

    @Data
    public static class A implements Serializable {

        private static final long serialVersionUID = 1L;
        private int a = 1;
        private String b = "s";
        private Map<String, String> m = Map.of("s", "s");
    }

    @Test
    public void test() {
        A a = new A();
        Object json = JSON.toJSON(a);
        System.out.println(json);
    }

    @Test
    public void hashset() {
        {
            List<A> set = new ArrayList<>();
            set.add(new A());
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
        System.out.println("");
        {
            Set<A> set = new HashSet<>();
            set.add(new A());
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
        System.out.println("");
        {
            Set<A> set = new ConcurrentHashSet<>();
            set.add(new A());
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
        System.out.println("");
        {
            Map<Integer, A> set = new HashMap<>();
            set.put(1, new A());
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
        System.out.println("");
        {
            Map<Integer, A> set = new ConcurrentHashMap<>();
            set.put(1, new A());
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
        System.out.println("");
        {
            A set = new A();
            System.out.println(JSON.toJSONString(set, FastJsonUtil.Writer_Features_Type_Name));
            System.out.println(JSON.toJSONString(set, Objects.merge(FastJsonUtil.Writer_Features_Type_Name, SerializerFeature.NotWriteRootClassName)));
        }
    }

}
