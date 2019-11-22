package com.huatu.tiku.interview.entity.vo.response;

/*
     地区下班级列表
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaClassVO {

    //地区id
    private Long id;
    //地区名称
    private String name;
    //班级列表
    private List<AreaClassVO> classList;
}
