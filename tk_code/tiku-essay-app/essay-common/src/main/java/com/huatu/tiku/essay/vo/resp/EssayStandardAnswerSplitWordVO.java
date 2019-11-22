package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Builder
//@NoArgsConstructor
@AllArgsConstructor
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayStandardAnswerSplitWordVO {
    //id
    private long id;
    //词语内容
    private String item;
    //1为关键句的分词,2为议论文主题的切分词
    private int type;
    //对应的关联的id，例如关键句的id
    private long relationId;
    public EssayStandardAnswerSplitWordVO(){
        this.setItem("");
    }

    public void myTrim() {
        if(this.getItem()!=null){
            this.setItem(this.getItem().trim());
        }
    }
}
