package org.wxd.boot.core.lang;


import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-12-26 10:22
 **/
public class LoggerException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    public LoggerException() {
        this("日志记录");
    }

    public LoggerException(String message) {
        super(message);
    }

    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoggerException(Throwable cause) {
        super(cause);
    }

    public LoggerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
