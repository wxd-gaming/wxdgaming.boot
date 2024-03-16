package wxdgaming.boot.agent;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * 在本地执行命令行
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-12-04 09:56
 **/
public class LocalShell implements Serializable {

    static boolean windowsOs;
    static String initProcess;
    static Charset charset = Charset.forName("utf-8");

    static {
        String property = System.getProperty("os.name");
        System.out.println(property);
        windowsOs = property.toLowerCase().contains("windows");
        if (windowsOs) {
            charset = Charset.forName("GBK");
            initProcess = "cmd";
        } else {
            /*linux*/
            initProcess = "/bin/bash";
        }
    }

    public static void main(String[] args) throws Exception {
        String cmd = String.join(" ", args);
        cmd = cmd.trim();
        final LocalShell localShell = exec(cmd);
        Thread.sleep(1000);
        System.exit(localShell.getExitValue());
    }

    public static LocalShell build() throws Exception {
        return build(true, null);
    }

    public static LocalShell build(boolean showOutPut) throws Exception {
        return build(showOutPut, null);
    }

    public static LocalShell build(File path) throws Exception {
        return build(true, path);
    }

    public static LocalShell build(boolean showOutPut, File path) throws Exception {
        return new LocalShell(showOutPut, path);
    }

    public static LocalShell exec(String cmd) throws Exception {
        return exec(true, cmd);
    }

    public static LocalShell exec(boolean showOutPut, String cmd) throws Exception {
        return exec(showOutPut, null, cmd);
    }

    /**
     * @param path 目录，相当于执行cmd的目录 比如执行java文件需要指定运行目录
     * @param cmd  执行的命令
     * @return
     * @throws Exception
     */
    public static LocalShell exec(File path, String cmd) throws Exception {
        return exec(true, path, cmd);
    }

    /**
     * @param showOutPut 是否显示输出命令
     * @param path       目录，相当于执行cmd的目录 比如执行java文件需要指定运行目录
     * @param cmd        执行的命令
     * @return
     * @throws Exception
     */
    public static LocalShell exec(boolean showOutPut, File path, String cmd) throws Exception {
        return new LocalShell(showOutPut, path)
                .putCmd(cmd)
                .exit();
    }

    public static boolean checkProcess(String processId) {
        String command;

        if (windowsOs) {
            command = "cmd /c tasklist  /FI \"PID eq " + processId + "\"";
        } else {
            command = "ps aux | awk '{print $2}'| grep -w  " + processId;
        }
        LocalShell localShell = null;
        try {
            localShell = exec(false, null, command);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        if (localShell != null) {
            for (String line : localShell.getLines()) {
                if (line != null
                        && line.length() >= processId.length()
                        && line.contains(processId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Process process;//
    private PrintWriter outPut;
    /** 执行完成退出状态，判定执行是否成功 == 0 表示成功 */
    @Getter
    private Integer exitValue = null;
    @Getter
    @Setter
    private boolean appendLine = true;
    @Getter
    @Setter
    private boolean showOutPut = true;
    @Getter
    private LinkedList<String> lines = new LinkedList<>();
    @Getter
    private LinkedList<String> errorLines = new LinkedList<>();
    @Getter
    @Setter
    private Consumer<String> outPutLine = null;

    public LocalShell(boolean showOutPut, File path) throws Exception {
        this.showOutPut = showOutPut;
        showCmd(initProcess);
        //解决脚本没有执行权限; 需要获取一个执行容器
        process = Runtime.getRuntime().exec(initProcess, null, path);
        outPut = new PrintWriter(process.getOutputStream());
        processStream();
    }

    private void await() throws InterruptedException {
        while (process.isAlive()) {
            Thread.sleep(100);
        }
        Thread.sleep(500);
    }

    /**
     * 默认等待进程执行完毕
     *
     * @return
     * @throws Exception
     */
    public LocalShell exit() throws Exception {
        exit(true);
        return this;
    }

    /**
     * @return
     * @throws Exception
     */
    public LocalShell exit(boolean await) throws Exception {
        outPut.close();

        if (await) {
            await();
        }

        exitValue = process.exitValue();
        process.destroy();

        if (exitValue != 0) {
            System.out.println("\n执行结果 exit Value " + exitValue + "；失败");
        }
        if (showOutPut)
            System.out.println();
        return this;
    }

    void showCmd(String cmd) {
        if (showOutPut) {
            if (!windowsOs) {
                System.out.println("--" + cmd + "；");
                System.out.println();
            }
        }
    }

    public LocalShell putCmd(String cmd) throws InterruptedException {
        showCmd(cmd);

        outPut.println(cmd);
        outPut.flush();

        Thread.sleep(100);

        return this;
    }

    /**
     * @return
     * @throws Exception
     */
    protected LocalShell processStream() {
        BufferedReader infoReader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));
        final Thread readerThread = new Thread(() -> {
            while (process.isAlive()) {
                try {
                    reader(false, infoReader);
                    reader(true, errorReader);
                } catch (Throwable e) {
                    e.printStackTrace(System.out);
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }

            }
        }, "local-shell");
        readerThread.setDaemon(true);
        readerThread.start();
        return this;
    }

    private LocalShell reader(boolean isError, BufferedReader bufferedReader) throws Exception {
        if (bufferedReader.ready()) {
            String readLine = bufferedReader.readLine();
            if (readLine != null && readLine.length() > 0) {

                lines.add(readLine);
                if (outPutLine != null) outPutLine.accept(readLine);
                if (!isError) {
                    if (showOutPut) {
                        System.out.print(readLine);
                        if (appendLine || !windowsOs) {
                            System.out.println();
                        }
                    }
                } else {
                    errorLines.add(readLine);
                    System.err.print("\n" + readLine);
                    if (appendLine || !windowsOs) {
                        System.err.println();
                    }
                }

                readLine = readLine.toLowerCase();
                if (readLine.contains("[y/n]") || readLine.contains("y?")) {
                    putCmd("y");
                }
            }
        }
        return this;
    }

}
