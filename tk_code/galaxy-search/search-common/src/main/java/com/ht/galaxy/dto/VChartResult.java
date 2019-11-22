package com.ht.galaxy.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author jbzm
 * @date Create on 2018/3/15 11:38
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VChartResult {
    /**
     * 列数
     */
    private List<String> columns;
    /**
     * 行数
     */
    private List<JSONObject> rows;

}


