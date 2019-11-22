package com.huatu.tiku.teacher.notice.constant;

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
public class PushResponse<T> {


    private int code;
    private T data;

    public static <T> PushResponse<T> newInstance(Object data){
        PushResponse pushResponse = new PushResponse(1000000, data);
        return pushResponse;
    }

}
