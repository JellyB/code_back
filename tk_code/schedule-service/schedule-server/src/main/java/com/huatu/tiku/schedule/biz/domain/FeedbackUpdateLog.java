package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * 课时修改记录
 *
 * @author Geek-S
 */
@Entity
@Getter
@Setter
public class FeedbackUpdateLog extends BaseDomain {

    private static final long serialVersionUID = 5730966574014090038L;

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 课时ID
     */
    private Long classHourId;

    /**
     * 修改字段
     */
    private String field;

    /**
     * 原始值
     */
    private String oriValue;

    /**
     * 修改值
     */
    private String value;

    /**
     * 操作类型 0 教研 1 录播
     */
    private Integer type;
}
