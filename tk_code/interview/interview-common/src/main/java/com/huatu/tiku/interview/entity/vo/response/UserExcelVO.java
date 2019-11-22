package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 学员信息
 * @date 2018/7/26下午9:53
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserExcelVO {

    //学员姓名
    private String userName;
    //地区
    private String areaName;
    //班级
    private String className;
}
