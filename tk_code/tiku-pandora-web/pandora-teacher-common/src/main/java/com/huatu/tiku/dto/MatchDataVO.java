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
public class MatchDataVO {

    // 学员id
    private long id;
    // 模考名称
    private String name;
    // 时间信息
    private String timeInfo;
    //标签
    private String tag;
    //报名人数
    private long enrollCount;
    //考试人数
    private long examCount;


}
