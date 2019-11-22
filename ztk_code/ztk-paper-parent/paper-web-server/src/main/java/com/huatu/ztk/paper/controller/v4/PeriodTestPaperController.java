package com.huatu.ztk.paper.controller.v4;

import static com.huatu.ztk.paper.service.PaperAnswerCardService.sortPointTree;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.bo.PaperBo;
import com.huatu.ztk.paper.common.PeriodTestConstant;
import com.huatu.ztk.paper.dto.PointFocusDto;
import com.huatu.ztk.paper.dto.SocreDistributionDto;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.paper.service.v4.PeriodTestService;
import com.huatu.ztk.paper.vo.PeriodTestDetailVo;
import com.huatu.ztk.paper.vo.PeriodTestRankVo;
import com.huatu.ztk.paper.vo.PeriodTestReportTwoVo;
import com.huatu.ztk.paper.vo.PeriodTestReportVo;
import com.huatu.ztk.paper.vo.StandardCardVo;
import com.huatu.ztk.paper.vo.TeacherRemarkVo;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;

/**
 * 阶段测试考试相关
 * 
 * @author zhangchong
 *
 */
@RestController
@RequestMapping(value = "/v4/periodTest", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PeriodTestPaperController {

	private final static Logger logger = LoggerFactory.getLogger(PeriodTestPaperController.class);

	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private PaperAnswerCardService paperAnswerCardService;

	@Autowired
	private PaperUserMetaService paperUserMetaService;

	@Autowired
	private PeriodTestService periodTestService;

	@Autowired
	private QuestionPointDubboService questionPointDubboService;

	@Autowired
	private PaperService paperService;
	/**
	 * 阶段测试首页详情
	 * 
	 * @param token
	 * @param cv
	 * @param terminal
	 * @param paperId
	 * @param subject
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "{paperId}", method = RequestMethod.GET)
	public Object detail(@RequestHeader(required = true) String token, @RequestHeader(required = false) String cv,
			@RequestHeader(required = false) int terminal, @PathVariable Integer paperId,
			@RequestHeader(defaultValue = "-1") int subject,@RequestParam("syllabusId") Long syllabusId) throws BizException {

		logger.info("periodTest detail params: token={},cv={},termnal={},subject={},paperId={},syllabusId={}", token, cv, terminal,
				subject, paperId,syllabusId);
		userSessionService.assertSession(token);
		if (subject == -1) {
			subject = userSessionService.getSubject(token);
		}
		long uid = userSessionService.getUid(token);
		PeriodTestDetailVo detail = periodTestService.detail(paperId, uid, subject,syllabusId);
		if (detail == null) {
			return CommonErrors.RESOURCE_NOT_FOUND;
		}
		return detail;
	}

	/**
	 * 创建阶段测试答题卡|查询答题卡（开始考试和继续考试）
	 *
	 * @param paperId
	 * @param token
	 * @param cv
	 * @param terminal
	 * @param subject
	 * @return
	 * @throws WaitException
	 * @throws BizException
	 */
	@RequestMapping(value = "/practice/{paperId}", method = RequestMethod.POST)
	public Object createPeriodTestAnswerCard(  @PathVariable int paperId,
												@RequestHeader(required = true) String token,
												@RequestHeader(required = false) String cv,
												@RequestHeader(required = false) int terminal,
												@RequestHeader(defaultValue = "-1") int subject,
											    @RequestParam("syllabusId") Long syllabusId) throws WaitException, BizException {
		logger.info("createPeriodTestAnswerCard's params: paperId={},token={},cv={},termnal={},subject={}", paperId, token, cv, terminal, subject);
		if (null == syllabusId) {
			throw new BizException(PeriodTestConstant.SYLLABUSID_ISNOT_NULL, "大纲Id不能为空");

		}
		userSessionService.assertSession(token);
		if (subject == -1) {
			subject = userSessionService.getSubject(token);
		}
		long userId = userSessionService.getUid(token);
		final StandardCardVo standardCardVo = periodTestService.create(paperId, subject, userId, terminal,syllabusId);
		if (standardCardVo == null) {
			return CommonErrors.RESOURCE_NOT_FOUND;
		}
		return standardCardVo;
	}
	/**
	 * 全部做题的保存答案接口 5题提交一次
	 *
	 * @param practiceId 答题卡Id
	 * @param answers
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{practiceId}/answer", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
		public Object submitAnswers( @PathVariable long practiceId,
									  @RequestBody List<Answer> answers,
									  @RequestHeader(required = true) String token,
									  @RequestHeader(required = false) String cv,
									  @RequestHeader(required = false) int terminal,
									  @RequestHeader(defaultValue = "-1") int subject) throws BizException {
		userSessionService.assertSession(token);
		long userId = userSessionService.getUid(token);
		final int area = userSessionService.getArea(token);
		paperAnswerCardService.periodTestsubmitAnswers(practiceId, userId, answers, area);
		Map map = new HashMap(4);
		map.put("message", "答案保存成功");
		return map;
	}

    /**
     * 阶段测试交卷
     * @param token
     * @param practiceId
     * @param answers
     * @param terminal
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/{practiceId}", method = RequestMethod.POST)
    public Object submitPeriodTestPaper(@RequestHeader(required = false) String token,
                                      @PathVariable long practiceId,
                                      @RequestBody List<Answer> answers,
                                      @RequestHeader int terminal,
                                      @RequestParam(defaultValue = "-1") int time,
                                      @RequestParam("syllabusId") Long syllabusId) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        final String uName = userSessionService.getUname(token);
		logger.debug("submitPeriodTestPaper params, practiceId:{}, userId:{}", practiceId, userId);
        Object result = paperAnswerCardService.submitPeriodTestAnswerCard(practiceId, userId, answers, area,syllabusId, uName);
        /**
         * pc端总用时使用time字段(暂保留)
         */
        if (-1 != time && result instanceof StandardCard) {
            int expendTime = ((StandardCard) result).getExpendTime();
            int remainingTime = ((StandardCard) result).getRemainingTime();
            int totalTime = expendTime + remainingTime;
            ((StandardCard) result).setExpendTime(time);
            ((StandardCard) result).setRemainingTime(totalTime - time);
        }
        /**
         * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
         * update by lijun 2018-05-30
         */
        if (null != result && result instanceof AnswerCard) {
            sortPointTree((AnswerCard) result);
        }
		Map map = new HashMap(4);
		map.put("message", "交卷成功");
		return map;
    }

	/**
	 * 根据答题卡Id查询基础报告
	 *
	 * @param token
	 * @param practiceId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{practiceId}/base/report", method = RequestMethod.GET)
	public Object getPeriodTestBaseReport(@PathVariable long practiceId,
										  @RequestHeader(required = true) String token,
										  @RequestHeader(required = false) String cv,
										  @RequestHeader(required = false) int terminal,
										  @RequestHeader(defaultValue = "-1") int subject) throws BizException {
		userSessionService.assertSession(token);
		final long uid = userSessionService.getUid(token);
		logger.info("userId={}",uid);
		AnswerCard answerCard = paperAnswerCardService.findPeriodTestAnswerCardById(practiceId, uid);
		BigDecimal bigDecimal = new BigDecimal(answerCard.getScore());
		double score = bigDecimal.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();
		answerCard.setScore(score);
		/**
		 * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
		 * update by lijun 2018-05-30
		 */
		sortPointTree(answerCard);
		Map<String, Object> answerInTimeMap = periodTestService.getAnswerInTime(answerCard);
		Boolean isInTime = MapUtils.getBoolean(answerInTimeMap, "isAnswerInTime");
		Integer startTimeIsEffective = MapUtils.getInteger(answerInTimeMap, "startTimeIsEffective");
		StandardCard standardCard=(StandardCard)answerCard;
		TeacherRemarkVo teacherRemark=new TeacherRemarkVo();
		if (isInTime){
			teacherRemark=periodTestService.getTeacherRemark(standardCard,answerCard);
		}
        PaperBo paperBo=new PaperBo();
        paperBo.setModules(standardCard.getPaper().getModules());
        paperBo.setQuestions(standardCard.getPaper().getQuestions());
		String periodTypeName=periodTestService.getPeriodTypeName(standardCard);
		long rank=periodTestService.getRank(standardCard);
		Integer averageTime=0;
		Integer accuracy=0;
		Integer answerNum=((StandardCard) answerCard).getPaper().getQcount()-answerCard.getUcount();
		if (answerNum!=0){
			averageTime=answerCard.getExpendTime()/answerNum;
			accuracy=answerCard.getRcount() * 100 /answerNum ;
		}
		// 格式化答案
		String[] answers = answerCard.getAnswers();
		for (int i = 0; i < answers.length; i++) {
			if (StringUtils.isEmpty(answers[i])) {
				answers[i] = "0";
			}
		}
		PeriodTestReportVo periodTestReport = PeriodTestReportVo.builder().answers(answerCard.getAnswers())
				.averageScore((int) standardCard.getCardUserMeta().getAverage())
				.beatRate((int) standardCard.getCardUserMeta().getBeatRate()).corrects(answerCard.getCorrects())
				.doubts(answerCard.getDoubts()).expendTime(answerCard.getExpendTime())
				.maxScore((int) standardCard.getCardUserMeta().getMax()).paper(paperBo).name(answerCard.getName())
				.practiceId(answerCard.getId()).qcount(standardCard.getPaper().getQcount())
				.questionPointTrees(answerCard.getPoints()).rNum(answerCard.getRcount()).rank(rank)
				.remainTime(answerCard.getRemainingTime()).reportTime(System.currentTimeMillis())
				.score(((StandardCard) answerCard).getPaper().getScore()).submitTime(standardCard.getCreateTime())
				.teacherRemark(teacherRemark).times(answerCard.getTimes()).typeInfo(periodTypeName)
				.unum(answerCard.getUcount()).userScore((int) answerCard.getScore())
				.startTimeIsEffective(startTimeIsEffective).wnum(answerCard.getWcount()).isViewAllReport(isInTime)
				.averageTime(averageTime).accuracy(accuracy).build();
		return periodTestReport;
	}

	/**
	 * 获取用户指定试卷状态
	 * @param uid
	 * @param name
	 * @param paperId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{userId}/{syllabusId}/{paperId}", method = RequestMethod.GET)
	public Object getPaperStatus(@PathVariable("userId") Integer uid, @PathVariable("syllabusId") Integer syllabusId,
			@PathVariable("paperId") Integer paperId) throws BizException {
		logger.info("getPaperStatus param userId={}, syllabusId={}, paperId={}", uid, syllabusId, paperId);
		return periodTestService.getPaperStatus(paperId, uid, syllabusId);
	}

	/**
	 * 批量获取试卷状态（用户做题情况）
	 * @param uid
	 * @param paperSyllabusSet paperId_SyllabusId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/getPaperStatusBath/{userId}", method = RequestMethod.POST)
	public Object getPaperStatusBath(@PathVariable("userId") Integer uid,@RequestBody Set<String> paperSyllabusSet) throws BizException {
		logger.info("getPaperStatus param userId={}, paperSyllabusSet={}", uid, paperSyllabusSet);
		return periodTestService.getPaperStatusBath(uid, paperSyllabusSet);
	}
	/**
	 * 获取试卷答题卡id
	 * @param uid
	 * @param syllabusId
	 * @param paperId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{syllabusId}/{paperId}/getPracticeId", method = RequestMethod.GET)
	public Object getPracticeByPaperId( @RequestHeader(required = true) String token, @PathVariable("syllabusId") Integer syllabusId,
			@PathVariable("paperId") Integer paperId) throws BizException {
		userSessionService.assertSession(token);
		final long uid = userSessionService.getUid(token);
		logger.info("getPracticeByPaperId param userId={}, syllabusId={}, paperId={}", uid, syllabusId, paperId);
		return periodTestService.getPracticeByPaperId(paperId, uid, syllabusId);
	}

	/**
	 * 根据答题卡Id查询报告(剩余部分)
	 *
	 * @param token
	 * @param practiceId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{practiceId}/report", method = RequestMethod.GET)
	public Object getPeriodTestReport(@PathVariable long practiceId,
										  @RequestHeader(required = true) String token,
										  @RequestHeader(required = false) String cv,
										  @RequestHeader(required = false) int terminal,
										  @RequestHeader(defaultValue = "-1") int subject,
                                          @RequestParam("syllabusId") Long syllabusId) throws BizException{
		userSessionService.assertSession(token);
		final long uid = userSessionService.getUid(token);
		logger.info("userId={}",uid);
		AnswerCard answerCard = paperAnswerCardService.findPeriodTestAnswerCardById(practiceId, uid);
		Map<String, Object> answerInTimeMap = periodTestService.getAnswerInTime(answerCard);
		Boolean isInTime = MapUtils.getBoolean(answerInTimeMap, "isAnswerInTime");
		PeriodTestRankVo scoreRank=new PeriodTestRankVo();
		List<PointFocusDto> pointFocus= Lists.newArrayList();
		List<SocreDistributionDto> socreDistribution=Lists.newArrayList();
		if (isInTime){
			//排名
			scoreRank=periodTestService.getAnswerCards(answerCard);
			//重点关注列表
			pointFocus= periodTestService.getPointFocus(answerCard);
			//成绩分布信息
			socreDistribution=periodTestService.getSocreDistribution(answerCard,syllabusId);
		}

		return PeriodTestReportTwoVo.builder().scoreTop(scoreRank).focusList(pointFocus).socreDistribution(socreDistribution).build();
	}
	
	/**
	 * 蓝色后台获取某个试卷统计信息
	 * @param paperId
	 * @param syllabusId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{paperId}/getCourseStatisticsInfo", method = RequestMethod.GET)
	public Object getCourseStatisticsInfo(@PathVariable Integer paperId,@RequestParam("syllabusId") Long syllabusId,@RequestParam("courseId") Long courseId,@RequestParam("roomId") Long roomId) throws BizException{
		
		return periodTestService.getCourseData(paperId, syllabusId,courseId);
	}

	/**
	 * 获取试卷答题卡id
	 * @param uid
	 * @param syllabusId
	 * @param paperId
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/{syllabusId}/{paperId}/getPracticeIdStr", method = RequestMethod.GET)
	public Object getPracticeIdStrByPaperId( @RequestHeader(required = true) String token, @PathVariable("syllabusId") Integer syllabusId,
										@PathVariable("paperId") Integer paperId) throws BizException {
		userSessionService.assertSession(token);
		final long uid = userSessionService.getUid(token);
		logger.info("getPracticeByPaperId param userId={}, syllabusId={}, paperId={}", uid, syllabusId, paperId);
		String practiceId=periodTestService.getPracticeIdStrByPaperId(paperId, uid, syllabusId);
		HashMap<String,String> map = Maps.newHashMap();
		map.put("practiceId", practiceId);
		return map;
	}
}
