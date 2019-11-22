package com.huatu.tiku.essay.vo.teacher;

import lombok.Data;

/**
 * Created by duanxiangchao on 2019/7/11
 */
@Data
public class TeacherQuery {

    private Long id;

    private String teacherName;

    private Integer teacherStatus;

    private Integer correctType;

    private Integer teacherLevel;

    private String phoneNum;

}
