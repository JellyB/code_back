package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 添加教师参数
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class CreateTeacherDto implements Serializable {

    private static final long serialVersionUID = -1492287125662154148L;
    /**
     * 姓名
     */
    @NotEmpty(message = "名称不能为空")
    private String name;

    /**
     * 考试类型
     */
    private ExamType examType;

    /**
     * 科目
     */
    private Long subjectId;

    /**
     * 类型 0 组长 1 组员
     */
    private Boolean leaderFlag;

    /**
     * 教师类型
     */
    @NotNull(message = "教师类型不能为空")
    private TeacherType teacherType;

    /**
     * 教师级别
     */
    private TeacherLevel teacherLevel;

    /**
     * 手机
     */
    @NotEmpty(message = "手机不能为空")
    private String phone;

    /**
     * 微信
     */
    @NotEmpty(message = "微信不能为空")
    private String wechat;


    /**
     * 教师授课目录
     */
    private List<TeacherSubject> teacherSubjects;
}
