package com.huatu.tiku.match.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CacheBean {
    private int subject;    //科目
    private boolean open;       //使用guava缓存的标识
    private long startTime;     //guava开始时间
    private long endTime;       //guava结束时间

}
