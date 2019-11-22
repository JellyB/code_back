package com.huatu.tiku.response.area;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 招警机考 地区列表
 * @date 2018/8/20下午4:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaWithSchoolRespVO {

    /**
     * 地区
     */
    private Long areaId;
    private String areaName;

    /**
     * 学院列表
     */
    private List<SchoolRespVO> schoolList;

    @Data
    public static class SchoolRespVO {
        private Long schoolId;
        private String schoolName;

    }

}

