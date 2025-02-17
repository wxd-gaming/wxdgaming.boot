package wxdgaming.boot.net.pojo;

import io.protostuff.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.lang.ObjectBase;
import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.ann.ProtoMapping;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * pojo生成
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-05-27 16:39
 **/
public class ProtoBuf2Pojo {

    /** 处理proto文件 */
    public static void actionProtoFile(String outPath, String readPath) {

        FileUtil
                .walkFiles(readPath, ".proto")
                .forEach(filePath -> {

                    AtomicBoolean multiple_files = new AtomicBoolean(true);

                    AtomicReference<BeanInfo> comment = new AtomicReference<>(new BeanInfo(multiple_files.get()));
                    AtomicBoolean start = new AtomicBoolean();
                    AtomicReference<String> packageName = new AtomicReference<>("");

                    TreeSet<String> imports = new TreeSet<>();
                    imports.add(Tag.class.getName());
                    imports.add(Getter.class.getName());
                    imports.add(Setter.class.getName());
                    imports.add(PojoBase.class.getName());
                    imports.add(Accessors.class.getName());
                    imports.add(List.class.getName());
                    imports.add(ArrayList.class.getName());
                    imports.add(Map.class.getName());
                    imports.add(LinkedHashMap.class.getName());
                    imports.add(MapOf.class.getName());


                    StringBuilder stringBuilder = new StringBuilder();

                    FileReadUtil.readLine(filePath, StandardCharsets.UTF_8, line -> {
                        line = line.trim();
                        if (StringUtils.isBlank(line)) return;
                        if (line.contains("java_multiple_files")) {
                            // if (line.contains("true")) {
                            //     multiple_files.set(true);
                            //     comment.get().multiple_files = true;
                            // }
                        } else if (line.contains("java_package")) {
                            int i = line.indexOf("=");
                            int i1 = line.indexOf(";");
                            String trim = line.substring(i + 1, i1).trim().replace("\"", "");
                            packageName.set(trim);
                        } else if (line.startsWith("//")) {
                            comment.get().comment = line.substring(2);
                        } else if (line.startsWith("message") || line.startsWith("enum")) {
                            String[] split = line.split(" ");
                            System.out.println("【" + split[1] + "】");

                            comment.get().classType = line.startsWith("message") ? "class" : "enum";
                            comment.get().className = split[1];

                            start.set(true);
                        } else if (line.contains("}")) {
                            String string = comment.get().string();
                            if (multiple_files.get()) {
                                String p1 = filePath.getName().replace(".proto", "").replace("Message", "");
                                p1 = StringUtils.lowerFirst(p1);
                                String to = "package " + packageName + "." + p1 + ";";
                                to += "\n";
                                for (String anImport : imports) {
                                    to += "\nimport " + anImport + ";";
                                }
                                to += "\n\n\n";
                                to += string;
                                System.out.println(to);
                                FileWriteUtil.writeString(outPath + "/" + packageName.get().replace(".", "/") + "/" + p1 + "/" + comment.get().getClassName() + ".java", to);
                            }
                            stringBuilder.append(string);
                            comment.set(new BeanInfo(multiple_files.get()));
                            start.set(false);
                        } else {
                            if (start.get()) {
                                comment.get().addField(line);
                            }
                        }
                    });

                    if (!multiple_files.get()) {

                        String collect = imports.stream().map(v -> "import " + v + ";").collect(Collectors.joining("\n"));
                        String className = filePath.getName().replace(".proto", "");
                        String to = """
                                package %s;
                                
                                %s
                                
                                /**
                                 * %s
                                 *
                                 *@author: wxd-gaming(無心道, 15388152619)
                                 *@version: v1.1
                                 */
                                public class %s {
                                %s
                                }""".formatted(
                                packageName.get(),
                                collect,
                                filePath.getName(),
                                className,
                                stringBuilder.toString());
                        System.out.println(to);
                        FileWriteUtil.writeString(outPath + "/" + packageName.get().replace(".", "/") + "/" + className + ".java", to);
                    }
                });

    }

