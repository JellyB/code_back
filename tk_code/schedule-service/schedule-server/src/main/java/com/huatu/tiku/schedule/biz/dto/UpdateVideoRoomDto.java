package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 修改录播间
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateVideoRoomDto extends CreateVideoRoomDto  {


    private static final long serialVersionUID = 1853291834656186040L;

    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "是否展示不能为空")
    private Boolean showFlag;

}
