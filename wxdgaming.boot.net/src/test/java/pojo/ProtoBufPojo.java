package pojo;

import io.protostuff.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Test;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * pojo生成
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-27 16:39
 **/
public class ProtoBufPojo {

    @Test
    public void readProtobuf() {
        actionProtoFile("./src/test/java", "./src/main");
    }

    public void actionProtoFile(String outPath, String readPath) {

        FileUtil
                .walkFiles(readPath, ".proto")
                .forEach(file -> {

                    AtomicReference<BeanInfo> comment = new AtomicReference<>(new BeanInfo());
                    AtomicBoolean start = new AtomicBoolean();
                    AtomicReference<String> packageName = new AtomicReference<>("");

                    TreeSet<String> imports = new TreeSet<>();
                    imports.add(Tag.class.getName());
                    imports.add(Getter.class.getName());
                    imports.add(Setter.class.getName());
                    imports.add(PojoBase.class.getName());
                    imports.add(Accessors.class.getName());
                    imports.add(List.class.getName());
                    imports.add(Map.class.getName());

                    StringBuilder stringBuilder = new StringBuilder();

                    FileReadUtil.readLine(file, StandardCharsets.UTF_8, line -> {
                        if (StringUtil.emptyOrNull(line)) return;
                        if (line.contains("java_multiple_files") && line.contains("true")) {

                        } else if (line.contains("java_package")) {
                            int i = line.indexOf("=");
                            int i1 = line.indexOf(";");
                            String trim = line.substring(i + 1, i1).trim().replace("\"", "");
                            packageName.set(trim);
                        } else if (line.startsWith("//")) {
                            comment.get().comment = line.substring(2);
                        } else if (line.startsWith("message")) {
                            String[] split = line.split(" ");
                            System.out.println("【" + split[1] + "】");
                            comment.get().className = split[1];
                            start.set(true);
                        } else if (line.contains("}")) {
                            stringBuilder.append(comment.get().classString());
                            comment.set(new BeanInfo());
                            start.set(false);
                        } else {
                            if (start.get()) {
                                comment.get().addField(line);
                            }
                        }
                    });

                    String to = "package " + packageName + ";";
                    to += "\n";
                    for (String anImport : imports) {
                        to += "\nimport " + anImport + ";";
                    }
                    to += "\n";
                    String className = file.getName().replace(".proto", "");
                    to += "\npublic class " + className + " {";
                    to += stringBuilder.toString();
                    to += "\n}";
                    System.out.println(to);
                    FileWriteUtil.writeString(outPath + "/" + packageName.get().replace(".", "/") + "/" + className + ".java", to);
                });

    }

    @Getter
    public static class BeanInfo {


        private String className;
        private String comment;
        private List<FiledInfo> filedInfos = new ArrayList<>();

        public void addField(String line) {
            FiledInfo filedInfo = new FiledInfo(line);
            addField(filedInfo);
        }

        public void addField(FiledInfo filedInfo) {
            if (StringUtil.notEmptyOrNull(filedInfo.getField())) {
                if (filedInfos.stream().anyMatch(v -> v.getTag() == filedInfo.getTag())) {
                    // tag重复
                    throw new RuntimeException(className + " - tag " + filedInfo.getTag() + " 重复");
                }
                if (filedInfos.stream().anyMatch(v -> Objects.equals(v.getFieldName(), filedInfo.getFieldName()))) {
                    // tag重复
                    throw new RuntimeException(className + " - tag " + filedInfo.getTag() + " 重复");
                }
                getFiledInfos().add(filedInfo);
            }
        }

        public String classString() {
            String to = "";
            to += "\n";
            to += "\n   /** " + comment + " */";
            to += "\n   @Getter";
            to += "\n   @Setter";
            to += "\n   @Accessors(chain = true)";
            to += "\n   public static class " + className + " extends " + PojoBase.class.getSimpleName() + " {\n";
            for (FiledInfo filedInfo : filedInfos) {
                to += "\n" + filedInfo.toString();
            }
            to += "\n";
            to += "\n   }";
            return to;
        }

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class FiledInfo extends ObjectBase {
        private int tag;
        private String fieldName;
        private String field;
        private String comment = "";

        public FiledInfo(String line) {
            if (StringUtil.emptyOrNull(line)) {
                return;
            }
            line = line.trim();
            String[] split = line.split("[;；]");
            if (split.length > 1) {
                comment = split[1].replace("//", "");
            }
            List<String> split1 = List.of(split[0].split(" "));
            ArrayList<String> tmp = new ArrayList<>();
            for (String string : split1) {
                if (StringUtil.emptyOrNull(string)) continue;
                tmp.add(string.trim());
            }

            String string = tmp.getFirst();
            boolean repeated = false;
            if (string.equals("repeated")) {
                repeated = true;
                tmp.removeFirst();
                string = tmp.getFirst();
            }
            switch (string) {
                case "bool":
                    if (repeated) {
                        field = "List<" + Boolean.class.getSimpleName() + ">";
                    } else {
                        field = boolean.class.getSimpleName();
                    }
                    break;
                case "int32":
                    if (repeated) {
                        field = "List<" + Integer.class.getSimpleName() + ">";
                    } else {
                        field = int.class.getSimpleName();
                    }
                    break;
                case "int64":
                    if (repeated) {
                        field = "List<" + Long.class.getSimpleName() + ">";
                    } else {
                        field = long.class.getSimpleName();
                    }
                    break;
                case "string":
                    if (repeated) {
                        field = "List<" + String.class.getSimpleName() + ">";
                    } else {
                        field = String.class.getSimpleName();
                    }
                    break;
                default: {
                    if (string.startsWith("map<") && string.endsWith(">")) {
                        field = String.class.getSimpleName();
                        field = StringUtil.upperFirst(field);
                        field = field
                                .replace("bool", Boolean.class.getSimpleName())
                                .replace("int32", Integer.class.getSimpleName())
                                .replace("int64", Long.class.getSimpleName())
                                .replace("string", String.class.getSimpleName())
                        ;
                    } else {
                        throw new RuntimeException("解析失败 " + string);
                    }
                }
            }
            fieldName = tmp.get(1);
            field += " " + tmp.get(1);
            field += ";";
            tag = Integer.parseInt(tmp.get(3));
        }

        @Override public String toString() {
            String to = "";
            to += "       /** " + comment + " */";
            to += "\n       @Tag(" + tag + ")";
            to += "\n       private " + field;
            return to;
        }
    }

}
