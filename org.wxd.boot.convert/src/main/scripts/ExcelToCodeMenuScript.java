import org.wxd.boot.batis.code.CodeLan;
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
public class ExcelToCodeMenuScript implements Serializable, AddMenu {

    @Override
    public int index() {
        return 11;
    }

    @Override
    public void addMenu(MainForm mainForm, JMenuBar menuBar) {
        JMenu excelMenu = new JMenu("Excel to Code");
        {
            JMenuItem javaCodeMenuItem = new JMenuItem("Java");
            javaCodeMenuItem.setToolTipText("输出 Excel 标记 Server 和 All 属性的字段");
            javaCodeMenuItem.addActionListener(
                    (e) ->
                            action(
                                    mainForm,
                                    CodeLan.Java,
                                    "target/out/server/java",
                                    "com.server.cfg",
                                    "server"
                            )
            );
            excelMenu.add(javaCodeMenuItem);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("C#");
            jsonMenu.setToolTipText("输出 Excel 标记 Client 和 All 属性的字段");
            jsonMenu.addActionListener(
                    (e) ->
                            action(
                                    mainForm,
                                    CodeLan.CSharp,
                                    "target/out/client/csharp",
                                    "Cfg",
                                    "client"
                            )
            );
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem javaCodeMenuItem = new JMenuItem("TypeScript");
            javaCodeMenuItem.setToolTipText("输出 Excel 标记 Client 和 All 属性的字段");
            javaCodeMenuItem.addActionListener(
                    (e) ->
                            action(
                                    mainForm,
                                    CodeLan.TypeScript,
                                    "target/out/client/ts",
                                    "com.server.cfg",
                                    "client"
                            )
            );
            excelMenu.add(javaCodeMenuItem);
        }
        excelMenu.addSeparator();
        {
            JMenuItem javaCodeMenuItem = new JMenuItem("Lua");
            javaCodeMenuItem.setToolTipText("输出 Excel 标记 Client 和 All 属性的字段");
            javaCodeMenuItem.addActionListener(
                    (e) ->
                            action(
                                    mainForm,
                                    CodeLan.Lua,
                                    "target/out/client/lua",
                                    "com.server.cfg",
                                    "client"
                            )
            );
            excelMenu.add(javaCodeMenuItem);
        }
        menuBar.add(excelMenu);
    }

    public void action(MainForm mainForm, CodeLan codeLan, String savePath, String packageName, String... haveExtends) {
        mainForm.event(() -> {
            ExcelRead2Json excelRead2Json = mainForm.excelRead2Json(haveExtends);
            if (excelRead2Json == null) return;

            final Map<String, JsonEntityTable> excelDataTableMap = excelRead2Json.getExcelDataTableMap();

            for (Map.Entry<String, JsonEntityTable> entry : excelDataTableMap.entrySet()) {
                excelRead2Json.createCode(codeLan, savePath, packageName, entry.getValue());
                mainForm.addLog("Create " + codeLan.getComment() + " Code：" + savePath + "/" + entry.getKey());
            }
            mainForm.showBox("转化 " + codeLan.getComment() + " Code 处理完成");
        });
    }

}
