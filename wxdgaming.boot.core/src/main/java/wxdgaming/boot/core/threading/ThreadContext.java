package wxdgaming.boot.core.threading;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.collection.concurrent.ConcurrentObjMap;

import java.util.Map;

/**
 * 本地线程变量
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-04-24 20:26
 **/
@Slf4j
@Getter
public class ThreadContext extends ConcurrentObjMap {

    private static final ThreadLocal<ThreadContext> local = new InheritableThreadLocal<>();

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
    public static ThreadContext context() {
        ThreadContext threadContext = local.get();
        if (threadContext == null) {
            threadContext = new ThreadContext();
            local.set(threadContext);
        }
        return threadContext;
    }

    /** 设置参数 */
    public void set() {
        local.set(new ThreadContext());
    }

    /** 设置参数 */
    public void set(ThreadContext threadContext) {
        local.set(threadContext);
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

        private final ThreadContext threadContext;
        private final Runnable task;

        public ContextRunnable(Runnable task) {
            this.task = task;
            threadContext = new ThreadContext(context());
        }

        @Override public void run() {
            try {
                local.set(threadContext);
                task.run();
            } finally {
                cleanup();
            }
        }
    }

    /** 清理缓存初始化的时候自动 clone 当前线程上下文 */
    public static abstract class ContextEvent implements Runnable {

        final ThreadContext threadContext;

        public ContextEvent() {
            threadContext = new ThreadContext(context());
        }

        @Override public void run() {
            try {
                local.set(threadContext);
                onEvent();
            } finally {
                cleanup();
            }
        }

        public abstract void onEvent();

    }

    public ThreadContext() {
    }

    public ThreadContext(Map m) {
        super(m);
    }

}
