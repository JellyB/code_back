package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/11/23.
 * 单题详情VO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionVO {
    //展示信息
    private String showMsg;
    //展示信息id
    private Long similarId;
    private Integer sort;
    //题目id
    private Long questionBaseId;
    private Long questionDetailId;

    //试卷id
    private Long paperId;
    //题干
    private String stem;
    //答题时间
    private Integer limitTime;
    //答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
    private Integer bizStatus;
    //智能批改次数
    private Long correctTimes;

    //所属地区
    private List<EssayQuestionAreaVO> essayQuestionBelongPaperVOList;

    //分数
    private Double score;
    /* 答题范围 */
    private String answerRange;
    /* 答题细节（要求） */
    private String answerDetails;
    /* 答题任务 */
    private String answerTask;
    //答题要求  文字说明
    private String answerRequire;
    //输入字数最低限制
    private Integer inputWordNumMin;
    //输入字数最多限制(录入时限制字数)
    private Integer inputWordNumMax;
    //输入字数最多限制(提交时限制字数)
    private Integer commitWordNumMax;


    //材料列表
    List<EssayMaterialVO> materials;
    /* 是否已作答过该题 0未作答  1已作答*/
    private Integer isAnswered;


    /* 用户答案 */
    private String content;

    /* 答题卡id */
    private Long answerCardId;
    /* 学员答案字数 */
    private Integer inputWordNum;
    /* 用户已作答时间 */
    private Integer spendTime;
    /* 学员实际得分 */
    private Double examScore;
    /*  批改后的答案 */
    private String correctedContent;
    /* 用户已作答时间 */
    private Integer totalSpendTime;
    /* 学员实际得分 */
    private Double totalExamScore;

    /*  难度系数 */
    private String difficultGrade;


    //得分点
    private List<ScoreVO> addScoreList;
    //扣分点
    private List<ScoreVO> subScoreList;

    /* 参考答案 */
    private String answerComment;
    /*  标题  */
    private String topic;
    /* 子标题 */
    private String subTopic;
    /* 称呼 */
    private String callName;

    /* 落款日期 */
    private String inscribedDate;
    /* 落款名称 */
    private String inscribedName;


    //多个答案 V6.3
    List<EssayStandardAnswer> answerList;


    /* 阅卷规则描述 */
    private String correctRule;
    /* 权威点评 */
    private String authorityReviews;
    /* 审题要点  试题分析 */
    private String analyzeQuestion;
    //答案类型(0 参考答案  1标准答案)(V1单个答案根据阅卷规则判断)
    private Integer answerFlag;
    //试题类型
    private Integer type;

    /**
     * 试题类型名称
     */
    private String questionTypeName;
    /**
     * 人工批改总次数
     */
    private Integer correctSum;

    /* 用户智能批改次数 */
    private Integer correctNum;


    //是否上线
    private Boolean isOnline;


    /**
     * 是否有存在视频解析
     */
    private Boolean videoAnalyzeFlag;
    /**
     * 视频解析id
     */
    private Integer videoId;

    /**
     * 百家云视频token
     */
    private String token;

    /**
     * 批改类型1关键词2关键句
     */
    private Integer correctType;

    /**
     * 人工批改,本题目阅卷
     */
    private List<RemarkVo> remarkList;
    /**
     * 扣分项
     */
    private List<RemarkVo> deRemarkList;
    /**
     * 人工批注信息
     */
    private List<CorrectImageVO> userMeta;

    /**
     * 名师之声id
     */
    private Integer audioId;
    /**
     * 名师之声token
     */
    private String audioToken;

    /**
     * 批改类型
     *
     * @see CorrectModeEnum
     */
    private Integer correctMode;

    /**
     * 另外一种批改模式的答题卡
     */
    private Long otherAnswerCardId;

    /**
     * 人工批改次数
     */
    private Integer manualNum;
    /**
     * 全站人工批改总次数
     */
    private Integer manualSum;

    /**
     * 试题类型
     */
    private Integer questionType;

    /**
     * 智能已经转人工次数
     */
    private int convertCount;
}
