package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by huangqp on 2017\12\10 0010.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerSubFormatVO {
    private double score;
    private List<AdminQuestionKeyWordVO> childKeyWords;

    public void myTrim() {
        if(CollectionUtils.isNotEmpty(this.getChildKeyWords())){
            for(AdminQuestionKeyWordVO adminQuestionKeyWordVO:this.getChildKeyWords()){
                adminQuestionKeyWordVO.myTrim();
            }
        }
    }
}
