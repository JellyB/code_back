package com.huatu.tiku.schedule.biz.vo.php;

import com.huatu.tiku.schedule.biz.domain.Teacher;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
@NoArgsConstructor
public class PHPUpdateTeacherVo implements Serializable{
    private static final long serialVersionUID = 7415074293649672140L;

    /**
     * 教师id
     */
    private Long pid;

    /**
     * 教师名称
     */
    private String name;

    /**
     * 考试类型
     */
    private Integer examType;

    /**
     * 科目
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

    /**
     *根据teacher封装
     * 用于php同步数据返回值
     */
    public PHPUpdateTeacherVo(Teacher teacher){
        pid=teacher.getPid();
        name=teacher.getName();
        examType=teacher.getExamType()==null?null:teacher.getExamType().getId();
        subjectId=teacher.getSubjectId();
        status=teacher.getStatus()==null?null:teacher.getStatus().ordinal();
        phone=teacher.getPhone();
    }

}
