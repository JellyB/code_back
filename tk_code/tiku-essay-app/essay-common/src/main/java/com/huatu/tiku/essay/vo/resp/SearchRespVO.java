package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 搜索结果响应体
 * @date 2018/12/145:24 PM
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRespVO {
    private int type;

    private String typeName;

    private Object data;

}
