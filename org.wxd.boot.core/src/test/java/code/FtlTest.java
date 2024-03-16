package code;

import org.junit.Test;
import org.wxd.boot.core.str.TemplatePack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ftl 模版测试
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-03-16 11:41
 **/
public class FtlTest {

    @Test
    public void ftl() {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", "sss");
        map.put("columns", List.of("1", 2, 4, "5"));

        TemplatePack template = TemplatePack.build(this.getClass().getClassLoader(), "template/sql");
        System.out.println(template.ftl2String("replace.ftl", map));

        System.out.println(TemplatePack.ftl2String(this.getClass().getClassLoader(), "template/sql", "replace.ftl", map));
    }

}
