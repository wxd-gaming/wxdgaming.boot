package wxdgaming.boot.agent.exception;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-10-29 10:16
 **/
public class Throw extends RuntimeException implements Serializable {

    public static final StackTraceElement[] StackTraceEmpty = new StackTraceElement[0];

    public static RuntimeException of(Throwable throwable) {
        return of(null, throwable);
    }

    public static RuntimeException of(String msg, Throwable throwable) {

        String message = null;

        if (msg != null && !msg.isBlank()) {
            message = msg;
        }

        String throwableMessage;
        if (throwable instanceof Throw) {
            throwableMessage = throwable.getLocalizedMessage();
        } else {
            throwableMessage = throwable.toString();
        }

        if (throwableMessage != null && !throwableMessage.isBlank()) {
            if (message != null) message = message + ", " + throwableMessage;
            else message = throwableMessage;
        }

        return new Throw(message, throwable);
    }

    public static String ofString(Throwable throwable) {
        return ofString(throwable, true);
    }

    public static String ofString(Throwable throwable, boolean appendLine) {
        StringBuilder stringBuilder = new StringBuilder();
        ofString(stringBuilder, throwable, appendLine);
        return stringBuilder.toString();
    }

    /**
     * 处理错误日志的堆栈信息
     *
     * @param stringBuilder 带出
     * @param throwable     需要处理的异常
     * @param appendLine    是否使用换行符
     */
    public static void ofString(StringBuilder stringBuilder, Throwable throwable, boolean appendLine) {
        if (throwable != null) {

            ofString(stringBuilder, throwable.getCause(), appendLine);

            if (appendLine) stringBuilder.append("\n");
            else stringBuilder.append("=>");

            if (!Throw.class.equals(throwable.getClass())) {
                stringBuilder.append(throwable.getClass().getName());
            } else {
                stringBuilder.append(RuntimeException.class.getName());
            }
            stringBuilder.append(": ");
            if (throwable.getMessage() != null && !throwable.getMessage().isEmpty()) {
                stringBuilder.append(throwable.getMessage());
            } else {
                stringBuilder.append("null");
            }
            if (appendLine) stringBuilder.append("\n");
            else stringBuilder.append("=>");
            StackTraceElement[] stackTraces = throwable.getStackTrace();
            ofString(stringBuilder, stackTraces, appendLine);
            if (appendLine)
                stringBuilder.append("-----------------------------------------------------------------------------");
        }
    }

    public static void ofString(StringBuilder stringBuilder, StackTraceElement[] stackTraces, boolean appendLine) {
        for (StackTraceElement e : stackTraces) {
            if (appendLine)
                stringBuilder.append("    at ");
            ofString(stringBuilder, e);
            if (appendLine) stringBuilder.append("\n");
            else stringBuilder.append("=>");
        }
    }

    public static String ofString(StackTraceElement traceElement) {
        StringBuilder stringBuilder = new StringBuilder();
        ofString(stringBuilder, traceElement);
        return stringBuilder.toString();
    }

    public static void ofString(StringBuilder stringBuilder, StackTraceElement traceElement) {
        stringBuilder.append(traceElement.getClassName()).append("#").append(traceElement.getMethodName())
                .append("(").append(traceElement.getFileName()).append(":").append(traceElement.getLineNumber()).append(")");
    }

    /**
     * 代理的异常过滤掉
     *
     * @param throwable
     * @return
     */
    public static Throwable filterInvoke(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            if (throwable.getCause() != null) {
                return filterInvoke(throwable.getCause());
            }
        }
        return throwable;
    }

    /**
     * 判断异常是否是线程终止异常，如果是继续终止线程
     *
     * @param throwable
     * @return
     */
    public static boolean isInterruptedException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return true;
        }
        return isInterruptedException(throwable.getCause());
    }

    public static StackTraceElement[] revertStackTrace(StackTraceElement[] sts) {
        if (sts != null && sts.length > 0) {
            int len = sts.length;
            if (len > 3) {
                len = 3;
            }
            StackTraceElement[] stackTraces = new StackTraceElement[len];
            System.arraycopy(sts, 0, stackTraces, 0, len);
            return stackTraces;
        }
        return StackTraceEmpty;
    }

    static String message(String msg, Throwable throwable) {
        String throwableName;
        if (throwable.getClass().equals(Throw.class)) {
            throwableName = throwable.getMessage();
        } else {
            throwableName = throwable.getClass().getName() + ", " + throwable.getMessage();
        }
        if (!(msg == null || msg.isBlank())) {
            throwableName += ", " + msg;
        }
        return throwableName;
    }

    public Throw(String message) {
        super(message);
    }

    public Throw(String message, Throwable throwable) {
        super(message, throwable.getCause(), false, true);
        this.setStackTrace(throwable.getStackTrace());
    }

    @Override public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
