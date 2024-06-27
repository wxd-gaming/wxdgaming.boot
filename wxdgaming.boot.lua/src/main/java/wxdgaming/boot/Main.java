package wxdgaming.boot;

import org.luaj.vm2.LuaValue;

import java.util.concurrent.CompletableFuture;

public class Main {

    protected static LuaBus luaBus = null;

    public static void main(String[] args) throws Exception {
        System.out.println(System.currentTimeMillis());
        luaBus = LuaBus.build(Thread.currentThread().getContextClassLoader(), "script/");
        luaBus.set("objVar", 1);
        Thread.sleep(1000);
        testString();
        testFile();
        Thread.sleep(10000);
    }

    public static void testString() throws Exception {
        LuaValue chunk = luaBus.getGlobals().load("print(\"holle world \" .. tostring(os.time()))");
        for (int i = 0; i < 1; i++) {
            CompletableFuture.runAsync(() -> chunk.call())
                    .exceptionally(ex -> {
                        ex.printStackTrace(System.out);
                        return null;
                    });
        }

    }

    public static void testFile() throws Exception {

        for (int i = 0; i < 50; i++) {
            luaBus.exec("t2", i + " - " + String.valueOf(System.currentTimeMillis()));
            luaBus.execUserdataAsync("t3", new M(i));
        }

    }

    public static class M {

        private final int i;

        public M(int i) {
            this.i = i;
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