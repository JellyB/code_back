package com.arj.monitor.exception;


/**
 * @author zhouwei
 * @Description: 返回成功提示信息
 * @create 2018-10-15 上午12:04
 **/
public class SuccessMessage implements Result {
    private static final String DEFAULT_MESSAGE = "成功";
    private String message;

    SuccessMessage(String message) {
        this.message = message;
    }

    public static final SuccessMessage create(){
        return create(DEFAULT_MESSAGE);
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
