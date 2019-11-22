package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 修改教师状态
 *
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateTeacherStatusDto implements Serializable {
    private static final long serialVersionUID = -2226629886208455096L;

    /**
     * ID
     */
    @NotEmpty(message = "ids不能为空")
    private List<Long> ids;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private TeacherStatus status;
}
