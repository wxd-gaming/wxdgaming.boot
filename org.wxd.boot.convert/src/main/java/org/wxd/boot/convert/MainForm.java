package org.wxd.boot.convert;


import org.wxd.boot.agent.LocalShell;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.loader.ClassBytesLoader;
import org.wxd.boot.agent.loader.JavaCoderCompile;
import org.wxd.boot.batis.excel.ExcelRead2Json;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-18 21:28
 **/
public class MainForm implements Serializable {

    /** 当前窗体 */
    public JFrame jFrame;
    JPanel root;
    /** 展示当前选择的文件 */
    public JList jList;
    /** 附加文件，当前选择的文件 */
    public LinkedHashSet<FileItem> selectFiles = new LinkedHashSet<>();
    public JTextArea textAreaLog;

    public static void main(String[] args) {
        final MainForm mainForm = new MainForm();
        mainForm.jFrame.pack();
        mainForm.jFrame.setVisible(true);
    }

    public MainForm() {
        jFrame = new JFrame("無心道 & 转化工具");
        // 关闭窗口结束程序
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setUndecorated(true); // 去掉窗口的装饰
        jFrame.getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);// 采用指定的窗口装饰风格
        // 创建内容面板
        Container contentpage = jFrame.getContentPane();
        // 创建流式布局管理器 对齐方式为左对齐
        final GridLayout gridLayout = new GridLayout();
        LayoutManager layout = new FlowLayout(FlowLayout.LEADING, 4, 3);
        // 设置内容面板布局方式为流布局
        contentpage.setLayout(layout);
        {
            {
                jList = new JList();
                final Dimension dimension1 = new Dimension(235, 587);
                jList.setPreferredSize(dimension1);
                jFrame.add(jList);
            }
            {
                textAreaLog = new JTextArea();
                final Dimension dimension1 = new Dimension(543, 587);
                textAreaLog.setPreferredSize(dimension1);
                jFrame.add(textAreaLog);
            }
        }
        Dimension dimension = new Dimension(800, 650);
        jFrame.setPreferredSize(dimension);
        jFrame.setSize(dimension);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        JMenuBar menuBar = new JMenuBar();
        File scripts = FileUtil.findFile("scripts");
        if (scripts == null) {
            scripts = FileUtil.findFile("src/main/scripts");
        }
        if (scripts == null) {
            scripts = FileUtil.findFile("org.wxd.boot.convert/src/main/scripts");
        }
        if (scripts == null) {
            showBox("没有找到 menu 脚本!!!!");
            System.exit(0);
        }
        try {
            final JavaCoderCompile javaCoderCompile = new JavaCoderCompile();
            javaCoderCompile.compilerJava(scripts.getPath());
            final ClassBytesLoader classBytesLoader = javaCoderCompile.builderClassLoader();
            final Collection<Class<?>> classes = classBytesLoader.getLoadClassMap().values();
            List<AddMenu> addMenus = new ArrayList<>();
            for (Class<?> aClass : classes) {
                final Constructor<?> constructor = aClass.getConstructor();
                final AddMenu instance = (AddMenu) constructor.newInstance();
                addMenus.add(instance);
            }
            addMenus.sort(Comparator.comparingInt(AddMenu::index));
            for (AddMenu addMenu : addMenus) {
                addMenu.addMenu(this, menuBar);
            }
        } catch (Throwable throwable) {
            throw Throw.as(throwable);
        }
        jFrame.setJMenuBar(menuBar);
    }

    public List<String> getSelectPath(String... extendNames) {
        if (this.selectFiles == null || this.selectFiles.isEmpty()) {
            showBox("尚未选择任何文件");
            return null;
        }

        Stream<FileItem> stream = this.selectFiles.stream();
        if (extendNames != null && extendNames.length > 0) {
            stream = stream.filter(v -> {
                final String s = v.getName().toLowerCase();
                for (String extendName : extendNames) {
                    if (s.endsWith(extendName)) {
                        return true;
                    }
                }
                return false;
            });
        }
        List<String> collect = stream
                .map(v -> v.getPath())
                .collect(Collectors.toList());

        if (collect == null || collect.isEmpty()) {
            showBox("尚未选择任何文件：" + String.join(";", extendNames));
            return null;
        }

        return collect;
    }

    public ExcelRead2Json excelRead2Json(String... haveExtends) {

        List<String> collect = getSelectPath(".xls", ".xlsx");
        if (collect == null) return null;

        final ExcelRead2Json excelRead2Json = ExcelRead2Json.builder();

        excelRead2Json
                .dataIndex(2, 3, 4, 1, 5)
                .excelPaths(false, collect.toArray(new String[0]))
                .loadExcel(haveExtends);

        addLog("");

        return excelRead2Json;
    }

    public void actionProtobuf(String codeLan,
                               String out_path,
                               String protoc_path,
                               String code_out_name) {
        event(() -> {
            try {
                final File java_out_file = new File(out_path);
                if (!java_out_file.exists()) {
                    java_out_file.mkdirs();
                }
                String final_out_path = java_out_file.getCanonicalPath();
                File file = FileUtil.findFile("protobuf/" + protoc_path);
                if (file == null) {
                    file = FileUtil.findFile("src/main/protobuf/" + protoc_path);
                }
                if (file == null) {
                    showBox("protobuf/" + protoc_path + " 没有找到; ");
                    return;
                }

                List<String> collect = getSelectPath(".proto");
                if (collect == null) return;
                final LocalShell localShell = LocalShell.build(file.getAbsoluteFile().getParentFile());

                for (String s : collect) {
                    final File r = new File(s);
                    addLog("开始处理 Protobuf 文件：" + r.getName() + " 生成 " + codeLan + " 文件");
                    final File absoluteFile = r.getAbsoluteFile();
                    final String parent = absoluteFile.getParent();
                    String cmd = file.getName() + " --proto_path=" + parent + " --" + code_out_name + "=" + final_out_path + "   " + r.getName();
                    localShell.putCmd(cmd);
                }

                addLog("");
                final LinkedList<String> errorLines = localShell.getErrorLines();
                if (errorLines != null && !errorLines.isEmpty()) {
                    String joinError = errorLines.stream().collect(Collectors.joining("\n"));
                    addLog(joinError);
                    addLog("执行异常：");
                }
                addLog("");

                showBox("转化 " + codeLan + " 处理完成 !!! 请注意异常");
            } catch (Throwable throwable) {
                addLog(Throw.ofString(throwable));
            }
        });
    }

    public void event(Runnable runnable) {
        new Thread(() -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                final String s = Throw.ofString(throwable);
                addLog(s);
            }
        }).start();
    }

    public void addLog(Object logMsg) {
        this.textAreaLog.setText(" " + String.valueOf(logMsg) + "\n" + this.textAreaLog.getText());
    }

    public void showBox(Object logMsg) {
        JOptionPane.showMessageDialog(null, String.valueOf(logMsg));
    }
}
