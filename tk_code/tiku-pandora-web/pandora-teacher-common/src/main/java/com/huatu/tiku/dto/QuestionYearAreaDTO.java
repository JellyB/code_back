package com.huatu.tiku.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by lijun on 2018/8/24
 */
@AllArgsConstructor
@Data
@Builder
public class QuestionYearAreaDTO {

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 年份
     */
    private List<Integer> yearList;

    /**
     * 区域信息
     */
    private List<Area> areaList;

    @AllArgsConstructor
    @Data
    @Builder
    public static class Area {
        private Long areaId;
        private String name;
    }
}
