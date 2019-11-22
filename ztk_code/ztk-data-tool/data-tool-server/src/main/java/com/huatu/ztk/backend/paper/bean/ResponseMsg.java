package com.huatu.ztk.backend.paper.bean;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2017-12-25 下午5:54
 **/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseMsg<T> {
    private int code;
    private String message;
    private T data;

    public static ResponseMsg success(Object data) {
        return ResponseMsg.builder().code(200).message("").data(data).build();
    }

    public static ResponseMsg success() {
        return ResponseMsg.builder().code(200).message("").data(null).build();
    }

    public static ResponseMsg error(Object data, int code, String msg) {
        return ResponseMsg.builder().code(code).message(msg).data(data).build();
    }

    public static ResponseMsg error(Object data, int code) {
        return error(data, code, "lol");
    }

    public static ResponseMsg error(int code) {
        return error(null, code);
    }

    public static ResponseMsg error(int code, String msg) {
        return error(null, code, msg);
    }

}