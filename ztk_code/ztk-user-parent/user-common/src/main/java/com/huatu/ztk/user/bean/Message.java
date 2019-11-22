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
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String target;//NativeTarget
    private Map params;
    private Long onLineTime;
    private Long offLineTime;
    private Integer type;

    class NativeTarget {
        public static final String COURSE_DETAIL = "ztk://course/detail";
        public static final String ARENA_HOME = "ztk://arena/home";
        public static final String H5_ACTIVE = "ztk://h5/active";
        public static final String H5_SIMULATE = "ztk://h5/simulate";
    }
}
