package com.huatu.ztk.commons;

/**
 * Created by shaojieyue on 10/18/15.
 */
public class JacksonDeserializeException extends RuntimeException {
    public JacksonDeserializeException() {
    }

    public JacksonDeserializeException(String message) {
        super(message);
    }

    public JacksonDeserializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JacksonDeserializeException(Throwable cause) {
        super(cause);
    }

    public JacksonDeserializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
