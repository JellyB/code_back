package com.huatu.tiku.essay.vo.resp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/28.
 * 交卷完整对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperCommitVO {

    //答题卡id
    private Long answerCardId;
    //操作类型(0 保存  1交卷 2 更新时间)
    private Integer saveType;
    //类型  0单题  1套题 2 议论文
    private Integer type;
    //考试类型  1 真题  0模考题
    private Integer examType;

    //试卷id
    private Long paperBaseId;



    /*  未做答题数量   */
    private Integer unfinishedCount;
    /*  本次答题做到第几题  */
    private Integer lastIndex;
    // 答题用时
    private Integer spendTime;

    //套题答案信息
    private List<PaperCommitAnswerVO> answerList;
    
    /**
     * 批改类型
     * @see CorrectModeEnum
     */
    private Integer correctMode;
    
    /**
     * @see
     * 是否顺延
     */
    private Integer delayStatus;
    
    
    /**
     * 1普通类型 2课后作业类型
     */
    private Integer exercisesType;


}
