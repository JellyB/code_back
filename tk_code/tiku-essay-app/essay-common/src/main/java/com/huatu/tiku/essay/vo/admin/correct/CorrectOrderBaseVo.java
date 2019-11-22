package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CorrectOrderBaseVo {

    //订单ID
    private Long id;

    //批改订单状态
    private int bizStatus;

    // 批改订单状态(批改中,待分配)
    private String bizStatusName;

    //任务类型(小题, 应用文,议论文,套题)
    private int type;

    //任务类型名称
    private String typeName;

    //试题题目
    private String stem;

    //到期时间
    protected Date gmtDeadLine;
    //答题卡ID
    private long answerCardId;

    //答题卡类型 0 单题(question_answer) 1 套题（paper_answer）TypeEnum
    private int answerCardType;

    //订单到期时间（订单提交时间+订单超时时长（24小时）） *
    private Date theDueDate;

    //退回原因（退回学员原因,其他类型原因均在操作日志表中查询）
    private String correctMemo;

    //是否已经评价（0 未评价 1 评价）
    private int feedBackStatus;

    //剩余时间
    private long leftTime;

    //提交时间
    private Date gmtCreate;

    //累计时长
    private long totalTime;

    /*～～～～～～～～～～～～～老师信息～～～～～～～～～～～*/

    //用户ID
    private long userId;

    //接单老师
    private long receiveOrderTeacher;

    //接单老师姓名
    private String teacherName;

    //接单人手机号
    private String phoneNum;

    //老师接单时间
    private Date receiveTime;

    //完成时间
    private Date endTime;

    //是否顺延 (0 不顺延, 1顺延)
    private int delayStatus;

    //是否超时(0 未超时 1 超时)
    private int timeOutStatus;
    //老师评分
    private double score;

    //批改类型(1智能批改)
    private int correctMode;

    //是否可用 (1有效订单 -1 无效订单 ),二次批改,会将第一次的工单状态修改为无效订单,方便老师工作量统计
    private int effectiveStatus = 1;
    //是否可以再次批改（0 可再次批改 1不可再次批改）
    private int reCorrectStatus;


}
