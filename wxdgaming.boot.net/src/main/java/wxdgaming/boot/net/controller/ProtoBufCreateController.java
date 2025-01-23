package wxdgaming.boot.net.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.LocalShell;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.TemplatePack;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.ProtoMapping;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-08 15:01
 **/
@Getter
@Setter
@Accessors(chain = true)
public class ProtoBufCreateController {

    /** proto 源文件目录 */
    private String protoSourcePath;
    /** proto 导出编译文件目录 */
    private String protoOutPath;
    /** 生成handler目录 */
    private String codeOutPath;

    public void buildProtobufToJava(String protoc_path) {
        buildProtobuf("java", protoc_path, "java_out");
    }

    public void buildProtobufToJs(String protoc_path) {
        buildProtobuf("js", protoc_path, "js_out");
    }

    /**
     * @param codeLan
     * @param protoc_path
     * @param code_out_name proto导出明明
     */
    public void buildProtobuf(String codeLan,
                              String protoc_path,
                              String code_out_name) {
        try {
            final File java_out_file = new File(protoOutPath);
            if (!java_out_file.exists()) {
                java_out_file.mkdirs();
            }

            File file = FileUtil.findFile(protoc_path);
            if (file == null) {
                new RuntimeException(protoc_path + " 没有找到; ").printStackTrace(System.err);
                return;
            }

            Collection<File> collect = FileUtil.walkFiles(protoSourcePath, 1, ".proto").toList();
            if (collect.isEmpty()) return;
            final LocalShell localShell = LocalShell.build(file.getAbsoluteFile().getParentFile());

            for (File r : collect) {
                final File absoluteFile = r.getAbsoluteFile();
                final String parent = absoluteFile.getParent();
                String cmd = file.getName() + " --proto_path=" + parent + " --" + code_out_name + "=" + protoOutPath + " " + r.getName();
                localShell.putCmd(cmd);
            }
            localShell.exit();
            final LinkedList<String> errorLines = localShell.getErrorLines();
            if (errorLines != null && !errorLines.isEmpty()) {
                System.out.println("执行异常：");
                String joinError = String.join("\n", errorLines);
                System.out.println(joinError);
            }
            System.out.println("转化 " + codeLan + " 处理完成 !!! 请注意异常");
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
        }
    }

    public TreeMap<String, Integer> createMessageId(String startsWithRegex) {
        final String _startsWithRegex = "^(" + startsWithRegex + ").*";
        Collection<File> lists = FileUtil.walkFiles(protoSourcePath, 1, ".proto").toList();
        TreeMap<String, Integer> msgIdMap = new TreeMap<>();
        for (File protoFile : lists) {

            AtomicReference<String> messagePackage = new AtomicReference<>();
            AtomicReference<String> upLine = new AtomicReference<>();

            FileReadUtil.readLine(protoFile, StandardCharsets.UTF_8, line -> {
                if (line.startsWith("option java_package = \"")) {
                    int indexOf = line.indexOf("\"");
                    int of = line.lastIndexOf('"');
                    String mp = line.substring(indexOf + 1, of);
                    messagePackage.set(mp);
                } else if (line.startsWith("message ")) {
                    String messageName = line.replace("message ", "");
                    boolean matches = messageName.matches(_startsWithRegex);
                    if (matches) {
                        int indexOf = messageName.lastIndexOf('{');
                        messageName = StringUtil.filterLine(messageName.substring(0, indexOf));
                        int msgId = StringUtil.hashcode(messageName);
                        String key = messagePackage.get() + "." + messageName;
                        if (msgIdMap.containsKey(key) || msgIdMap.containsValue(msgId)) {
                            throw new RuntimeException("存在相同的消息名称：" + key + ", id=" + msgId);
                        }
                        msgIdMap.put(key, msgId);
                    }
                }
                upLine.set(line);
            });
        }
        return msgIdMap;
    }

    public void createController(String startsWithRegex, String serviceClass) {
        createController("proto-controller.ftl", startsWithRegex, serviceClass);
    }

