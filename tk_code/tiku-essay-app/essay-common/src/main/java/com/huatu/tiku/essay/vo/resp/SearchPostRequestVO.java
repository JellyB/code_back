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
public class SearchPostRequestVO {
    private int type;

    private int page;

    private int size;

    private String keyword;


}
