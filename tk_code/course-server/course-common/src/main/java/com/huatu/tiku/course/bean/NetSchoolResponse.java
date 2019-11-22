package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.beans.ImmutableBean;

/**
 * @author hanchao
 * @date 2017/8/25 18:40
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class NetSchoolResponse<T> {
    /**
     * mock
     */
    public static final NetSchoolResponse DEFAULT = (NetSchoolResponse) ImmutableBean.create(new NetSchoolResponse(0,"",null));

    /**
     * 上面可能引起客户端直接崩溃
     */
    public static final NetSchoolResponse DEFAULT_ERROR = (NetSchoolResponse) ImmutableBean.create(new NetSchoolResponse(-1000,"服务器繁忙",null));


    private int code;
    private String msg;
    private T data;


    public static <T> NetSchoolResponse<T> newInstance(Object data){
        NetSchoolResponse netSchoolResponse = new NetSchoolResponse(1,"",data);
        return netSchoolResponse;
    }
}
