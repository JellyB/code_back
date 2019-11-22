package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**修改直播绑定直播间
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateLiveRoomDto implements Serializable {

    private static final long serialVersionUID = -5942438520135307317L;

    /**
     * 直播id
     */
    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    /**
     * 直播间id
     */
    @NotNull(message = "直播间ID不能为空")
    private Long roomId;
}
