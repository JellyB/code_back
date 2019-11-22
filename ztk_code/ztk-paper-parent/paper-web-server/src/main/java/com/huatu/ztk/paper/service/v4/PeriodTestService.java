package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.dto.PointFocusDto;
import com.huatu.ztk.paper.dto.SocreDistributionDto;
import com.huatu.ztk.paper.vo.*;
import com.self.generator.core.WaitException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shanjigang
 * @date 2019/2/25 17:17
 */
public interface PeriodTestService {
    /**
     * 创建阶段测试答题卡
     * @param paperId
     * @param subject
     * @param userId
     * @param terminal
     * @return
     */
    StandardCardVo create(int paperId, int subject, long userId, int terminal,long syllabusId) throws WaitException, BizException;

	PeriodTestDetailVo detail(int paperId, long uid, int subject, long syllabusId) throws BizException;

    /**
     * 查询阶段测交卷次数
     * @param paperId
     * @return
     */
    int getSubmitCount(int paperId, long syllabusId);

    /**
     * 阶段测试交卷次数加1
     * @param paperId
     */
    void incrementPeriodTestAnswerCardCount(int paperId, long syllabusId);

    /**
     * 获取用户试卷信息
     * @param paperId
     * @param uid
     * @param syllabusId
     * @return
     */
	int getPaperStatus(Integer paperId, Integer uid, Integer syllabusId) throws BizException;

	/**
	 * 批量获取用户试卷信息
	 * @param uid
	 * @param paperSyllabusSet
	 * @return
	 */
	Map<String, Integer> getPaperStatusBath(Integer uid, Set<String> paperSyllabusSet);

	/**
	 * 获取试卷答题卡id
	 */
	long getPracticeByPaperId(Integer paperId, long uid, Integer syllabusId);

    /**
     * 获取三级知识点
     * @param pointTrees 知识点树
     * @return
     */
    public List<QuestionPointTree> getThreePoints(List<QuestionPointTree> pointTrees,List<QuestionPointTree> threePointTrees);

    /**
     * 获取教师评语中涉及的信息
     * @param standardCard
     * @return
     */
    public TeacherRemarkVo getTeacherRemark(StandardCard standardCard,AnswerCard answerCard);

    /**
     * 获取答题卡排名
     * @return
     */
    public PeriodTestRankVo getAnswerCards(AnswerCard answerCard);

    /**
     * 重点关注列表
     * @return
     */
    public List<PointFocusDto>  getPointFocus(AnswerCard answerCard);

    /**
     * 成绩分布信息
     * @return
     */
    public List<SocreDistributionDto>  getSocreDistribution(AnswerCard answerCard,long syllabusId);

    /**
     * 填充用户答题卡信息
     * @param answerCard
     */
    public void fillCardUserMeta(AnswerCard answerCard);

    /**
     * 是否在规定时间内作答
     * @param answerCard
     * @return
     */
	public Map<String, Object> getAnswerInTime(AnswerCard answerCard);

    /**
     * 查询阶段测试练习类型
     * @param standardCard
     * @return
     */
    public String getPeriodTypeName(StandardCard standardCard);

    /**
     * 获取班级数据
     */
    public CourseStatisticsInfo getCourseData(int paperId,long syllabusId,long courseId);

    /**
     * 获取最新排名
     * @param standardCard
     * @return
     */
    public long getRank(StandardCard standardCard);

    public List<AnswerCard> getAnswerCardByType(int type);

    /**
     * 获取试卷答题卡id字符串
     */
    String getPracticeIdStrByPaperId(Integer paperId, long uid, Integer syllabusId);

}
