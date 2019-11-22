package com.huatu.tiku.position.base.exception;

/**未授权异常
 * @author wangjian
 **/
public class NoAuthException extends RuntimeException {
    private static final long serialVersionUID = -8355280153725198075L;

    public NoAuthException(String message) {
        super(message);
    }
}
