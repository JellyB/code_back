package com.ht.base.dto;

import com.ht.base.common.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhengyi
 * @date 2018/9/7 6:25 PM
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData implements Result {
    private Object data;
    private Integer code;
    private Object message;

    @Override
    public int getCode() {
        return this.code;
    }
}