package com.arj.monitor.exception;


import java.io.Serializable;


/**
 * @author zhouwei
 * @Description: 返回结果抽象类
 * @create 2018-10-15 上午11:49
 **/
public interface Result extends Serializable {
    int SUCCESS_CODE=1000000;
    /**
     * 获取结果code
     * @return
     */
    public int getCode();
}
