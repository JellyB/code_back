package com.huatu.tiku.interview.entity.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:31
 * @Description
 */
@Data
public class ResultState implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1692432930341768342L;

    private int errcode; // 状态

    private String errmsg; //信息

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}