package com.huatu.tiku.position.base.exception;

/**未登录异常
 * @author wangjian
 **/
public class NoLoginException extends RuntimeException {
    private static final long serialVersionUID = -7269224994156636478L;

    public NoLoginException(String message) {
        super(message);
    }
}
