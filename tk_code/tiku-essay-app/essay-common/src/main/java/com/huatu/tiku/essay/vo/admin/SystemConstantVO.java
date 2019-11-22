package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhaoxi
 * @Description: 系统相关配置
 * @date 2018/12/272:24 PM
 */
@Builder
@AllArgsConstructor
@Data
public class SystemConstantVO {
    //key值
    private String key;
    //value
    private Object value;
    //描述
    private String desc;

}
