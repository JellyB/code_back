package com.base.exception;

/**
 * Created by junli on 2017/9/22.
 */
public class MyBaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MyBaseException(String message) {
        super(message);
    }

    public MyBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyBaseException(Throwable cause) {
        super(cause);
    }
}
