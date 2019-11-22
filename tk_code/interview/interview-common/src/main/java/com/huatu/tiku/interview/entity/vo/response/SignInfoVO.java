package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/5/17.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInfoVO {
    //地区id
    private long areaId;
    //地区名称
    private String areaName;
    // 班级名称
    private String className;
    // 班级id
    private Long classId;
    // 学员id
    private long id;
    // 学员姓名
    private String uname;
    // 打卡信息列表
    private List<SignTimeVO> dateList;




}
