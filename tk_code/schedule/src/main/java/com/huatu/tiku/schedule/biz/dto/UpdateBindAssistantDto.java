package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**修改直播绑定助教
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateBindAssistantDto implements Serializable{
    private static final long serialVersionUID = -6419154845719174802L;

    /**
     * 直播id
     */
    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    /**
     * 助教id
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 助教类型
     */
    @NotNull(message = "教师类型不能为空")
    private TeacherType teacherType;
}
