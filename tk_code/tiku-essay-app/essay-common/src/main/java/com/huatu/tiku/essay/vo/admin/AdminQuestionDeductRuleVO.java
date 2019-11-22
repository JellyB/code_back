package com.huatu.tiku.essay.vo.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by huangqp on 2017\12\8 0008.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminQuestionDeductRuleVO {
    private long questionDetailId;
    private long paperId;
    private List<AdminCommonDeductVO> deductRuleList = new ArrayList<>();
    public void myTrim(){
        if(CollectionUtils.isNotEmpty(this.getDeductRuleList())){
            for(AdminCommonDeductVO adminCommonDeductVO:this.getDeductRuleList()){
                adminCommonDeductVO.myTrim();
            }
        }
    }
}
