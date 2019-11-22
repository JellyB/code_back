package com.huatu.tiku.schedule.biz.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherLevel;

import lombok.Data;

/**更改教师信息
 * @author wangjian
 **/
@Data
public class UpdateTeacherDto implements Serializable{
    private static final long serialVersionUID = -4421317914413292430L;

    /**
     * ID
     */
    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 姓名
     */
    @NotEmpty(message = "名称不能为空")
    private String name;

    /**
     * 考试类型
     */
    @NotNull(message = "考试类型不能为空")
    private ExamType examType;

    /**
     * 科目
     */
    @NotNull(message = "科目不能为空")
    private Long subjectId;

    /**
     * 类型 0 组长 1 组员
     */
    @NotNull(message = "是否组长不能为空")
    private Boolean leaderFlag;

    /**
     * 教师级别
     */
    private TeacherLevel teacherLevel;

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
