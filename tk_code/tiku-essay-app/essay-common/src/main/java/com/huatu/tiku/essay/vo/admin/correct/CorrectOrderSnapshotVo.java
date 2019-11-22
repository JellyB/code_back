package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 订单批改行为记录
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectOrderSnapshotVo {

    /**
     * 记录ID
     */
    private long id;
    /**
     * 订单ID
     */
    private Long orderId;
    /**
     * 订单操作ID
     */
    private Integer operate;
    /**
     * 订单操作名称
     */
    private String operateName;
    /**
     * 操作描述（根据不同的操作行为存储不同的JSON数据，解析方式不同）
     */
    private String description;
    /**
     * 操作渠道（0后台1老师2学员）
     */
    private Integer channel;

    //创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreate;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;
    /**
     * 手机号
     */
    private String phoneNum;
}
