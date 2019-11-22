package com.huatu.ztk.commons;

/**
 *
 * Created by shaojieyue on 10/18/15.
 */
public class JacksonSerializeException extends RuntimeException {
    public JacksonSerializeException() {
    }

    public JacksonSerializeException(String message) {
        super(message);
    }

    public JacksonSerializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JacksonSerializeException(Throwable cause) {
        super(cause);
    }

    public JacksonSerializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
