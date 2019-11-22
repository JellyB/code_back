package com.ht.base.common;


import java.io.Serializable;

/**
 * Response 返回结果抽象类
 * Created by shaojieyue
 * Created time 2016-04-18 11:32
 */
public interface Result extends Serializable {
    int SUCCESS_CODE=1000000;
    /**
     * 获取结果code
     * @return
     */
    public int getCode();
}
