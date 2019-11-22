package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by x6 on 2017/11/27.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayAnswerVO {

    /* 题目base表id */
    private Long questionBaseId;
    /* 题目detail表id */
    private Long questionDetailId;
    /* 题干信息 */
    private String stem;
    /*题目类型*/
    private int questionType;

    /**
     * 题组ID
     */
    private long similarId;
    /* 地区id */
    private  Long areaId;
    /* 地区名称 */
    private  String areaName;
    /* 批改时间 */
    private String correctDate;
    /*  实际得分 */
    private  Double examScore;
    /*  总分 */
    private  Double score;
    /* 试卷名称 */
    private  String paperName;
   /* 试卷id */
   private  Long paperId;
    /* 答题卡id */
    private  Long answerId;

    //答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
    private Integer bizStatus;

    /**
     * 是否有存在视频解析
     */
    private Boolean videoAnalyzeFlag;
    /**
     * 视频解析id
     */
    private Integer videoId;

    /**
     * 是否可以查看报告
     */
    private Boolean paperReportFlag;
    
    /**
     * 修改时间
     */
    private Date modifyDate;
    
    /**
     * 创建时间
     */
    private Long createDate;
    
    /**
     * 批改日期年月日
     */
    private String correctDateStr;
    


}
