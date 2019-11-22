package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;


/**
 * Created by x6 on 2017/11/27.
 */

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayAnswerV2VO extends EssayAnswerVO {

    /**
     * 批改备注，被驳回原因
     */
    private String correctMemo;

    /**
     * 批改模式 1 智能批改 2人工批改
     */
    private Integer correctMode;
    
    /**
     * 试卷类型
     */
    private Integer paperType;

    /**
     * 点击提示文案
     */
    private String clickContent;
    

}
