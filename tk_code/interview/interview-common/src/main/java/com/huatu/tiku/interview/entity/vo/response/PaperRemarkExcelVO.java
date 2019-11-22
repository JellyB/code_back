package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: TODO
 * @date 2018/7/27下午3:48
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperRemarkExcelVO {


    //------------举止仪态-------------
    private String behavior;
    //------------语言表达-------------
    private String languageExpression;
    //------------是否精准扣题-------------
    private String focusTopic;
    //------------是否条理清晰-------------
    private String isOrganized;
    //------------是否言之有物-------------
    private String haveSubstance;
}
