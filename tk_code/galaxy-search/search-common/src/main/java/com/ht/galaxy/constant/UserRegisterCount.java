package com.ht.galaxy.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author jbzm
 * @date Create on 2018/3/16 13:35
 */
@Builder
@Data
@AllArgsConstructor
public class UserRegisterCount {
    /**
     * 总数
     */
    private int count;
    /**
     * 每月数量
     */
    private int month;
    /**
     * 每周数量
     */
    private int week;
    /**
     * 每天数量
     */
    private int day;
}
