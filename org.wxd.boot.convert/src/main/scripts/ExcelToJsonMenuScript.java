import org.wxd.boot.batis.excel.ExcelRead2Json;
import org.wxd.boot.batis.text.json.JsonEntityTable;
import org.wxd.boot.convert.AddMenu;
import org.wxd.boot.convert.MainForm;

import javax.swing.*;
import java.io.Serializable;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-19 13:54
 **/
public class ExcelToJsonMenuScript implements Serializable, AddMenu {

    @Override
    public int index() {
        return 10;
    }

    @Override
    public void addMenu(MainForm mainForm, JMenuBar menuBar) {
        JMenu excelMenu = new JMenu("Excel to Json");
        {
            JMenuItem jsonMenu = new JMenuItem("All");
            jsonMenu.setToolTipText("输出 excel 所有属性的字段");
            jsonMenu.addActionListener((e) -> action(mainForm, "target/out/json/config_json"));
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("Server");
            jsonMenu.setToolTipText("输出 Excel 标记 Server 和 All 属性的字段");
            jsonMenu.addActionListener((e) -> action(mainForm, "target/out/server/config_json", "server"));
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("Client");
            jsonMenu.setToolTipText("输出 Excel 标记 Client 和 All 属性的字段");
            jsonMenu.addActionListener((e) -> action(mainForm, "target/out/client/config_json", "client"));
            excelMenu.add(jsonMenu);
        }
        menuBar.add(excelMenu);
    }

    public void action(MainForm mainForm, String out_path, String... haveExtends) {
        mainForm.event(() -> {
            ExcelRead2Json excelRead2Json = mainForm.excelRead2Json(haveExtends);
            if (excelRead2Json == null) return;

            excelRead2Json.setSaveJsonPath(out_path);

            final Map<String, JsonEntityTable> excelDataTableMap = excelRead2Json.getExcelDataTableMap();

            for (Map.Entry<String, JsonEntityTable> entry : excelDataTableMap.entrySet()) {
                excelRead2Json.saveData(entry.getValue());
                mainForm.addLog("Create Json：" + excelRead2Json.getSaveJsonPath() + "/" + entry.getKey() + ".json");
            }

            mainForm.showBox("转化 " + String.join(", ", haveExtends) + " Json 处理完成");
        });
    }

}
