package com.huatu.tiku.essay.vo.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/26.
 * base试卷对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminEssayPaperVO{

    /*Status -1 删除状态  1未审核  2审核中  3审核未通过  4 审核通过  */
    /* BizStatus 0未上线  1上线中  2已下线 */
    private int id;
    /* 试卷名称 */
    private String name;
    /** 试题年份 **/
    private String paperYear;
    /** 试题日期 **/
    private String paperDate;
    /** 地区id **/
    private long areaId;
    /** 地区名称 **/
    private String areaName;
    /** 真题 1  模考题 0 **/
    private int type;
    /* 答题限时 */
    private int limitTime;
    /* 得分 */
    private double score;

}
