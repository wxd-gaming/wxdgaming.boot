package org.wxd.boot.assist;

import javassist.CtBehavior;
import javassist.CtClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * 包装类，可以通过字节码增加
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-05 20:49
 **/
public class AssistClassTransform implements ClassFileTransformer {

    PrintStream printStream = null;

    /** 增强类所在包名白名单 */
    private final Set<String> Filter_PACKAGE = new HashSet<>();
    private final JavaAssistBox javaAssistBox = JavaAssistBox.of();

    public AssistClassTransform(String filterPackage) {
        if (filterPackage != null && !filterPackage.isEmpty() && !filterPackage.isBlank()) {
            String[] split = filterPackage.split("\\|");
            for (String string : split) {
                Filter_PACKAGE.add(string);
            }
        }
        try {
            new File(AssistMonitor.ASSIST_OUT_DIR).mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(AssistMonitor.ASSIST_OUT_DIR + "/assist.error", false);
            printStream = new PrintStream(fileOutputStream);
        } catch (Throwable e) {
            printStream = System.out;
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String clazzName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        try {
            final String className = clazzName.replace("/", ".");
            if (className.startsWith("com.sun")
                    || className.startsWith("jdk.")
                    || className.startsWith("java.")
                    || className.startsWith("javax.")
                    || (!Filter_PACKAGE.isEmpty() && Filter_PACKAGE.stream().noneMatch(className::startsWith)))
                return classFileBuffer;

            JavaAssistBox.JavaAssist javaAssist = javaAssistBox.editClass(className);
            if (!check(javaAssist.getCtClass(), IAssistMonitor.class.getName()))
                return classFileBuffer;

            CtBehavior[] behaviors = javaAssist.getCtClass().getDeclaredBehaviors();
            //遍历方法进行增强
            for (CtBehavior m : behaviors) {
                MonitorAnn monitorAnn = (MonitorAnn) m.getAnnotation(MonitorAnn.class);
                if (monitorAnn != null && monitorAnn.filter()) continue;
                enhanceStartMethod(className, m);
            }
            if (check(javaAssist.getCtClass(), IAssistOutFile.class.getName())) {
                /* 输出修改后的class文件内容 */
                //System.out.println(className + " - out");
                javaAssist.writeFile(AssistMonitor.ASSIST_OUT_DIR);
            }
            return javaAssist.toBytes();
        } catch (Throwable e) {
            new RuntimeException(clazzName, e).printStackTrace(printStream);
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
            /*空方法，没意义*/
            return;
        }
        String methodName = method.getName();
        if (method.getMethodInfo().isStaticInitializer()) {
            /*如果这不是构造函数或类初始值设定项（静态初始值设定项）*/
            return;
        }
        try {
            method.addLocalVariable("hasParent", CtClass.booleanType);
            method.insertBefore(String.format("hasParent = %s.start();", AssistMonitor.class.getName()));
            String str = """                    
                    %s.close(hasParent, this);
                    """
                    .formatted(AssistMonitor.class.getName());
            method.insertAfter(str);
        } catch (Throwable e) {
            new RuntimeException(className + "." + methodName, e).printStackTrace(printStream);
        }
    }
}
