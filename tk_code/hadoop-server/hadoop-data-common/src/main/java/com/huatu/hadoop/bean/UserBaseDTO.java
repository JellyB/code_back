package com.huatu.hadoop.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserBaseDTO {

    // 用户名
    private String user_name;
    // 姓名
    private String true_name;
    // 手机号
    private String phone;
    // 渠道来源
    private String from;
    // 注册终端
    private String reg_type;
    // 考试类型
    private String subject;
    // 考试地区
    private String area;
    // ip对应地址
    private String reg_ip;
    // 注册时间
    private String reg_time;
    // 最近一次登录时间
    private String last_login_time;

}
