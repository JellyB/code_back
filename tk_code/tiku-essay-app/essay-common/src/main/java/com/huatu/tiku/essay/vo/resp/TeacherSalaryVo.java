package com.huatu.tiku.essay.vo.resp;

import lombok.Data;

/**
 * 教师薪资vo
 *
 * @author zhangchong
 */
@Data
public class TeacherSalaryVo {

    /**
     * 类型
     */
    private String label;

    /**
     * 批改数量
     */
    private Integer count;

    /**
     * 总薪资
     */
    private Integer totalMoney;
}
