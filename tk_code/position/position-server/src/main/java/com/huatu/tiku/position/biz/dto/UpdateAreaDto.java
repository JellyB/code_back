package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class UpdateAreaDto implements Serializable{

    private static final long serialVersionUID = -5807397038826171263L;

    @NotEmpty(message = "报考地点不能为空")
    private List<Long> areaIds;//报考地点

}
