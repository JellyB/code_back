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
public class SignTimeVO {
    //日期
    private String date;
    //签到时间列表
    private List<String> signList;
    //签到状态打卡状态（1 正常  2打卡异常  0未打卡（还没到日子））
    private Integer status;
}
