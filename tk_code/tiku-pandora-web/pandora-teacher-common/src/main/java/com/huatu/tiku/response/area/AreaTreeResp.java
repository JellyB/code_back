package com.huatu.tiku.response.area;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.response.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 地区树形结构对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaTreeResp extends BaseResp {

    /**
     * 地区id
     */
    private Long id;
    /**
     * 地区名称
     */
    private String name;
    /**
     * 下级地区
     */
    private List<AreaTreeResp> subAreaList;


}
