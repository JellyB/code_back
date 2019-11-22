package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author zhaoxi
 * @Description: 单题组查询--单题VO
 * @date 2018/9/18下午8:53
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionSearchVO {
    private Long baseId;
    private Long detailId;

    private int sort;
    //材料
    private List<Map<String,Object>> materialList;
    //题干
    private String stem;
    //地区id
    private long areaId;
    //地区名称
    private String areaName;
    //子地区id
    private long subAreaId;
    //子地区名称
    private String subAreaName;
}
