package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/5/18.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_user_report_push_log")
public class UserReportPushLog extends BaseEntity{

    //用户id
    private String openId;
    //报告日期
    private String date;

}
