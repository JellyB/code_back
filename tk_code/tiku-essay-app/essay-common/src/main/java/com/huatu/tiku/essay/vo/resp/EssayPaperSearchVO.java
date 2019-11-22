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
 * @Description: 试卷搜索同步对象
 * @date 2018/9/18下午8:53
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperSearchVO {
    private Long id;

    //id
    private Long paperId;
    //名称
    private String paperName;


    //地区id
    private long areaId;
    //地区名称
    private String areaName;
    //子地区id
    private long subAreaId;
    //子地区名称
    private String subAreaName;

    //材料
    private List<Map<String, Object>> materialList;
    //题干
    private List<Map<String, Object>> stemList;
    /**
     * 是否有存在视频解析
     */
    private Boolean videoAnalyzeFlag;
}
