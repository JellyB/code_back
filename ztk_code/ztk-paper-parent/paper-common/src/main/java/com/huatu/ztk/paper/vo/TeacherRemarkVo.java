package com.huatu.ztk.paper.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师评语
 * @author shanjigang
 * @date 2019/3/4 16:51
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class TeacherRemarkVo {
    /**
     * 知识点灵活运用的条数
     */
    private int elasticCount;

    /**
     * 灵活运用知识点名称
     */
    private String elasticName;

    /**
     * 知识点了解条数
     */
    private int knowCount;

    /**
     * 了解的知识点名称
     */
    private String knowName;

    /**
     * 知识点掌握条数
     */
    private int knowWellCount;

    /**
     * 掌握的知识点名称
     */
    private String knowWellName;

    /**
     * 	知识点条数
     */
    private int pointCount;

    /**
     * 教师名称（根据解析课查询的老师名称）
     */
    private String teacherName;

    /**
     * 知识点理解条数
     */
    private int understandCount;

    /**
     * 理解的知识点名称
     */
    private String understandName;

}
