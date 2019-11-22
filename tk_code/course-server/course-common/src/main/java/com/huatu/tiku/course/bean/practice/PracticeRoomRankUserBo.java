package com.huatu.tiku.course.bean.practice;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by lijun on 2019/2/27
 */
@NoArgsConstructor
@Setter
@Getter
public class PracticeRoomRankUserBo implements Serializable {

    private static final String RANK_INFO_TEMPLATE = "0000%s";
    
    /**
     * 排名权重
     */
    private static final Integer RANK_INFO_SCORE = 10000;

    /**
     * ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 总分
     */
    private Integer totalScore;


    //总用时
    private Integer totalTime;

    //获取最终排名使用数据 4位答题总分数 + (10000减去 4 位答题总时间)
    public Integer buildRankInfo() {
        return Integer.valueOf(formatNum(totalScore) +formatNum(RANK_INFO_SCORE-totalTime));
    }

    private static String formatNum(Integer number) {
        final String numberString = String.format(RANK_INFO_TEMPLATE, number);
        return numberString.substring(numberString.length() - 4, numberString.length());
    }

    @Builder
    public PracticeRoomRankUserBo(Integer id, String name, Long courseId, Integer totalScore, Integer totalTime) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.totalScore = totalScore;
        this.totalTime = totalTime;
    }
    public static void main(String[] args) {
    	System.out.println(Integer.valueOf(formatNum(50) +formatNum(RANK_INFO_SCORE-5)));
    	
    	Integer ret = 509995%10000;
    	System.out.println(ret);
	}
}
