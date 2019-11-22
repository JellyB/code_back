package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物流路由信息
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ExpressDetail {
    private String time;
    private String ftime;  //时间 (优先取)
    private String context;  //内容
}
