package wxdgaming.boot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;
import wxdgaming.boot.agent.function.Predicate2;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * lua 脚本 加载器 执行器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-06-27 10:10
 **/
@Slf4j
@Getter
public class LuaBus {

    @Getter private static final ConcurrentHashMap<String, Object> lua_data = new ConcurrentHashMap<>();

    public static LuaBus buildFromResources(ClassLoader classLoader, String package_name) {
        LuaBus luaBus = new LuaBus();
        return luaBus;
    }

    /**
     * 规则目录下面
     * --script
     * --------模块1
     * --------模块2
     * --------模块3
     * --util 公共脚本
     *
     * @param base_dir 主目录
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-28 17:33
     */
    public static LuaBus buildFromDirs(String base_dir) {
        LuaBus luaBus = new LuaBus();

        File script_path = new File(base_dir + "/script");

        FileUtil.walkDirs(script_path.getPath(), 1).forEach(dir -> {
            if (dir.equals(script_path)) return;
            System.out.println("加载功能模块目录：" + dir + " - " + dir.getName());
            GlobalPool globalPool = luaBus.new GlobalPool(dir.getName());
            globalPool.loadDirs(dir.getPath(), 99);
            globalPool.loadDirs(base_dir + "/util", 99);
            luaBus.globalPools.put(globalPool.getName(), globalPool);
        });

        return luaBus;
    }

    private final HashMap<String, GlobalPool> globalPools = new HashMap<>();

    public GlobalPool globalPool(String name) {
        return globalPools.get(name);
    }

    public GlobalPool globalPoolNew(String name) {
        return globalPools.computeIfAbsent(name, l -> new GlobalPool(name));
    }

    public void forExec(String method) {
        forExec(method, null);
    }

    public void forExec(String method, BiConsumer<String, LuaValue> consumer) {
        for (Map.Entry<String, GlobalPool> poolEntry : globalPools.entrySet()) {
            LuaValue luaValue = poolEntry.getValue().get(method);
            if (luaValue != null) {
                LuaValue callLuaValue = luaValue.call();
                if (consumer != null) {
                    consumer.accept(poolEntry.getKey(), callLuaValue);
                }
            }
        }
    }

    public void forExecTry(String method) {
        forExecTry(method, null);
    }

    public void forExecTry(String method, BiConsumer<String, LuaValue> consumer) {
        predicateTry(method, (name, luaValue) -> {
            LuaValue ret = luaValue.call();
            if (consumer != null) {
                consumer.accept(name, ret);
            }
            return false;
        });
    }

    public void forExecTry(String method, String data, BiConsumer<String, LuaValue> consumer) {
        runTry(method, LuaValue.valueOf(data), consumer);
    }

    public void forExecTry(String method, Object data, BiConsumer<String, LuaValue> consumer) {
        runTry(method, CoerceJavaToLua.coerce(data), consumer);
    }

    /**
     * @param method   方法名字
     * @param data     数据
     * @param consumer
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-07-29 10:51
     */
    public void runTry(String method, LuaValue data, BiConsumer<String, LuaValue> consumer) {
        predicateTry(method, (name, luaValue) -> {
            LuaValue ret = luaValue.call(data);
            if (consumer != null) {
                consumer.accept(name, ret);
            }
            return false;
        });
    }


    public void predicateTry(String method, Predicate2<String, LuaValue> exec) {
        for (Map.Entry<String, GlobalPool> poolEntry : globalPools.entrySet()) {
            LuaValue luaValue = poolEntry.getValue().get(method);
            if (luaValue != null && !luaValue.isnil()) {
                try {
                    if (exec.test(poolEntry.getKey(), luaValue)) {
                        return;
                    }
                } catch (Exception e) {
                    log.error("name={}, method={}", poolEntry.getKey(), method, e);
                }
            }
        }
    }

    @Getter
    public final class GlobalPool {

        private final String name;
        private final Globals globals;

        protected GlobalPool(String name) {
            this.name = name;
            this.globals = JsePlatform.standardGlobals();
            LuaJC.install(this.globals);
            set("logbackUtil", log);
            set("lua_data", lua_data);
        }

        /**
         * 从 lua 字符加载
         *
         * @param luaString
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:11
         */
        public LuaValue loadString(String luaString) {
            return globals.load(luaString);
        }

