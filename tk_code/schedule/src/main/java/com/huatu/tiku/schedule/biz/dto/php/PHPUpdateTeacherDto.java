package com.huatu.tiku.schedule.biz.dto.php;

import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import lombok.Data;

import java.io.Serializable;

/**PHP的修改教师参数
 * @author wangjian
 **/
@Data
public class PHPUpdateTeacherDto implements Serializable{
    private static final long serialVersionUID = -8994240311074294710L;

    private Long pid;

    /**
     * 教师名称
     */
    private String name;

    /**
     * 考试类型id
     */
    private Integer examType;

    /**
     * 科目id
     */
    private Long subjectId;

    /**
     * 教师状态
     */
    private Integer status;

    /**
     * 电话
     */
    private String phone;

}
