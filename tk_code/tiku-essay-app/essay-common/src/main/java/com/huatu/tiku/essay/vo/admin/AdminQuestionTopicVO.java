package com.huatu.tiku.essay.vo.admin;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerSplitWordVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by huangqp on 2017\12\16 0016.
 */
@Builder
//@NoArgsConstructor
@AllArgsConstructor
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminQuestionTopicVO {
    private long id;
    private String item;
    //关键短句分数
    private double score;
    //对应试题id
    private long questionDetailId;
    //关键词列表
    private List<EssayStandardAnswerSplitWordVO> splitWordList;
    public AdminQuestionTopicVO(){
        this.item = "";
        EssayStandardAnswerSplitWordVO essayStandardAnswerSplitWordVO = new EssayStandardAnswerSplitWordVO();
        this.splitWordList = Lists.newLinkedList();
        splitWordList.add(essayStandardAnswerSplitWordVO);
    }

    public void myTrim() {
        if(this.getItem()!=null){
            this.setItem(this.getItem().trim());
        }
        if(!CollectionUtils.isEmpty(this.getSplitWordList())){
            for(EssayStandardAnswerSplitWordVO essayStandardAnswerSplitWordVO:this.getSplitWordList()){
                essayStandardAnswerSplitWordVO.myTrim();
            }
        }
    }
}
