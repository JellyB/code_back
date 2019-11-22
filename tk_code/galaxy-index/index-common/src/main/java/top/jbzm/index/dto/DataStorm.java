package top.jbzm.index.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author jbzm
 * @date Create on 2018/4/3 13:29
 */
@Builder
@Data
public class DataStorm {
    /**
     * 索引名称
     */
    private String index;
    /**
     * 索引类型
     */
    private String type;
    /**
     * 接收数据
     */
    private List<Map<String, Object>> data;
    /**
     * 目标库类型
     */
    private String dataType;
    /**
     * 偏移量类型
     */
    private int offsetType;
}
