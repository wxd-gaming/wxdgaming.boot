package code;

import org.junit.Test;
import org.wxd.boot.batis.code.CodeLan;
import org.wxd.boot.batis.excel.ExcelRead2Json;

/**
 * excel 转化 json 文件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-02-29 17:11
 **/
public class Excel2JsonTest {

    @Test
    public void t1() {
        ExcelRead2Json.builder()
                .dataIndex(2, 3, 4, 1, 5)
                .excelPaths(false, "src/test/resources")
                .loadExcel("server")
                .saveData("target/out/server/config_json")
//                .showData()
                .createCode(CodeLan.Java, "target/out/server/code", "com.server.cfg")
                .loadExcel("client")
                .saveData("target/out/client/config_json")
                .createCode(CodeLan.CSharp, "target/out/client/code", "Cfg")
        ;
    }

}
