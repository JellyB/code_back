package com.huatu.tiku.position.biz.vo;

import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
@NoArgsConstructor
public class AreaVo implements Serializable {

    private static final long serialVersionUID = 3118680509412654324L;

    private Long id;

    private String name;

    private String code;

    private Integer type;

    private Long parentId;

    private Status status;

    private List<AreaVo> areas;

    private Long provinceId;//所在省id

    public AreaVo(Area area) {
        this.id = area.getId();
        this.name = area.getName();
        this.code = area.getCode();
        this.type = area.getType();
        this.parentId = area.getParentId();
        this.status = area.getStatus();
        this.provinceId = area.getProvinceId();
    }
}
