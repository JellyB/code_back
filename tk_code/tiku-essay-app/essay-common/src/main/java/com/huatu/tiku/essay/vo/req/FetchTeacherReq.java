package com.huatu.tiku.essay.vo.req;

import lombok.Data;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@Data
public class FetchTeacherReq {

    private Long teacherId;

    private String teacherName;

    private Integer teacherStatus;

    private String correctType;

    private Integer teacherLevel;

    private String phoneNum;

    private Integer isPay = 1;

    private Integer page = 1;

    private Integer pageSize = 20;
    /**
     * 对应接单类型
     */
    private Integer teacherOrdertype;
    
 

}



