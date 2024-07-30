package code.assist;

import javassist.CtClass;
import javassist.Modifier;
import org.junit.Test;
import wxdgaming.boot.assist.JavaAssistBox;

import java.util.concurrent.atomic.AtomicReference;

/**
 * javassist 测试
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2023-12-06 20:07
 **/
public class JavaAssistBoxTest {


    @Test
    public void main() throws Exception {

        JavaAssistBox javaAssistBox = JavaAssistBox.of();
        JavaAssistBox.JavaAssist javaAssist = javaAssistBox
                .extendSuperclass(ICheck.class)
                .createMethod(Modifier.PUBLIC, CtClass.voidType, "println", new Class[]{AtomicReference.class, Object.class}, ctMethod -> {
                    ctMethod.setBody("System.out.println(\"impl\");");
                })
                .createMethod("""
                            public void println2(Object out,Object[] objs) {
                             java.util.concurrent.atomic.AtomicReference ar=(java.util.concurrent.atomic.AtomicReference)out; 
                                System.out.println("impl " + out);
                            }
                        }
                        """)
                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .writeFile("target/assist-out");

        javaAssistBox
                .editClass(ICheck.class)
                .declaredMethod("println", new Class[]{}, ctMethod -> {
                    ctMethod.setBody("System.out.println(\"impl\");");
                })
                .createMethod("""
                            public void println2() {
                                System.out.println("impl");
                            }
                        }
                        """)

                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .writeFile("target")
        ;

        javaAssistBox
                .create(ICheck.class.getName())
                .createMethod("""
                            public void p2() {
                                System.out.println("impl");
                            }
                        }
                        """)

                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .call(aClass -> {
                    System.out.println(aClass + " - " + aClass.hashCode());
                })
                .writeFile("target/assist-out");


        // createMethod(ICheck.class, CtClass.voidType, "println", new CtClass[]{}, ";", null).writeFile("target");
        // replaceMethodBody(ICheck.class, "println", new CtClass[]{}, ";", null).writeFile("target");
    }

}
