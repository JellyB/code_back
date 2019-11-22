package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**修改面试直播授课类型
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateCourseLiveCategoryDto implements Serializable{

    private static final long serialVersionUID = -2988601230778205111L;
    /**
     * 直播id
     */
    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    /**
     * 授课类型
     */
    @NotNull(message = "授课类型不能为空")
    private CourseLiveCategory courseLiveCategory;
}
