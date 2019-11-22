package com.huatu.one.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户检测
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCheckVo {

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 权限列表
     */
    private Integer[] menuIndexes;
}