    public void createController(String ftlName, String startsWithRegex, String serviceClass) {
        TemplatePack templatePack = TemplatePack.build(this.getClass().getClassLoader(), "template/message");
        createController(templatePack, ftlName, startsWithRegex, serviceClass);
    }

    public void createController(TemplatePack templatePack, String ftlName,
                                 String startsWithRegex,
                                 String serviceClass) {
        final String _startsWithRegex = "^(" + startsWithRegex + ").*";
        Collection<File> lists = FileUtil.walkFiles(protoSourcePath, 1, ".proto").toList();
        for (File protoFile : lists) {

            String name = FileUtil.fileName(protoFile);
            System.out.println(name);
            AtomicReference<String> messagePackage = new AtomicReference<>();
            AtomicReference<String> upLine = new AtomicReference<>();

            FileReadUtil.readLine(protoFile, StandardCharsets.UTF_8, line -> {
                if (line.startsWith("option java_package = \"")) {
                    int indexOf = line.indexOf("\"");
                    int of = line.lastIndexOf('"');
                    String mp = line.substring(indexOf + 1, of);
                    messagePackage.set(mp);
                    System.out.println(messagePackage);
                } else if (line.startsWith("message ")) {
                    String message = line.replace("message ", "");
                    boolean matches = message.matches(_startsWithRegex);
                    if (matches) {
                        int indexOf = message.lastIndexOf('{');
                        message = StringUtil.filterLine(message.substring(0, indexOf));

                        String savePack = messagePackage.get().replace(".message", ".controller");
                        String saveClassName = message + "Controller";

                        String saveFileName = codeOutPath + "/"
                                              + savePack.replace(".", "/")
                                              + "/" + saveClassName + ".java";

                        if (FileUtil.exists(saveFileName)) {
                            return;
                        }

                        JSONObject objMap = MapOf.newJSONObject();

                        Set<String> imports = new TreeSet<>();
                        imports.add(messagePackage.get() + "." + message);

                        imports.add(JSONObject.class.getName());
                        imports.add(SocketSession.class.getName());
                        imports.add(ProtoListenerAction.class.getName());
                        imports.add(ProtoController.class.getName());
                        imports.add(ProtoMapping.class.getName());
                        imports.add(IController.class.getName());
                        imports.add(Slf4j.class.getName());

                        objMap.put("imports", imports);
                        objMap.put("filePath", protoFile.getName());
                        objMap.put("comment", upLine.get().replace("//", ""));
                        objMap.put("times", MyClock.nowString());
                        objMap.put("messageName", message);
                        objMap.put("service", "service");
                        objMap.put("savePack", savePack);
                        objMap.put("saveClassName", saveClassName);

                        if (StringUtil.emptyOrNull(serviceClass)) {
                            objMap.put("serviceClass", "");
                        } else {
                            objMap.put("serviceClass", "service = \"" + serviceClass + "\"");
                        }

                        if (message.startsWith("Req")) {
                            String resMessage = message.replace("Req", "Res");
                            imports.add(messagePackage.get() + "." + resMessage);
                            objMap.put("res", resMessage + ".Builder res4Builder = " + resMessage + ".newBuilder();");
                        } else {
                            objMap.put("res", "");
                        }

                        templatePack.ftl2File(ftlName, objMap, saveFileName);


                        try (StreamWriter out = new StreamWriter()) {
                            out.writeLn("==============================================================================================================");
                            out.writeLn("消息文件：" + protoFile.getName());
                            out.writeLn("消息名称：" + messagePackage.get() + "." + message);
                            out.writeLn("消息ID：" + StringUtil.hashcode(objMap.getString("messageName")));
                            out.writeLn("文件包名：" + objMap.getString("savePack"));
                            out.writeLn("处理文件：" + objMap.getString("saveClassName") + ".java");
                            out.writeLn("==============================================================================================================");
                            System.out.println(out.toString());
                            System.out.flush();
                        }
                    }
                }
                upLine.set(line);
            });

        }
    }

}
