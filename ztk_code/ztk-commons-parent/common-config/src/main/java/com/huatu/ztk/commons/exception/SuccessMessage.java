package com.huatu.ztk.commons.exception;

/**
 * 返回成功提示信息
 * Created by shaojieyue
 * Created time 2016-06-06 15:49
 */
public class SuccessMessage implements Result{
    private static final long serialVersionUID = 1L;
    private static final int SUCCESS_CODE=1000000;
    private String message;

    SuccessMessage(String message) {
        this.message = message;
    }

    /**
     * 创建一个返回成功信息的对象
     * @param message
     * @return
     */
    public static final SuccessMessage create(String message){
        //判断是否为空
        if (message == null || message.trim().equals("")) {
            throw new RuntimeException("message is blank. message="+message);
        }
        return new SuccessMessage(message);
    }

    /**
     * 获取结果code
     *
     * @return
     */
    @Override
    public int getCode() {
        return SUCCESS_CODE;
    }

    public String getMessage() {
        return message;
    }
}
