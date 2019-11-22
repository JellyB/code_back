package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linkang on 8/11/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class FreeCourseBean {
    private int source;         //来源
    private String username;    //ucenter的用户名
    private int tag;            //标志
    private int catgory;
}
