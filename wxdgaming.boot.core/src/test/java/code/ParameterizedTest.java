package code;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型类的泛型字段
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-12 19:16
 **/
public class ParameterizedTest {

    @Setter
    @Getter
    public static class A<ID> {

        private ID id;

    }

    @Setter
    @Getter
    public static class B extends A<Long> {

    }

    @Test
    public void findType() throws Exception {
        // 获取 B 类的 Class 对象
        Class<B> bClass = B.class;

        // 获取父类 A 中的 id 字段
        Field idField = bClass.getSuperclass().getDeclaredField("id");

        // 获取字段的泛型类型
        Type genericType = idField.getGenericType();

        // 获取字段的实际类型参数
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type type : actualTypeArguments) {
                System.out.println("The actual type of id field is: " + type.getTypeName());
            }
        } else {
            System.out.println("The type of id field is: " + idField.getType().getName());
        }
    }

}
