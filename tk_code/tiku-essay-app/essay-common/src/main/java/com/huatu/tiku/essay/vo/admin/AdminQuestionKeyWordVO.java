package com.huatu.tiku.essay.vo.admin;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by huangqp on 2017\12\12 0012.
 */
@Builder
@AllArgsConstructor
@Data
public class AdminQuestionKeyWordVO {
    private long id;
    //内容
    private String item;
    //分数
    private double  score;
    //切分词组合
    private List<String> splitWords;
    /**
     * 该关键词类型：1为某小题的关键词；
     *             2为某关键词的近义词；
     *             3为某关键句下面的关键词；
     *             4为格式中标题的关键词；
     *             5为格式中称呼的关键词；
     *             6为格式中落款的关键词 ；
     *             7.关键词的描述（12.19 zhaoxi 新增）
     */
    private int type;

    //对应的id，若type为1，则为某小题的detail_ID；type为2,则为某关键词的id；type为3，则为某关键句的id；type为4，5,6，则为格式的id
    private long correspondingId;
    //近义词列表
    private List<AdminQuestionKeyWordVO> similarWordVOList;
    //试题id
    private long questionDetailId;

    public AdminQuestionKeyWordVO(){
        this.setItem("");
        this.setSplitWords(Lists.newLinkedList());
        this.setSimilarWordVOList(Lists.newLinkedList());
    }
    public void myTrim(){
        if(this.item!=null){
            this.item = this.item.trim();
        }
        if(!CollectionUtils.isEmpty(this.getSplitWords())){
            for(String word:this.getSplitWords()){
                if(word!=null){
                    word = word.trim();
                }
            }
        }
        if(!CollectionUtils.isEmpty(this.getSimilarWordVOList())){
            for(AdminQuestionKeyWordVO adminQuestionKeyWordVO:this.getSimilarWordVOList()){
                adminQuestionKeyWordVO.myTrim();
            }
        }
    }

    public void clearId() {
        this.setId(0);
        this.setCorrespondingId(0);
        this.setQuestionDetailId(0);
        if(!CollectionUtils.isEmpty(similarWordVOList)){
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : similarWordVOList) {
                adminQuestionKeyWordVO.clearId();
            }
        }
    }
}
