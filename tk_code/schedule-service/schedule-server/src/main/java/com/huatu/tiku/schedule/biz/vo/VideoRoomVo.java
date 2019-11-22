package com.huatu.tiku.schedule.biz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoomVo implements Serializable{
    private static final long serialVersionUID = -8658178916629228566L;

    private Long id;

    private String name;

    private String mark;

    private Boolean showFlag;
}
