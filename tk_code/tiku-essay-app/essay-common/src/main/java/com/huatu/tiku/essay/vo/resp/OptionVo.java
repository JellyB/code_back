package com.huatu.tiku.essay.vo.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by duanxiangchao on 2019/7/15
 */
@Data
public class OptionVo implements Serializable {

    private static final long serialVersionUID = 6253722691900462387L;

    /**
     * 值
     */
    private Object value;

    /**
     * 显示内容
     */
    private String text;

    /**
     * 是否选中
     */
    private Boolean checked = false;

    /**
     * 默认分数
     */
    private Integer defaultScore;

}
