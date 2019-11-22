package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/12/12.
 *  单题组对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSingleQuestionGroupVO {

    //单题组的id
    private long id;
    //关联id
    private long relationId;
    //题目id
    private long questionBaseId;

    //展示信息
    private String showMsg;

    //年份
    private String year;
    //日期
    private String date;

    //单题id列表
    private List<Long> questionIdList;
    private List<AdminSingleQuestionVO> questionList;

    //用户id
    private long userId;

    //操作类型   0移除题组  1移除单题  2下线题组 3上线题组
    private int saveType;

    /* 1归纳概括、 2 综合分析、3 提出对策、4 应用文、5 议论文 */
    /* 以questionType表为准*/
    private int type;

    private List<Long> questionType;
    /* 题组名称 */
    private String questionTypeName;

    private int bizStatus;
}
