package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**新增规则
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class CreatRuleDto implements Serializable{
    private static final long serialVersionUID = -377025893326815688L;

    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 课程分类
     */
    @NotNull(message = "课程分类不能为空")
    private CourseCategory courseCategory;

    /**
     * 考试类型
     */
    @NotNull(message = "考试类型不能为空")
    private ExamType examType;

    /**
     * 直播类型(授课,练习)
     */
    private CourseLiveCategory liveCategory;

    /**
     * 计算系数
     */
    @NotNull(message = "折算系数不能为空")
    private Float coefficient;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "开始日期不能为空")
    private Date dateBegin;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateEnd;

}
