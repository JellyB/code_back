package com.huatu.tiku.interview.exception;

import com.huatu.tiku.interview.constant.ResultEnum;
import lombok.Data;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/15 9:46
 * @Description
 */
@Data
public class ReqException extends RuntimeException{
    private String message;
    private Integer code;

    public ReqException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.message = resultEnum.getMessage();
        this.code = resultEnum.getCode();
    }
}
