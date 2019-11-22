package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * 格式规则
 */
public class EssayStandardAnswerFormatVO {
    private long id;
    //格式类型，1代表只有标题；2代表有标题、称呼；3代表有标题、落款；4代表有标题、称呼和落款；5没有任何格式
    private int type;
    //标题分数
    private double titleScore;
    //称呼分数
    private double appellationScore;
    //落款
    private double inscribeScore;
    //对应试题
    private long questionDetailId;
    private int status = 1;
    private int bizStatus = 0;
    private List<EssayStandardAnswerKeyWordVO> childKeyWords;

}
