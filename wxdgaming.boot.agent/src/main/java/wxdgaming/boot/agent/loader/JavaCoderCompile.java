package wxdgaming.boot.agent.loader;


import wxdgaming.boot.agent.JDKVersion;
import wxdgaming.boot.agent.LogbackUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * java 文件编译
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 **/
public class JavaCoderCompile {

    /*获取编译器实例*/
    private final JavaCompiler compiler;
    private final DiagnosticCollector<JavaFileObject> oDiagnosticCollector;
    private JavaFileObjectManager javaFileManager = null;
    /**
     * spring 项目需要把主jar包解压，然后classpath指定 BOOT-INF/classes/ 目录
     * 比如 ./lib:./BOOT-INF/classes/
     */
    private String classPath = null;
    private ClassLoader parentClassLoader = null;

    public JavaCoderCompile() {
        compiler = ToolProvider.getSystemJavaCompiler();
        oDiagnosticCollector = new DiagnosticCollector<>();
    }

    public JavaCoderCompile classPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    public JavaCoderCompile parentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }

    public JavaFileObjectManager javaFileManager() {
        if (javaFileManager == null) {
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(oDiagnosticCollector, null, StandardCharsets.UTF_8);
            javaFileManager = new JavaFileObjectManager(fileManager, parentClassLoader);
        }
        return javaFileManager;
    }

    /**
     * 加载java 源代码，并返回一个类
     *
     * @param javaCoder
     * @return
     */
    public JavaCoderCompile compilerCode(String javaCoder) throws Exception {
        //类全名
        String fullClassName = JavaFileObject4StringCode.readFullClassName(javaCoder);
        //构造源代码对象
        JavaFileObject javaFileObject = new JavaFileObject4StringCode(fullClassName, javaCoder);
        List<JavaFileObject> javaFileObjects = List.of(javaFileObject);
        compilerJava(null, javaFileObjects);
        return this;
    }

    /**
     * @param sourceDir 需要编译的文件路径
     */
    public JavaCoderCompile compilerJava(String sourceDir) throws Exception {
        final Collection<File> sourceFileList = FileUtil.walkFiles(sourceDir, ".java").collect(Collectors.toList());
        compilerJava(sourceDir, sourceFileList);
        return this;
    }

    /**
     * 需要编译的文件
     *
     * @param sourceDir      文件原路径
     * @param sourceFileList 文件列表
     * @return
     */
    public JavaCoderCompile compilerJava(String sourceDir, Collection<File> sourceFileList) throws Exception {
        LogbackUtil.logger().info("目录：{}/{}, 文件数量：{}", System.getProperty("user.dir"), sourceDir, sourceFileList.size());
        if (!sourceFileList.isEmpty()) {
            final Iterable<? extends JavaFileObject> compilerFiles = javaFileManager().getSuperFileManager().getJavaFileObjectsFromFiles(sourceFileList);
            this.compilerJava(sourceDir, compilerFiles);
        }
        return this;
    }

    /**
     * @param sourceDir     可以null
     * @param compilerFiles 需要编译的文件
     */
    public JavaCoderCompile compilerJava(String sourceDir, Iterable<? extends JavaFileObject> compilerFiles) throws Exception {
        JDKVersion jdkVersion = JDKVersion.runTimeJDKVersion();
        LogbackUtil.logger().info("目录：{}/{}, compiler java file jdk_version：{}", System.getProperty("user.dir"), sourceDir, jdkVersion.getCurVersionString());
        /**
         * 编译选项，在编译java文件时，
         * <p>
         *     编译程序会自动的去寻找java文件引用的其他的java源文件或者class。
         * <p>
         *     -sourcepath选项就是定义java源文件的查找目录，
         * <p>
         *     -classpath选项就是定义class文件的查找目录。
         */
        List<String> options = new LinkedList<>();
        options.add("-g");
        options.add("-Xdiags:verbose");/*完整信息*/
        options.add("-source");
        options.add(JDKVersion.runTimeJDKVersion().getVersionString());
        options.add("-encoding");
        options.add(StandardCharsets.UTF_8.toString());

        if (sourceDir != null) {
            options.add("-sourcepath");
            options.add(sourceDir); //指定文件目录
        }

        if (classPath != null) {
            options.add("-classpath");
            options.add(classPath); //指定文件目录
        }

        /*获取编译器实例*/
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null,
                javaFileManager(),
                oDiagnosticCollector,
                options,
                null,
                compilerFiles);
        // 运行编译任务
        Boolean call = compilationTask.call();
        if (!call) {
            StringBuilder sb = new StringBuilder();
            sb.append("编译异常：").append("\n");

            for (Diagnostic<? extends JavaFileObject> oDiagnostic : oDiagnosticCollector.getDiagnostics()) {
                sb
                        .append("\n").append(oDiagnostic.getKind().toString())
                        .append(" ：").append(oDiagnostic.getMessage(Locale.SIMPLIFIED_CHINESE));
                if (oDiagnostic.getSource() != null) {
                    sb
                            .append(" 文件：").append(oDiagnostic.getSource().getName());
                } else {
                    sb
                            .append(" 文件：未知");
                }
                sb
                        .append(" line:").append(oDiagnostic.getLineNumber())
                        .append(" pos:").append(oDiagnostic.getStartPosition());
            }

            throw new Exception(sb.toString());
        }
        return this;
    }

    /**
     * 导出文件
     *
     * @param outPath 路径
     */
    public JavaCoderCompile outPutFile(String outPath, boolean forceClear) {
        if (forceClear) {
            FileUtil.del(outPath);
        }
        final Map<String, byte[]> stringMap = toBytesMap();
        LogbackUtil.logger().info("输出 class文件 目录：{} 数量：{}", outPath, stringMap.size());
        FileWriteUtil.writeClassFile(outPath, stringMap);
        LogbackUtil.logger().info("输出 class文件 目录：{} 数量：{}", outPath, stringMap.size());
        return this;
    }

    /** 当前编译器，所有类的加载器 */
    public ClassDirLoader classLoader() {
        return classLoader(this.parentClassLoader, "target/classes", true);
    }

    /**
     * 当前编译器，所有类的加载器，通过输出到目录，重新得到完整的类加载
     *
     * @param forceClear 强制覆盖目录
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-04-04 23:31
     */
    public ClassDirLoader classLoader(boolean forceClear) {
        return classLoader(this.parentClassLoader, "target/classes", forceClear);
    }

    /**
     * 当前编译器，所有类的加载器，通过输出到目录，重新得到完整的类加载
     *
     * @param outClassPath 文件所在的目录
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-04-04 23:31
     */
    public ClassDirLoader classLoader(String outClassPath) {
        return classLoader(this.parentClassLoader, outClassPath, true);
    }

    /**
     * 当前编译器，所有类的加载器，通过输出到目录，重新得到完整的类加载
     *
     * @param outClassPath class文件输出的目录
     * @param forceClear   强制覆盖目录
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-04-04 23:31
     */
    public ClassDirLoader classLoader(String outClassPath, boolean forceClear) {
        return classLoader(this.parentClassLoader, outClassPath, forceClear);
    }

    /**
     * 当前编译器，所有类的加载器，通过输出到目录，重新得到完整的类加载
     *
     * @param parent       class的父级
     * @param outClassPath class文件输出的目录
     * @param forceClear   强制覆盖目录
     * @return
     * @throws Exception
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-04-04 23:21
     */
    public ClassDirLoader classLoader(ClassLoader parent, String outClassPath, boolean forceClear) {
        outPutFile(outClassPath, forceClear);
        return new ClassDirLoader(parent, outClassPath);
    }


    /** 编译后所有的类 */
    public Map<String, byte[]> toBytesMap() {
        return javaFileManager().getClassFileObjectLoader().getClassFileMap();
    }

    /** 获取所有的编译后的class */
    public Collection<ClassInfo> toAllClass() {
        return javaFileManager().getClassFileObjectLoader().getLoadClassInfoMap().values();
    }

}
