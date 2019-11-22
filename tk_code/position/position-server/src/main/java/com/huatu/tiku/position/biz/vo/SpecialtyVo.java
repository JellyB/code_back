package com.huatu.tiku.position.biz.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**专业vo
 * @author wangjian
 **/
@Data
public class SpecialtyVo implements Serializable{

    private static final long serialVersionUID = 5590071798990232102L;

    private Long value;

    private String label;

    private List<SpecialtyVo> children;

    public SpecialtyVo(){
        this.children=new ArrayList<>();
    }
}
