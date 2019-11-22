package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class VideoRoom extends BaseDomain implements Serializable {

    private static final long serialVersionUID = -7835615881111669330L;

    private String name;

    private String mark;//备注

    /**
     * 是否显示
     */
    private Boolean showFlag;
}
