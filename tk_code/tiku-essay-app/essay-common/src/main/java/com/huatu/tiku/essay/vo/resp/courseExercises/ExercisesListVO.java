package com.huatu.tiku.essay.vo.resp.courseExercises;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant.EssayAnswerBizStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课后作业多题列表
 * @author zhangchong
 *
 */
@Data
@JsonInclude
@Builder
public class ExercisesListVO {
	
	/**
	 * 共多少题
	 */
    private Integer total;

    /**
     * 未完成数
     */
   // private Integer unfinishedCount;
    
    /**
     * 已完成数
     */
    private Integer finishedCount;
    
    /**
     * 课后作业列表
     */
    private List<ExercisesItemVO> exercisesItemList;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
	public static class ExercisesItemVO {
	    /* 题目base表id */
	    private Long questionBaseId;
	    /* 题目detail表id */
	    private Long questionDetailId;
	    /* 题干信息 */
	    private String stem;
	    /*题目类型*/
	    private Integer questionType;
	    /**
	     * @see EssayAnswerBizStatusEnum
	     */
	    private Integer bizStatus;
	    /*  实际得分 */
	    private  Double examScore;
	    /*  总分 */
	    private  Double score;
	    //用时
	    private Integer spendtime;
	    
	    private Integer sort;
	    /**
	     * 输入字数
	     */
	    private Integer inputWordNum;
	    
	    /**
	     * 答题卡id
	     */
	    private Long questionAnswerId;

	    private int correctNum;

	    private Long areaId;

	    private String areaName;

	    /**
	     * 被退回原因
	     */
		private String correctMemo;

		/**
		 * 预计出报告时间
		 */
		private String clickContent;
	}
    
}
