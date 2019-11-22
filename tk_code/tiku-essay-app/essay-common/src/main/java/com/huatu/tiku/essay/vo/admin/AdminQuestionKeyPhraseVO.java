package com.huatu.tiku.essay.vo.admin;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
@AllArgsConstructor
@Data
public class AdminQuestionKeyPhraseVO {
    private long id;
    private String item;
    //关键短句该出现的位置，1为出现在首段，2为出现在中间，3为出现在末尾4为全篇(默认2)
    private int  position;
    //关键短句分数
    private double score;
    //对应试题id
    private long questionDetailId;
    private int type;
    //论点划档级别
    private int level;
    //关键词列表
    private List<AdminQuestionKeyWordVO> keyWordVOList;
    //上级id
    private long pid;

    //近似句列表
    private List<AdminQuestionKeyPhraseVO> similarPhraseList;

    public AdminQuestionKeyPhraseVO(){
        this.setItem("");
        this.setKeyWordVOList(Lists.newLinkedList());
    }
    public void myTrim(){
        if(this.item!=null){
            this.item = this.item.trim();
        }
        if(!CollectionUtils.isEmpty(this.getKeyWordVOList())){
            for(AdminQuestionKeyWordVO adminQuestionKeyWordVO:this.getKeyWordVOList()){
                adminQuestionKeyWordVO.myTrim();
            }
        }
    }

    public void clearId() {
        this.setId(0);
        this.setPid(0);
        this.setQuestionDetailId(0);
        if(!CollectionUtils.isEmpty(similarPhraseList)){
            for (AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO : similarPhraseList) {
                adminQuestionKeyPhraseVO.clearId();
            }
        }
        if(!CollectionUtils.isEmpty(keyWordVOList)){
            for (AdminQuestionKeyWordVO keyWordVO : keyWordVOList) {
                keyWordVO.clearId();
            }
        }
    }
}
