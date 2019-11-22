package com.huatu.ztk.question.exception;

/**
 * 非法的试题异常
 * Created by shaojieyue
 * Created time 2016-05-20 13:49
 */
public class IllegalQuestionException extends Exception{
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public IllegalQuestionException(String message) {
        super(message);
    }
}
