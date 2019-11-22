package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**修改助教信息
 * @author wangjian
 **/
@Data
public class UpdateAssistantDto implements Serializable {

    private static final long serialVersionUID = 3250577391270577372L;


    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 姓名
     */
    @NotEmpty(message = "名称不能为空")
    private String name;


    /**
     * 微信
     */
    @NotEmpty(message = "微信不能为空")
    private String wechat;

    @NotNull(message = "类型不能为空")
    private TeacherType teacherType;
}