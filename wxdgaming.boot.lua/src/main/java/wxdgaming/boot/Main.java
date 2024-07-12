package wxdgaming.boot;

import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class Main {

    protected static LuaBus luaBus = null;

    static int forCount = 5000;

    public interface T {
        int t1 = 1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.currentTimeMillis() + " - " + System.getProperty("user.dir"));
        // luaBus = LuaBus.buildFromResources(Thread.currentThread().getContextClassLoader(), "script/");
        luaBus = LuaBus.buildFromDirs("src/main/lua");
        luaBus.set("objVar", 1);
        /*注册函数*/
        luaBus.set("testfun0", new VarArgFunction() {

            @Override public Varargs onInvoke(Varargs args) {
                /*参数的数量*/
                int narg = args.narg();
                System.out.println(narg + " - " + args.toString());
                return LuaValue.valueOf("无参");
            }
        });
        /*注册函数*/
        luaBus.set("testfun1", new VarArgFunction() {

            @Override public Varargs onInvoke(Varargs args) {
                /*参数的数量*/
                int narg = args.narg();
                LuaValue checkvalue = args.checkvalue(1);
                System.out.println(narg + " - " + args.toString());
                return LuaValue.valueOf("testfun1");
            }
        });

        /*注册函数*/
        luaBus.set("testfun2", new VarArgFunction() {

            @Override public Varargs onInvoke(Varargs args) {
                /*参数的数量*/
                int narg = args.narg();
                LuaValue v1 = args.checkvalue(1);
                LuaValue v2 = args.checkvalue(2);
                System.out.println(narg + " - " + args.toString());
                return LuaValue.valueOf("testfun2");
            }
        });

        Thread.sleep(500);
        luaBus.forExecTry("t1");
        Thread.sleep(500);
        testString();


        test("t3");
        Thread.sleep(3000);
        test("t3");
        Thread.sleep(3000);
        test("t4");
        Thread.sleep(3000);
        test("t4");
        Thread.sleep(3000);
        test("t2_2");
        Thread.sleep(3000);
        test("t2_2");
        Thread.sleep(3000);

    }

    public static void testString() throws Exception {
        LuaValue chunk = luaBus.globalPoolNew("main")
                .loadString("print(\"holle world \" .. tostring(os.time()))");
        for (int i = 0; i < 1; i++) {
            CompletableFuture.runAsync(() -> chunk.call())
                    .exceptionally(ex -> {
                        ex.printStackTrace(System.out);
                        return null;
                    });
        }

    }

    public static void test(String method_name) throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(forCount);
        long l = System.currentTimeMillis();
        for (int i = 0; i < forCount; i++) {
            // luaBus.exec("t2", i + " - " + String.valueOf(System.currentTimeMillis()));
            luaBus.execUserdataAsync(method_name, new M(i))
                    .thenApply((value) -> {
                        // log.info("{}", value);
                        countDownLatch.countDown();
                        return null;
                    });
        }
        countDownLatch.await();
        long l1 = System.currentTimeMillis() - l;
        System.out.println("执行：" + method_name + ", " + forCount + " 次, 耗时：" + l1 + " ms");
    }

    public static class M {

        private final int i;

        public M(int i) {
            this.i = i;
        }

        public void log_info(String msg) {
            log.info(msg);
        }

        public int index() {
            return i;
        }

        public String gString() {
            // return LuaValue.valueOf("g1");
            return "gString";
        }

        public LuaValue gStringValue() {
            return LuaValue.valueOf("gStringValue");
        }

        public LuaValue gValue() {
            return LuaValue.listOf(
                    new LuaValue[]{
                            LuaValue.valueOf("gValue"),
                            LuaValue.valueOf(i)
                    }
            );
        }

    }

}