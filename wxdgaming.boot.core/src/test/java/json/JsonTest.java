package json;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

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

}
