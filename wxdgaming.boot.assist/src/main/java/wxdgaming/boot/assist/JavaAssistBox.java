package wxdgaming.boot.assist;

import javassist.*;
import lombok.Getter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * javassist 代码编辑器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-06 20:07
 **/
@Getter
public class JavaAssistBox {

    public static String javaClassPath() {
        return System.getProperty("java.class.path");
    }

    public static JavaAssistBox of() {
        return new JavaAssistBox();
    }

    static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    private JavaAssistBox() {
    }

    public ClassPool build() {
        final ClassPool classPool = new ClassPool(null);
        classPool.appendSystemPath();
        try {
            classPool.insertClassPath("./class");
            classPool.appendClassPath(javaClassPath());
        } catch (NotFoundException e) {
        }
        return classPool;
    }

    /**
     * 回调
     *
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-01-05 21:16
     **/
    public interface Call<T> {
        void accept(T t) throws Exception;
    }

    @Getter
    public static class JavaAssist extends ClassLoader {

        private final CtClass ctClass;
        private final ClassPool classPool;

        private JavaAssist(ClassPool classPool, CtClass ctClass, ClassLoader classLoader) {
            super(classLoader);
            this.classPool = classPool;
            this.ctClass = ctClass;
        }

        public CtClass ctClass(String name) {
            try {
                return classPool.get(name);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void importPackage(Class<?>... packages) {
            for (Class<?> aPackage : packages) {
                classPool.importPackage(aPackage.getPackageName());
            }
        }

        public void importPackage(String... packages) {
            for (String aPackage : packages) {
                classPool.importPackage(aPackage);
            }
        }

        CtClass[] convert(Class[] methodParams) {
            CtClass[] ctClasses = new CtClass[methodParams.length];
            for (int i = 0; i < methodParams.length; i++) {
                Class methodParam = methodParams[i];
                CtClass ctClass1 = ctClass(methodParam.getName());
                ctClasses[i] = ctClass1;
            }
            return ctClasses;
        }

        /** 查询已有的方法 */
        public JavaAssist declaredMethod(String methodName, Class[] methodParams, Call<CtMethod> call) {
            CtClass[] convert = convert(methodParams);
            try {
                CtMethod ctMethod = ctClass.getDeclaredMethod(methodName, convert);
                call.accept(ctMethod);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 创建一个方法
         *
         * @param modifier     Modifier.PUBLIC
         * @param returnType   方法的返回类型
         * @param methodName   方法的名字
         * @param methodParams 方法参数类型
         * @param call         回调
         */
        public JavaAssist createMethod(int modifier, CtClass returnType,
                                       String methodName, Class[] methodParams,
                                       Call<CtMethod> call) {
            try {
                CtClass[] convert = convert(methodParams);
                CtMethod ctMethod = new CtMethod(returnType, methodName, convert, ctClass);
                ctMethod.setModifiers(modifier);
                call.accept(ctMethod);
                ctClass.addMethod(ctMethod);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /** 完整的方法字符串 */
        public JavaAssist createMethod(String methodStr) {
            try {
                CtMethod method = CtNewMethod.make(methodStr, ctClass);
                ctClass.addMethod(method);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public JavaAssist writeFile(String directoryName) {
            try {
                getCtClass().writeFile(directoryName);
                return this;
            } catch (CannotCompileException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] toBytes() {
            try {
                return getCtClass().toBytecode();
            } catch (CannotCompileException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        /** 通过 classloader 加载类 */
        public JavaAssist call(Call<Class<?>> call) {
            try {
                Class<?> aClass = loadClass();
                call.accept(aClass);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /** 通过 classloader 加载类 */
        public Class<?> loadClass() {
            try {
                return loadClass(ctClass.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                //return ctClass.toClass(getParent());
                byte[] b = ctClass.toBytecode();
                return defineClass(name, b, 0, b.length);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public <R> R toInstance() {
            try {
                return (R) loadClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** 查找某个类编辑 */
    public JavaAssist editClass(Class<?> clazz) {
        return editClass(clazz.getName(), Thread.currentThread().getContextClassLoader());
    }

    public JavaAssist editClass(Class<?> clazz, ClassLoader classLoader) {
        return editClass(clazz.getName(), classLoader);
    }

    /** 查找某个类编辑 */
    public JavaAssist editClass(String clazzName) {
        return editClass(clazzName, Thread.currentThread().getContextClassLoader());
    }

    public JavaAssist editClass(String clazzName, ClassLoader classLoader) {
        try {
            ClassPool classPool = build();
            CtClass tmp = classPool.get(clazzName);
            return new JavaAssist(classPool, tmp, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 继承某个类的实现 */
    public JavaAssist extendSuperclass(Class<?> superclass) {
        return extendSuperclass(superclass, superclass.getClassLoader());
    }

    public JavaAssist extendSuperclass(Class<?> superclass, ClassLoader classLoader) {
        try {
            ClassPool classPool = build();
            CtClass tmp = classPool.makeClass(superclass.getName() + "Impl" + ATOMIC_INTEGER.incrementAndGet());
            tmp.setSuperclass(classPool.get(superclass.getName()));
            return new JavaAssist(classPool, tmp, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 实现某个接口的 */
    public JavaAssist implInterfaces(String className, Class<?>... interfaces) {
        return implInterfaces(className, Thread.currentThread().getContextClassLoader(), interfaces);
    }

    public JavaAssist implInterfaces(String className, ClassLoader classLoader, Class<?>... interfaces) {
        try {
            ClassPool classPool = build();
            CtClass tmp = classPool.makeClass(className + "Impl" + ATOMIC_INTEGER.incrementAndGet());
            for (Class<?> aClass : interfaces) {
                tmp.addInterface(classPool.get(aClass.getName()));
            }
            return new JavaAssist(classPool, tmp, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 创建一个类 */
    public JavaAssist create(String className) {
        return create(className, Thread.currentThread().getContextClassLoader());
    }

    public JavaAssist create(String className, ClassLoader classLoader) {
        try {
            ClassPool classPool = build();
            CtClass tmp = classPool.makeClass(className + "Impl" + ATOMIC_INTEGER.incrementAndGet());
            return new JavaAssist(classPool, tmp, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
