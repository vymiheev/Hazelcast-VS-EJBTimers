package ru.grfc.crashtest.cluster.timer;

/**
 * Created by mvj on 02.12.2016.
 */
public class DefaultTimerException extends Exception {
    private static final long serialVersionUID = 7129048240492163741L;

    public DefaultTimerException() {
        super();
    }

    public DefaultTimerException(String message) {
        super(message);
    }

    public DefaultTimerException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefaultTimerException(Throwable cause) {
        super(cause);
    }

    protected DefaultTimerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
