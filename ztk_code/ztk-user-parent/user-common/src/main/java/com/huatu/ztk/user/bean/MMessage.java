package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-10-31 11:32
 */

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class MMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String target;//NativeTarget
    private Map mParams;
    private Long onLineTime;
    private Long offLineTime;
    private Integer type;

}
