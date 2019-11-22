package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_correct_order")
@DynamicUpdate
@DynamicInsert
public class CorrectOrder extends BaseEntity {
    /**
     * 批改类型(小题,应用文,议论文,套题--注意此处有套题!!)
     * 对应的枚举 QuestionLabelEnum
     */
    private int type;

    private String name;

    /**
     * 订单到期时间（订单提交时间+订单超时时长（24小时））
     */
    @Temporal(TemporalType.TIMESTAMP)
    protected Date gmtDeadLine;

    /**
     * 答题卡ID
     */
    private long answerCardId;

    /**
     * 答题卡类型 0 单题(question_answer) 1 套题（paper_answer）
     * TypeEnum
     */
    private int answerCardType;

    /**
     * 用户ID
     */
    private long userId;

    /**
     * 用户名
     */
    private String userName;

    //bizStatus 是订单流转状态,即CorrectOrderStatusEnum中枚举值。注意：已反馈的状态不会再这里存放,只是虚拟状态,为后台展示使用
    // 是否反馈，是需要根据feedBackStatus字段来判断

    /**
     * 接单老师
     */
    private long receiveOrderTeacher;

    /**
     * 退回原因（退回学员原因,其他类型原因均在操作日志表中查询）
     */
    private String correctMemo;

    /**
     * 是否已经评价（0 未评价 1 评价）
     * CorrectFeedBackEnum
     */
    private int feedBackStatus;

    //老师批改时间
    private Date correctTime;
    //老师接单时间
    private Date receiveTime;

    //完成时间
    private Date endTime;

    /**
     * 是否顺延 (0 不顺延, 1顺延)
     */
    private int delayStatus;

    //批改模式(2人工批改3智能转人工)
    private int correctMode;

    //二次批改,关联的旧工单的ID;
    private long oldOrderId;
    //是否可用 (1有效订单 -1 无效订单 ),二次批改,会将第一次的工单状态修改为无效订单,方便老师工作量统计
    private int effectiveStatus = 1;

    /**
     * 结算状态 0
     */
    private int settlementStatus;

    /**
     * 商品订单详情id
     */
    private long goodsOrderDetailId;

    /**
     * 老师退回任务(存储老师退回原因)
     */
    private String teacherReturnReason;
    /**
     * 用户手机号
     */
    private String userPhoneNum;

    /**
     * 1 普通练习 2 课后练习
     */
    private Integer exercisesType;


}
