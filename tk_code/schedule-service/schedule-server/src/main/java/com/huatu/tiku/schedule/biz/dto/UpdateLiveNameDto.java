package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateLiveNameDto implements Serializable{
    private static final long serialVersionUID = 4090401889022541609L;

    /**
     * 直播id
     */
    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    /**
     * 直播名称
     */
    @NotNull(message = "直播内容不能为空")//空字符串可以传入
    private String liveName;
}