        /**
         * 从 lua 字符加载
         *
         * @param luaString 脚本字符
         * @param chunkname 别名，报错的时候标记，
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:11
         */
        public LuaValue loadString(String luaString, String chunkname) {
            return globals.load(luaString, chunkname);
        }

        /**
         * 从文件加载lua
         *
         * @param lua_file lua文件路径
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:11
         */
        public LuaValue loadFile(String lua_file) {
            if (log.isDebugEnabled()) {
                log.debug("find lua script {}", lua_file);
            }
            String string = FileReadUtil.readString(lua_file);
            /* chunkname 是别名 用于标记那一段代码出错*/
            return loadString(string, lua_file).call();
        }

        /**
         * 通过文件夹加载 .lua .LUA
         *
         * @param dir 文件夹
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 15:52
         */
        public GlobalPool loadDirs(String dir, int maxDepth) {
            FileUtil.walkFiles(dir, maxDepth, ".lua", ".LUA")
                    .forEach(lua_file -> loadFile(lua_file.getPath()));
            return this;
        }

        /**
         * 从 jar 包资源文件加载
         *
         * @param classLoader  指定 classloader
         * @param package_name 指定加载的目录
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:12
         */
        public GlobalPool loadResources(ClassLoader classLoader, String package_name) {
            try {
                Stream<Record2<String, InputStream>> record2Stream = FileUtil.resourceStreams(classLoader, package_name);
                record2Stream.forEach(item -> {
                    if (item.t1().endsWith(".lua") || item.t1().endsWith(".LUA")) {
                        if (log.isDebugEnabled()) {
                            log.debug("find lua script {}", item.t1());
                        }
                        String lua_script = FileReadUtil.readString(item.t2());
                        loadString(lua_script).call();
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("package_name = " + package_name, e);
            }
            return this;
        }

        /**
         * 设置全局变量
         *
         * @param key   存储变量名
         * @param value 变量的值
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:13
         */
        public GlobalPool set(String key, int value) {
            globals.set(key, value);
            return this;
        }

        /**
         * 设置全局变量
         *
         * @param key   存储变量名
         * @param value 变量的值
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:13
         */
        public GlobalPool set(String key, String value) {
            globals.set(key, value);
            return this;
        }

        /**
         * 设置全局变量
         *
         * @param key   存储变量名
         * @param value 变量的值
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 16:13
         */
        public GlobalPool set(String key, Object value) {
            globals.set(key, CoerceJavaToLua.coerce(value));
            return this;
        }

        /**
         * 查找 lua 虚拟机的对象
         *
         * @param lua_key 查找的 key值 或者 方法名称
         * @return
         * @author: Troy.Chen(無心道, 15388152619)
         * @version: 2024-06-27 10:17
         */
        public LuaValue get(String lua_key) {
            return globals.get(lua_key);/*方法名称*/
        }

        public LuaValue exec(String method_name) {
            return globals.get(method_name);/*方法名称*/
        }

        public LuaValue exec(String method_name, String val1) {
            return globals.get(method_name).call(LuaValue.valueOf(val1));/*方法名称*/
        }

        public LuaValue execUserdata(String method_name, Object val1) {
            return globals.get(method_name).call(CoerceJavaToLua.coerce(val1));/*方法名称*/
        }

        public CompletableFuture<LuaValue> execAsync(String method_name) {
            return CompletableFuture.supplyAsync(() -> exec(method_name))
                    .exceptionally(ex -> {
                        log.error("", ex);
                        return null;
                    });
        }

        public CompletableFuture<LuaValue> execAsync(String method_name, String val1) {
            return CompletableFuture.supplyAsync(() -> exec(method_name, val1))
                    .exceptionally(ex -> {
                        log.error("", ex);
                        return null;
                    });
        }

        public CompletableFuture<LuaValue> execUserdataAsync(String method_name, Object val1) {
            return CompletableFuture.supplyAsync(() -> execUserdata(method_name, val1))
                    .exceptionally(ex -> {
                        log.error("", ex);
                        return null;
                    });
        }
    }

    /**
     * 设置全局变量
     *
     * @param key   存储变量名
     * @param value 变量的值
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 16:13
     */
    public LuaBus set(String key, int value) {
        globalPools.values().forEach(g -> g.set(key, value));
        return this;
    }

    /**
     * 设置全局变量
     *
     * @param key   存储变量名
     * @param value 变量的值
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 16:13
     */
    public LuaBus set(String key, String value) {
        globalPools.values().forEach(g -> g.set(key, value));
        return this;
    }

    /**
     * 设置全局变量
     *
     * @param key   存储变量名
     * @param value 变量的值
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 16:13
     */
    public LuaBus set(String key, Object value) {
        globalPools.values().forEach(g -> g.set(key, CoerceJavaToLua.coerce(value)));
        return this;
    }

    /**
     * 设置全局变量
     *
     * @param key              存储变量名
     * @param valueArgFunction 变量的值
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 16:13
     */
    public LuaBus set(String key, VarArgFunction valueArgFunction) {
        globalPools.values().forEach(g -> g.set(key, valueArgFunction));
        return this;
    }

    /**
     * 查找 lua 虚拟机的对象
     *
     * @param lua_key 查找的 key值 或者 方法名称
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 10:17
     */
    public LuaValue get(String lua_key) {
        for (GlobalPool value : globalPools.values()) {
            LuaValue luaValue = value.get(lua_key);
            if (luaValue != null) {
                return luaValue;
            }
        }
        return null;
    }

    public LuaValue exec(String method_name) {
        LuaValue luaValue = get(method_name);
        if (luaValue != null) {
            return luaValue.call();
        }
        throw new NullPointerException("method_name=" + method_name);
    }

    public LuaValue exec(String method_name, String val1) {
        LuaValue luaValue = get(method_name);
        if (luaValue != null) {
            return luaValue.call(val1);
        }
        throw new NullPointerException("method_name=" + method_name);
    }

    public LuaValue exec(String method_name, Object... params) {
        LuaValue luaValue = get(method_name);
        if (luaValue != null) {
            LuaValue[] luaValues = convert(params);
            Varargs invoke = luaValue.invoke(luaValues);
            if (invoke != null && invoke != LuaValue.NIL && invoke.narg() > 0) {
                return invoke.arg1();
            }
            return null;
        }
        throw new NullPointerException("method_name=" + method_name);
    }

    /**
     * @param method   需要调用的方法
     * @param consumer 执行回调
     * @param params   具体参数
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-07-29 10:55
     */
    public void forExec(String method, BiConsumer<String, LuaValue> consumer, Object... params) {
        LuaValue[] luaValues = convert(params);
        for (Map.Entry<String, GlobalPool> poolEntry : globalPools.entrySet()) {
            LuaValue luaValue = poolEntry.getValue().get(method);
            if (luaValue != null) {
                Varargs invoke = luaValue.invoke(luaValues);
                LuaValue ret = null;
                if (invoke != null && invoke != LuaValue.NIL && invoke.narg() > 0) {
                    ret = invoke.arg1();
                }
                consumer.accept(poolEntry.getKey(), ret);
            }
        }
    }

    public LuaValue execUserdata(String method_name, Object val1) {
        LuaValue luaValue = get(method_name);
        if (luaValue != null) {
            return luaValue.call(CoerceJavaToLua.coerce(val1));
        }
        throw new NullPointerException("method_name=" + method_name);
    }

    public CompletableFuture<LuaValue> execAsync(String method_name) {
        return CompletableFuture.supplyAsync(() -> exec(method_name))
                .exceptionally(ex -> {
                    log.error("", ex);
                    return null;
                });
    }

    public CompletableFuture<LuaValue> execAsync(String method_name, String val1) {
        return CompletableFuture.supplyAsync(() -> exec(method_name, val1))
                .exceptionally(ex -> {
                    log.error("", ex);
                    return null;
                });
    }

    public CompletableFuture<Void> execUserdataAsync(String method_name, Object val1) {
        return CompletableFuture.runAsync(() -> {
                    forExecTry(method_name, val1, null);
                })
                .exceptionally(ex -> {
                    log.error("", ex);
                    return null;
                });
    }

    public LuaValue[] convert(Object... params) {
        LuaValue[] luaValues = new LuaValue[params.length];
        for (int i = 0; i < params.length; i++) {
            luaValues[i] = CoerceJavaToLua.coerce(params[i]);
        }
        return luaValues;
    }

}
