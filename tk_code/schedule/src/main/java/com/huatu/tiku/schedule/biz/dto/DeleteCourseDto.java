package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class DeleteCourseDto implements Serializable{
    private static final long serialVersionUID = 1045806463256166651L;

    /**
     * 课程id
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 删除原因
     */
    @NotBlank(message = "删除原因不能为空")
    private String reason;
}