    @Getter
    public static class BeanInfo {

        private boolean multiple_files = false;
        private String classType;
        private String className;
        private String comment;
        private final List<FiledInfo> filedInfos = new ArrayList<>();

        public BeanInfo(boolean multiple_files) {
            this.multiple_files = multiple_files;
        }

        public void addField(String line) {
            FiledInfo filedInfo = new FiledInfo(classType, line);
            addField(filedInfo);
        }

        public void addField(FiledInfo filedInfo) {
            if (StringUtils.isNotBlank(filedInfo.getField())) {
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

        public String string() {
            if ("class".equals(classType)) {
                return classString();
            }
            return enumString();
        }

        public String classString() {
            String f = "\n";
            for (FiledInfo filedInfo : filedInfos) {
                if (multiple_files) {
                    f += """
                                /** %s */
                                @Tag(%s) private %s;
                            """.formatted(filedInfo.comment, filedInfo.tag, filedInfo.field);
                } else {
                    f += """
                                    /** %s */
                                    @Tag(%s) private %s;
                            """.formatted(filedInfo.comment, filedInfo.tag, filedInfo.field);
                }
            }
            if (multiple_files) {
                return """                        
                        /** %s */
                        @Getter
                        @Setter
                        @Accessors(chain = true)
                        public class %s extends PojoBase {
                        
                            /** 反序列化 */
                            public static %s parseFrom(byte[] bytes) {
                                %s s = new %s();
                                s.decode(bytes);
                                return s;
                            }
                        %s
                        }
                        """.formatted(comment, className, className, className, className, f);
            } else {
                return """
                        
                            /** %s */
                            @Getter
                            @Setter
                            @Accessors(chain = true)
                            public static class %s extends PojoBase {
                        
                                /** 反序列化 */
                                public static %s parseFrom(byte[] bytes) {
                                    %s s = new %s();
                                    s.decode(bytes);
                                    return s;
                                }
                            %s
                            }
                        """.formatted(comment, className, className, className, className, f);
            }
        }

        public String enumString() {
            String f = "\n";
            for (FiledInfo filedInfo : filedInfos) {
                if (multiple_files) {
                    f += """
                                /** %s */
                                @Tag(%s)
                                %s(%s, "%s"),
                            """.formatted(filedInfo.comment, filedInfo.tag, filedInfo.field.trim(), filedInfo.tag, filedInfo.comment);
                } else {
                    f += """
                                    /** %s */
                                    @Tag(%s)
                                    %s(%s, "%s"),
                            """.formatted(filedInfo.comment, filedInfo.tag, filedInfo.field.trim(), filedInfo.tag, filedInfo.comment);
                }
            }
            if (multiple_files) {
                return """
                        /** %s */
                        @Getter
                        public enum %s {
                        %s
                            ;
                        
                            private static final Map<Integer, %s> static_map = MapOf.asMap(%s::getCode, %s.values());
                        
                            public static %s valueOf(int code) {
                                return static_map.get(code);
                            }
                        
                            /** code */
                            private final int code;
                            /** 备注 */
                            private final String command;
                        
                            %s(int code, String command) {
                                this.code = code;
                                this.command = command;
                            }
                        }
                        """.formatted(comment, className, f, className, className, className, className, className);
            } else {
                return """
                        
                        
                            /** %s */
                            @Getter
                            public enum %s {
                            %s
                                ;
                        
                                private static final Map<Integer, %s> static_map = MapOf.asMap(%s::getCode, %s.values());
                        
                                public static %s valueOf(int code) {
                                    return static_map.get(code);
                                }
                        
                                /** code */
                                private final int code;
                                /** 备注 */
                                private final String command;
                        
                                %s(int code, String command) {
                                    this.code = code;
                                    this.command = command;
                                }
                            }
                        """.formatted(comment, className, f, className, className, className, className, className);
            }
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

        public FiledInfo(String classType, String line) {
            if (StringUtils.isBlank(line)) {
                return;
            }
            line = line.trim();
            String[] split = line.split("[;；]");
            if (split.length > 1) {
                comment = split[1].replace("//", "");
            }
            List<String> split1 = List.of(split[0].split(" "));
            ArrayList<String> tmp = new ArrayList<>();
            if ("enum".equals(classType)) {
                tmp.add("enum");
            }
            for (String string : split1) {
                if (StringUtils.isBlank(string)) continue;
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
                case "enum": {
                    field = "";
                }
                break;
                case "bytes": {
                    field = "byte[]";
                }
                break;
                default: {
                    if (string.startsWith("map<") && string.endsWith(">")) {
                        field = String.class.getSimpleName();
                        field = StringUtils.upperFirst(field);
                        field = field
                                .replace("bool", Boolean.class.getSimpleName())
                                .replace("int32", Integer.class.getSimpleName())
                                .replace("int64", Long.class.getSimpleName())
                                .replace("bytes", byte[].class.getSimpleName())
                                .replace("string", String.class.getSimpleName())
                        ;
                    } else {
                        if (repeated) {
                            field = "List<" + string + ">";
                        } else {
                            field = string;
                        }
                    }
                }
            }
            fieldName = tmp.get(1);
            field += " " + tmp.get(1);
            if (repeated) {
                field += " = new ArrayList<>()";
            } else if (field.contains("Map<")) {
                field += " = new LinkedHashMap<>()";
            }
            tag = Integer.parseInt(tmp.get(3));
        }

    }

    /**
     * 创建mapping
     *
     * @param outPath         输出路径
     * @param packageName     输出包名
     * @param readPackageName 读取包名
     * @param spi             要处理的接口 例如Req
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2025-01-15 14:58
     */
    public static void createMapping(String outPath, String packageName, String spi, String readPackageName) {
        ReflectContext reflectContext = ReflectContext.Builder.of(readPackageName).build();
        reflectContext
                .withSuper(PojoBase.class)
                .forEach(pojoBase -> {

                    Class<?> cls = pojoBase.getCls();
                    createMapping0(outPath, packageName, spi, cls);

                });
    }

    public static void createMapping0(String outPath, String packageName, String spi, Class<?> cls) {
        String simpleName = cls.getSimpleName();
        if (!simpleName.startsWith(spi)) {
            return;
        }
        String[] split = cls.getPackageName().split("[.]");
        String p1 = split[split.length - 1];
        packageName = packageName + "." + p1 + "." + "spi";
        String className = simpleName + "Spi";
        String fileName = outPath + "/" + packageName.replace(".", "/") + "/" + className + ".java";
        Path filePath = Paths.get(fileName);
        boolean exists = Files.exists(filePath);
        System.out.println(String.format("\n是否存在=%s\n文件=%s\n包名=%s\n类名=%s\n", exists, fileName, packageName, className));
        if (exists) {
            return;
        }
        TreeSet<String> imports = new TreeSet<>();
        imports.add(ProtoMapping.class.getName());
        imports.add(Slf4j.class.getName());
        imports.add(SocketSession.class.getName());
        imports.add(cls.getName());

        String importString = imports.stream().map(s -> "import " + s + ";").collect(Collectors.joining("\n"));

        String spiCode = """
                package %s;
                
                %s
                
                /**
                 * %s
                 *
                 * @author: wxd-gaming(無心道, 15388152619)
                 * @version: v1.1
                 **/
                @Slf4j
                public class %s {
                
                    @ProtoMapping
                    public void %s(SocketSession socketSession, %s req) {
                
                    }
                
                }""".formatted(packageName, importString, "", className, cls.getSimpleName(), cls.getSimpleName());

        System.out.println(spiCode);
        FileWriteUtil.writeString(fileName, spiCode);

    }

}
