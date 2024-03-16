
import wxdgaming.boot.convert.AddMenu;
import wxdgaming.boot.convert.MainForm;

import javax.swing.*;
import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-19 13:54
 **/
public class ProtobufWinOs64MenuScript implements Serializable, AddMenu {

    @Override
    public int index() {
        return 20;
    }

    @Override
    public void addMenu(MainForm mainForm, JMenuBar menuBar) {
        JMenu excelMenu = new JMenu("ProtoBuf Windows");
        {
            JMenuItem jsonMenu = new JMenuItem("Java");
            jsonMenu.addActionListener((e) -> {
                String out_path = "target/out/proto/java";
                mainForm.actionProtobuf("Java", out_path, "win64/protoc.exe", "java_out");
            });
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("C#");
            jsonMenu.addActionListener((e) -> {
                String out_path = "target/out/proto/csharp";
                mainForm.actionProtobuf("CSharp", out_path, "win64/protoc.exe", "csharp_out");
            });
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("TypeScript");
            jsonMenu.addActionListener((e) -> {
                String out_path = "target/out/proto/ts";
                mainForm.actionProtobuf("TypeScript", out_path, "win64/protoc.exe", "ts_out");
            });
            excelMenu.add(jsonMenu);
        }
        excelMenu.addSeparator();
        {
            JMenuItem jsonMenu = new JMenuItem("Lua");
            jsonMenu.addActionListener((e) -> {
                String out_path = "target/out/proto/lua";
                mainForm.actionProtobuf("lua", out_path, "win64/protoc.exe", "csharp_out");
            });
            excelMenu.add(jsonMenu);
        }
        menuBar.add(excelMenu);
    }

}
