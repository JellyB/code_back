package com.huatu.tiku.essay.vo.req;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/18
 * @描述
 */
@Data
@NoArgsConstructor
public class CorrectOrderRep {

    private Long id;
    private String questionContent;
    private String taskStatus;
    private String taskType;
    private Integer delayStatus;
    private Integer timeOutStatus;
    private String phoneNum;
    private String teacherName;
    private Long teacherId;
    private Integer page = 1;
    private Integer pageSize = 20;
    private String userName; //用户名
    private String userPhoneNum;//用户手机号
    private Integer correctMode;//批改类型(2人工批改3智能转人工)


}
