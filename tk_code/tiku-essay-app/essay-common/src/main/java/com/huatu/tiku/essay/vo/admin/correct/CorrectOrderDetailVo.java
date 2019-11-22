package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 批改订单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectOrderDetailVo extends CorrectOrderBaseVo {

    //批改类型(0 普通批改,名师批改等)
    //TODO 待扩展
    private int correctType;

    private String correctTypeName;

    //学员姓名
    private String UserName;
    //老师基本金额
    private Double money;
    //结算状态(0 尚未结算 1已经结算,暂时都是未结算)
    private int settlementStatus;

    //批改类型（1智能批改2人工批改）
    private int correctMode;

    //管理员 是否可以再次批改 0 可再次批改 1不可再次批改
    private int reCorrectStatus;

    // 答题卡提交时间
    private Date submitTime;

    //用户手机号
    private String userPhoneNum;

    //批改类型名称(2人工批改 3智能转人工批改)
    private String correctModeName;


}
