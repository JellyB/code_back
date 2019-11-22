package com.huatu.tiku.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: TODO
 * @date 2018/9/29下午2:44
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResultVO {

    // 学员id
    private long userId;
    // 模考名称
    private String paperName;
    // 学员成绩
    private Double score;
    //交卷时间
    private String endTime;
    //答题时间
    private Integer expendTime;
    //模块得分
    List<Double> moduleScore;
    //用户昵称
    private String nick;
    //手机号
    private String mobile;
    //学员姓名
    private String name;
    //地区名称
    private String positionName;
    //地区id
    private long positionId;


}
