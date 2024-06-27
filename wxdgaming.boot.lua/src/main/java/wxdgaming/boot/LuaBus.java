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
import java.util.stream.Stream;

/**
 * lua 脚本 加载器 执行器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-06-27 10:10
 **/
@Slf4j
public class LuaBus {

    @Getter @Setter private static LuaBus ins = null;

    public static LuaBus build(ClassLoader classLoader, String package_name) {
        LuaBus luaBus = new LuaBus();
        luaBus.loadResources(classLoader, package_name);
        return luaBus;
    }

    @Getter private final Globals globals = JsePlatform.standardGlobals();

    LuaBus() {}

    public LuaBus loadString(String luaString) {
        globals.load(luaString);
        return this;
    }

    public LuaBus loadFile(String lua_file) {
        globals.loadfile(lua_file);
        return this;
    }

    public LuaBus loadResources(ClassLoader classLoader, String package_name) {
        try {
            Stream<Record2<String, InputStream>> record2Stream = FileUtil.resourceStreams(classLoader, package_name);
            record2Stream.forEach(item -> {
                if (item.t1().endsWith(".lua") || item.t1().endsWith(".LUA")) {
                    if (log.isDebugEnabled()) {
                        log.debug("find lua script {}", item.t1());
                    }
                    String lua_script = FileReadUtil.readString(item.t2());
                    globals
                            .load(lua_script)/*加载文件*/
                            .call()/*执行*/;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("package_name = " + package_name, e);
        }
        return this;
    }

    public LuaBus set(String key, int value) {
        globals.set(key, value);
        return this;
    }

    public LuaBus set(String key, String value) {
        globals.set(key, value);
        return this;
    }

    public LuaBus set(String key, Object value) {
        globals.set(key, CoerceJavaToLua.coerce(value));
        return this;
    }

    /**
     * 查找方法
     *
     * @param method_name 方法名称
     * @return
     * @author: Troy.Chen(無心道, 15388152619)
     * @version: 2024-06-27 10:17
     */
    public LuaValue get(String method_name) {
        return globals.get(method_name).call();/*方法名称*/
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
