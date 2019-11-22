package com.huatu.tiku.essay.vo.admin;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by huangqp on 2017\12\8 0008.
 */
@Data
@AllArgsConstructor
@Builder
public class AdminQuestionKeyRuleVO {
    //问题id
    private long questionDetailId;
    //试卷id
    private long paperId;
    //关键字
    private List<AdminQuestionKeyWordVO> keyWordList;
    //关键句
    private List<AdminQuestionKeyPhraseVO> keyPhraseList;
    //主题句
    private AdminQuestionTopicVO topic;
    //中心论点
    private List<AdminQuestionKeyPhraseVO> argumentList;


    //有描述的关键字
    private List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList;
    //有描述的关键句
    private List<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescList;


    public AdminQuestionKeyRuleVO(){
        this.keyWordList = Lists.newLinkedList();
        this.keyPhraseList = Lists.newLinkedList();
        this.argumentList = Lists.newLinkedList();
        this.topic = new AdminQuestionTopicVO();
    }
    public void myTrim(){
        if (!CollectionUtils.isEmpty(this.getKeyPhraseList())) {
            for(AdminQuestionKeyPhraseVO keyPhrase:this.getKeyPhraseList()){
                keyPhrase.myTrim();
            }
        }
        if (!CollectionUtils.isEmpty(this.getArgumentList())) {
            for(AdminQuestionKeyPhraseVO keyPhrase:this.getArgumentList()){
                keyPhrase.myTrim();
            }
        }
        if (!CollectionUtils.isEmpty(this.getKeyWordList())) {
            for(AdminQuestionKeyWordVO keyWordVO:this.getKeyWordList()){
                keyWordVO.myTrim();
            }
        }
        if(this.getTopic()!=null){
            topic.myTrim();
        }
    }
}
