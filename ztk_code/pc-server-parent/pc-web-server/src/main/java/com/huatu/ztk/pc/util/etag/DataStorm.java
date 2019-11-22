package com.huatu.ztk.pc.util.etag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\8 0008.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStorm {
    private String index;
    /**
     * 索引类型
     */
    private String type;
    /**
     * 接收数据
     */
    private List<Map<String,Object>> data;
    /**
     * 属于类型
     */
    private String dataType;
    /**
     * 游标
     */
    private int offsetType;

}
