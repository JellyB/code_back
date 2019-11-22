package com.huatu.one.biz.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大数据报表
 *
 * @author geek-s
 * @date 2019-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PHPBlueClassRankingResponse {

    private String rid;

    private String title;

    @JsonProperty("CategoryName")
    private String categoryName;

    private String count;

    private String sumprice;
}
