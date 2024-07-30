package aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.junit.Test;

import java.util.*;

/**
 * 规则模板
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-05-09 21:05
 **/
public class Code {

    @Test
    public void t0() {
        // exec执行方式，无需传递Map格式
        String age = "18";
        System.out.println(AviatorEvaluator.exec("'His age is '+ age +'!'", age));

        // execute执行方式，需传递Map格式
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("age", "18");
        System.out.println(AviatorEvaluator.execute("'His age is '+ age +'!'", map));

    }

    @Test
    public void t1() {
        Map<String, Object> map = new HashMap<>();
        map.put("s1", "123qwer");
        map.put("s2", "123");

        System.out.println(AviatorEvaluator.execute("string.startsWith(s1,s2)", map));

    }

    @Test
    public void t2() {
        // 注册自定义函数
        AviatorEvaluator.addFunction(new MultiplyFunction());
        // 方式1
        System.out.println(AviatorEvaluator.execute("multiply(12.23, 2.3,3)"));
        // 方式2
        Map<String, Object> params = new HashMap<>();
        params.put("a", 12.23);
        params.put("b", 2.3);
        System.out.println(AviatorEvaluator.execute("multiply(a, b)", params));
    }


    static class MultiplyFunction extends AbstractFunction {

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {

            double num1 = FunctionUtils.getNumberValue(arg1, env).doubleValue();
            double num2 = FunctionUtils.getNumberValue(arg2, env).doubleValue();
            return new AviatorDouble(num1 * num2);
        }

        @Override
        public String getName() {
            return "multiply";
        }
    }

    @Test
    public void t4() {
        final List<String> list = new ArrayList<>();
        list.add("hello");
        list.add(" world");

        final int[] array = new int[3];
        array[0] = 0;
        array[1] = 1;
        array[2] = 3;

        final Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());

        Map<String, Object> env = new HashMap<>();
        env.put("list", list);
        env.put("array", array);
        env.put("map", map);

        System.out.println(AviatorEvaluator.execute(
                "list[0]+':'+array[0]+':'+'today is '+map.date", env));
    }

}
