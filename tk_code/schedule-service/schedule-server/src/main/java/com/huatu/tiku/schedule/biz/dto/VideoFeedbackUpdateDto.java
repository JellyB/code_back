package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 录播课时修改参数
 *
 * @author Geek-S
 */
@Getter
@Setter
@ToString
public class VideoFeedbackUpdateDto implements Serializable {

    private static final long serialVersionUID = 5730966574014090038L;

    /**
     * 录播课时反馈信息ID
     */
    @NotNull(message = "录播课时反馈信息ID不能为空")
    private Long id;

    /**
     * 字段名称
     */
    @NotBlank(message = "修改字段不能为空")
    private String field;

    /**
     * 修改值
     */
    @NotNull(message = "修改值不能为空")
    private String value;
}
