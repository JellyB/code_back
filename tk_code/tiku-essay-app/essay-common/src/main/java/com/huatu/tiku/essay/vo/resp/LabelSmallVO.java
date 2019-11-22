package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author zhaoxi
 * @Description: 批注的小VO
 * @date 2018/7/16下午5:16
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabelSmallVO {

    //批注ID
    private Long labelId;

    //批注人
    private String labelTeacher;

    //批注时间
    private Date labelTime;
    //批注分数
    private Double labelScore;
}
