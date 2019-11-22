package com.huatu.one.base.exception;

import com.huatu.common.ISystemStatusEnum;

/**
 * Created by duanxiangchao on 2018/6/1
 */
public enum TikuSystemStatusEnum implements ISystemStatusEnum {


    SUCCESS(100000, "成功", "");


    /**
     * @description: 参数格式化枚举
     * @author duanxiangchao
     * @date 2018/6/1 上午10:11
     */
    public static TikuSystemStatusEnum create(TikuSystemStatusEnum systemStatus,
                                              Object... params) {
        systemStatus.setMessage(String.format(systemStatus.getSourceMessage(), params));
        return systemStatus;
    }

    private Integer code;
    private String message;
    private String sourceMessage;

    TikuSystemStatusEnum(Integer code, String desc, String sourceMessage) {
        this.code = code;
        this.message = desc;
        this.sourceMessage = sourceMessage;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
    }
}
