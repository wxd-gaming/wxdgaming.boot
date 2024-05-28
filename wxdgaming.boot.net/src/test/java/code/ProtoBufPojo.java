package code;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Test;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.StringUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
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

    }

    public void actionProtoFile(String outPath) {
        AtomicReference<BeanInfo> comment = new AtomicReference<>(new BeanInfo());
        AtomicBoolean start = new AtomicBoolean();

        AtomicReference<String> packageName = new AtomicReference<>("");
        TreeSet<String> imports = new TreeSet<>();
        imports.add(Getter.class.getName());
        imports.add(Setter.class.getName());
        imports.add(ObjectBase.class.getName());
        imports.add(Accessors.class.getName());
        File file = new File(outPath);
        FileReadUtil.readLine(file, StandardCharsets.UTF_8, line -> {
            if (StringUtil.emptyOrNull(line)) return;
            if (line.startsWith("//")) {
                comment.get().comment = line.substring(2);
            } else if (line.contains("java_package")) {
                int i = line.indexOf("=");
                int i1 = line.indexOf(";");
                String trim = line.substring(i + 1, i1).trim().replace("\"", "");
                trim += "." + file.getName().replace(".proto", "").trim().toLowerCase();
                packageName.set(trim);
            } else if (line.startsWith("message")) {
                String[] split = line.split(" ");
                System.out.println("【" + split[1] + "】");
                comment.get().className = split[1];
                start.set(true);
            } else if (line.contains("}")) {
                System.out.println(comment.get().classString(packageName.get(), imports));
                comment.set(new BeanInfo());
                start.set(false);
            } else {
                if (start.get()) {
                    comment.get().addField(line);
                }
            }
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

        public String classString(String packageName, TreeSet<String> imports) {
            String to = "package " + packageName + ";";
            to += "\n";
            for (String anImport : imports) {
                to += "\nimport " + anImport;
            }
            to += "\n";
            to += "\n";
            to += "\n/** " + comment + " */";
            to += "\n@Getter";
            to += "\n@Setter";
            to += "\n@Accessors(chain = true)";
            to += "\npublic class " + className + " extends ObjectBase {\n";
            for (FiledInfo filedInfo : filedInfos) {
                to += "\n" + filedInfo.toString();
            }
            to += "\n";
            to += "\n" +
                    "   public byte[] encode() {\n" +
                    "      return SerializerUtil.encode(this);\n" +
                    "   }\n" +
                    "   \n" +
                    "   public " + className + " decode(byte[] bytes) {\n" +
                    "      SerializerUtil.decode(bytes, this);\n" +
                    "      return this;\n" +
                    "   }";
            to += "\n}";
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

            String string = tmp.get(0);
            switch (string) {
                case "bool":
                    field = boolean.class.getSimpleName();
                    break;
                case "int32":
                    field = int.class.getSimpleName();
                    break;
                case "int64":
                    field = long.class.getSimpleName();
                    break;
                case "string":
                    field = String.class.getSimpleName();
                    break;
                default: {
                    throw new RuntimeException("解析失败 " + string);
                }
            }
            fieldName = tmp.get(1);
            field += " " + tmp.get(1);
            field += ";";
            tag = Integer.parseInt(tmp.get(3));
        }

        @Override public String toString() {
            String to = "";
            to += "   /** " + comment + " */";
            to += "\n   @Tag(" + tag + ")";
            to += "\n   private" + field;
            return to;
        }
    }

    public static class PojoBase {

        public byte[] encode() {
            return SerializerUtil.encode(this);
        }

        public PojoBase decode(byte[] bytes) {
            SerializerUtil.decode(bytes, this);
            return this;
        }

    }

}
