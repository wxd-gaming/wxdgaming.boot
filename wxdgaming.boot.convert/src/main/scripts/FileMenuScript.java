
import wxdgaming.boot.convert.AddMenu;
import wxdgaming.boot.convert.FileItem;
import wxdgaming.boot.convert.MainForm;
import wxdgaming.boot.core.str.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-19 13:37
 **/
public class FileMenuScript implements Serializable, AddMenu {

    @Override
    public int index() {
        return 1;
    }

    @Override
    public void addMenu(MainForm mainForm, JMenuBar menuBar) {
        /** 文件选择处理按钮 */
        JMenu fileMenu = new JMenu("File");
        {
            JMenuItem openFileBtn = new JMenuItem("Select Files");
            openFileBtn.setToolTipText("选择文件");
            fileMenu.add(openFileBtn);

            openFileBtn.addActionListener(e -> {
                FileDialog openFile = new FileDialog(mainForm.jFrame, "选择文件", FileDialog.LOAD);
                openFile.setMultipleMode(true);
                openFile.setLocationRelativeTo(null);
                openFile.setVisible(true);
                String dirName = openFile.getDirectory();
                if (StringUtil.notEmptyOrNull(dirName)) {
                    mainForm.selectFiles.addAll(
                            Arrays.stream(openFile.getFiles())
                                    .filter(v -> {
                                        String name = v.getName().toLowerCase();
                                        if (name.endsWith(".xls")
                                                || name.endsWith(".xlsx")
                                                || name.endsWith(".proto")) {
                                            return true;
                                        }
                                        return false;
                                    })
                                    .map(v -> {
                                        mainForm.addLog("选择文件：" + v.getPath());
                                        return new FileItem(v);
                                    })
                                    .collect(Collectors.toList())
                    );
                    //JOptionPane.showMessageDialog(null, collect); //显示提示信息
                    mainForm.jList.setListData(mainForm.selectFiles.toArray());
                }
            });
        }
        {
            fileMenu.addSeparator();
        }
        {
            JMenuItem menuItem = new JMenuItem("Clear Files");
            menuItem.setToolTipText("清空已经选择的文件");
            menuItem.addActionListener((e) -> {
                        mainForm.selectFiles.clear();
                        mainForm.jList.setListData(new Object[0]);
                    }
            );
            fileMenu.add(menuItem);
        }
        {
            JMenuItem menuItem = new JMenuItem("Clear Log");
            menuItem.setToolTipText("清空输出日志记录");
            menuItem.addActionListener((e) -> mainForm.textAreaLog.setText(""));
            fileMenu.add(menuItem);
        }
        {
            fileMenu.addSeparator();
        }
        {
            JMenuItem menuItem = new JMenuItem("Exit");
            menuItem.addActionListener((e) -> System.exit(0));
            fileMenu.add(menuItem);
        }
        menuBar.add(fileMenu);
    }

}
