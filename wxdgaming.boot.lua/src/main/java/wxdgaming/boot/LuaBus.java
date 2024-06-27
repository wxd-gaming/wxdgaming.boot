package wxdgaming.boot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.lang.Record2;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * lua 脚本 加载器 执行器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-06-27 10:10
 **/
@Slf4j
public class LuaBus {

    @Getter private static final ConcurrentHashMap<String, Object> lua_data = new ConcurrentHashMap<>();
    @Getter @Setter private static LuaBus ins = null;

    public static LuaBus buildFromResources(ClassLoader classLoader, String package_name) {
        LuaBus luaBus = new LuaBus();
        luaBus.loadResources(classLoader, package_name);
        return luaBus;
    }

    public static LuaBus buildFromDirs(String dir) {
        LuaBus luaBus = new LuaBus();
        luaBus.loadDirs(dir);
        return luaBus;
    }

    @Getter private final Globals globals;

    LuaBus() {
        globals = JsePlatform.standardGlobals();
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
        return globals.load(luaString).call();
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
        return globals.loadfile(lua_file).call();
    }

    /**
     * 通过文件夹加载 .lua .LUA
     *
     * @param dir 文件夹
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 15:52
     */
    public LuaBus loadDirs(String dir) {
        FileUtil.walkFiles(dir, ".lua", ".LUA")
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
    public LuaBus loadResources(ClassLoader classLoader, String package_name) {
        try {
            Stream<Record2<String, InputStream>> record2Stream = FileUtil.resourceStreams(classLoader, package_name);
            record2Stream.forEach(item -> {
                if (item.t1().endsWith(".lua") || item.t1().endsWith(".LUA")) {
                    if (log.isDebugEnabled()) {
                        log.debug("find lua script {}", item.t1());
                    }
                    String lua_script = FileReadUtil.readString(item.t2());
                    loadString(lua_script);
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
    public LuaBus set(String key, int value) {
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
    public LuaBus set(String key, String value) {
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
    public LuaBus set(String key, Object value) {
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
        return globals.get(lua_key).call();/*方法名称*/
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
