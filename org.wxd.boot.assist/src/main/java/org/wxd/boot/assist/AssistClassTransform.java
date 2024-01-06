package org.wxd.boot.assist;

import javassist.CtBehavior;
import javassist.CtClass;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * 包装类，可以通过字节码增加
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-05 20:49
 **/
public class AssistClassTransform implements ClassFileTransformer {

    String outDir = "target/assist-out";

    PrintStream printStream = null;

    /** 增强类所在包名白名单 */
    private final String BASE_PACKAGE;
    private final JavaAssistBox javaAssistBox = JavaAssistBox.of();

    public AssistClassTransform(String basePackage) {
        this.BASE_PACKAGE = basePackage;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outDir + "/assist.error", false);
            printStream = new PrintStream(fileOutputStream);
        } catch (Exception e) {
            printStream = System.out;
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        try {
            if (BASE_PACKAGE == null || BASE_PACKAGE.isEmpty() || BASE_PACKAGE.isBlank()) return classFileBuffer;
            className = className.replace("/", ".");
            if (!className.startsWith(BASE_PACKAGE)) {
                return classFileBuffer;
            }
            JavaAssistBox.JavaAssist javaAssist = javaAssistBox.editClass(className);
            if (!check(javaAssist.getCtClass(), IAssistMonitor.class.getName()))
                return classFileBuffer;

            CtBehavior[] behaviors = javaAssist.getCtClass().getDeclaredBehaviors();
            //遍历方法进行增强
            for (CtBehavior m : behaviors) {
                MonitorAnn monitorAnn = (MonitorAnn) m.getAnnotation(MonitorAnn.class);
                if (monitorAnn != null && monitorAnn.filter()) continue;
                if (m.getAnnotation(MonitorStart.class) != null) {
                    enhanceStartMethod(className, m);
                } else {
                    enhanceMethod(className, m);
                }
            }
            if (check(javaAssist.getCtClass(), IAssistOutFile.class.getName())) {
                /* 输出修改后的class文件内容 */
                //System.out.println(className + " - out");
                javaAssist.writeFile(outDir);
            }
            return javaAssist.toBytes();
        } catch (Exception e) {
            new RuntimeException(className, e).printStackTrace(printStream);
        }
        return classFileBuffer;
    }

    /***
     *
     * @param sourceClass 原始类
     * @param implClass 需要查找的实现类
     * @return
     * @throws Exception
     */
    private boolean check(CtClass sourceClass, String implClass) throws Exception {
        for (CtClass anInterface : sourceClass.getInterfaces()) {
            if (implClass.equals(anInterface.getName())) {
                return true;
            }
            if (check(anInterface, implClass)) {
                return true;
            }
        }
        if (sourceClass.getSuperclass() != null) {
            return check(sourceClass.getSuperclass(), implClass);
        }
        return false;
    }

    /** 方法增强，添加方法耗时统计 */
    private void enhanceStartMethod(String className, CtBehavior method) {
        if (method.isEmpty()) {
            return;
        }
        String methodName = method.getName();
        if (!method.getMethodInfo().isMethod()) {
            /*如果这不是构造函数或类初始值设定项（静态初始值设定项）*/
            return;
        }
        try {
            method.insertBefore(String.format("%s.THREAD_LOCAL.set(new %s());", IAssistMonitor.class.getName(), MonitorRecord.class.getName()));
            String str = """                    
                    java.lang.Object record = %s.THREAD_LOCAL.get();
                    %s.THREAD_LOCAL.remove();
                    print(record.toString());
                    """.formatted(
                    IAssistMonitor.class.getName(),
                    IAssistMonitor.class.getName()
            );
            method.insertAfter(str);
        } catch (Exception e) {
            new RuntimeException(className + "." + methodName, e).printStackTrace(printStream);
        }
    }

    /** 方法增强，添加方法耗时统计 */
    private void enhanceMethod(String className, CtBehavior method) {
        if (method.isEmpty()) {
            return;
        }
        String methodName = method.getName();
        if (!method.getMethodInfo().isMethod()) {
            /*如果这不是构造函数或类初始值设定项（静态初始值设定项）*/
            return;
        }
        try {
            method.addLocalVariable("start", CtClass.longType);
            method.insertBefore("start = System.nanoTime();");
            String str = """
                    float ms = ((System.nanoTime() - start) / 10000 / 100f);
                    monitor("%s", ms);
                    """
                    .formatted(className + "." + methodName);
            method.insertAfter(str);
        } catch (Exception e) {
            new RuntimeException(className + "." + methodName, e).printStackTrace(printStream);
        }
    }
}
