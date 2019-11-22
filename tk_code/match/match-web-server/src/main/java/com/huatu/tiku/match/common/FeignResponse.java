package com.huatu.tiku.match.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-06 下午4:40
 **/
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FeignResponse<T> {


    private int code;
    private T data;

    public static <T> FeignResponse<T> newInstance(Object data){
        FeignResponse pushResponse = new FeignResponse(1000000, data);
        return pushResponse;
    }

}
