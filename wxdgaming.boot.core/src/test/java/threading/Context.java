package threading;

import com.sh.game.basic.collection.ConcurrentObjMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 本地线程变量
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-04-24 20:26
 **/
@Slf4j
@Getter
public class Context extends ConcurrentObjMap {

    private static final ThreadLocal<Context> local = new InheritableThreadLocal<>();

    /** 获取参数 */
    public static <T> T context(final Class<T> clazz) {
        return (T) context().get(clazz.getName());
    }

    /** 获取参数 */
    public static <T> T context(final Object name) {
        return (T) context().get(name);
    }

    /** put参数 */
    public static <T> T putContent(final Class<T> clazz) {
        try {
            T ins = clazz.getDeclaredConstructor().newInstance();
            putContentIfAbsent(ins);
            return ins;
        } catch (Exception e) {
            throw new RuntimeException(clazz.getName(), e);
        }
    }

    /** put参数 */
    public static <T> void putContent(final T ins) {
        context().put(ins.getClass().getName(), ins);
    }

    /** put参数 */
    public static <T> void putContent(final Object name, T ins) {
        context().put(name, ins);
    }

    /** put参数 */
    public static <T> void putContentIfAbsent(final T ins) {
        context().putIfAbsent(ins.getClass().getName(), ins);
    }

    /** put参数 */
    public static <T> void putContentIfAbsent(final Object name, T ins) {
        context().putIfAbsent(name, ins);
    }

    /** 获取参数 */
    public static Context context() {
        Context context = local.get();
        if (context == null) {
            context = new Context();
            local.set(context);
        }
        return context;
    }

    /** 设置参数 */
    public void set() {
        local.set(new Context());
    }

    /** 设置参数 */
    public void set(Context context) {
        local.set(context);
    }

    /** 清理缓存 */
    public static void cleanup() {
        local.remove();
    }

    /** 清理缓存 */
    public static void cleanup(Class<?> clazz) {
        context().remove(clazz.getName());
    }

    /** 清理缓存 */
    public static void cleanup(String name) {
        context().remove(name);
    }

    /** 清理缓存初始化的时候自动 clone 当前线程上下文 */
    public static class ContextRunnable implements Runnable {

        final Context context;
        private final Runnable task;

        public ContextRunnable(Runnable task) {
            this.task = task;
            context = new Context(context());
        }

        @Override public void run() {
            try {
                local.set(context);
                task.run();
            } finally {
                cleanup();
            }
        }
    }

    /** 清理缓存初始化的时候自动 clone 当前线程上下文 */
    public static abstract class ContextEvent implements Runnable {

        final Context context;

        public ContextEvent() {
            context = new Context(context());
        }

        @Override public void run() {
            try {
                local.set(context);
                onEvent();
            } finally {
                cleanup();
            }
        }

        public abstract void onEvent();

    }

    public Context() {
    }

    public Context(Map m) {
        super(m);
    }

}
