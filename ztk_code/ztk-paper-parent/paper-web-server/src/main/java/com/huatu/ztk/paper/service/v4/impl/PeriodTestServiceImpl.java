package com.huatu.ztk.paper.service.v4.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.entity.subject.Category;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.CardUserMeta;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.bo.PaperBo;
import com.huatu.ztk.paper.controller.MatchControllerV2;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dto.PointFocusDto;
import com.huatu.ztk.paper.dto.QuestionDto;
import com.huatu.ztk.paper.dto.ScoreRankDto;
import com.huatu.ztk.paper.dto.SocreDistributionDto;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.paper.service.v4.HandlerMetaService;
import com.huatu.ztk.paper.service.v4.PeriodTestService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import com.self.generator.core.WaitException;

/**
 * @author shanjigang
 * @date 2019/2/2517:17
 */
@Service
public class PeriodTestServiceImpl implements PeriodTestService {
	
	private static final Logger logger = LoggerFactory.getLogger(MatchControllerV2.class);
    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private PaperUserMetaService paperUserMetaService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperAnswerCardService paperAnswerCardService;
    @Autowired
    private RedisTemplate redisTemplate;

    private final static int DEFAULT_FINISHED_COUNT = 0;
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserDubboService userDubboService;

    @Autowired
    private HandlerMetaService handlerMetaService;
    /**
     * 创建阶段测试答题卡（如果已经有答题卡，则返回存在的答题卡）
     * @param paperId
     * @param subject
     * @param userId
     * @param terminal
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @Override
    @Transactional
    public StandardCardVo create(int paperId, int subject, long userId, int terminal,long syllabusId) throws WaitException, BizException {
        final Paper paper = paperService.findById(paperId);
        if (paper==null){
            throw new BizException(PeriodTestConstant.PAPERID_ISNOT_EXISTS, "试卷不存在");
        }
        EstimatePaper periodTestPaper = (EstimatePaper) paper;
        if (periodTestPaper.getStartTimeIsEffective()==1){
            if (periodTestPaper.getStartTime() > System.currentTimeMillis()){
                throw new BizException(PeriodTestConstant.CANNOT_CREATE_PERIODTEST_ANSWERCARD, "阶段测试活动还没开始不能创建答题卡");
            }
        }

        PaperUserMeta userMeta = paperUserMetaService.findByPaperIdAndSyllabusId(userId, paperId,syllabusId);
        logger.info("period test create error, params, userId:{}, syllabusId:{}, data:{}", userId, syllabusId, JSONObject.toJSONString(userMeta));
        PaperBo paperBo=new PaperBo();
        paperBo.setModules(paper.getModules());
        paperBo.setQuestions(paper.getQuestions());

        if(null == userMeta){
            StandardCard standardCard = paperAnswerCardService.create(paper, subject, userId, terminal);
            //存入大纲id到答题卡中
            answerCardDao.update(standardCard.getId(), Update.update("syllabusId", syllabusId));
            if (periodTestPaper.getStartTimeIsEffective()==1){
                if (periodTestPaper.getEndTime() > standardCard.getCardCreateTime()){
                    //创建答题卡时将自动交卷时间放入zset
                    setPeriodTestSubmitTime(standardCard,periodTestPaper,syllabusId);
                }
            }

            // 将答题卡转换成答题卡vo
            StandardCardVo standardCardVo=new StandardCardVo();
            if (standardCard!=null){
                standardCardVo.setPracticeId(standardCard.getId());
                standardCardVo.setPracticeIdStr(standardCard.getId()+"");
                standardCardVo.setAnswers(standardCard.getAnswers());
                standardCardVo.setCorrects(standardCard.getCorrects());
                standardCardVo.setDoubts(standardCard.getDoubts());
                standardCardVo.setPaper(paperBo);
                standardCardVo.setName(standardCard.getName());
                standardCardVo.setTimes(standardCard.getTimes());
                standardCardVo.setRemainTime(standardCard.getRemainingTime());
                standardCardVo.setStatus(standardCard.getStatus());
                standardCardVo.setLastIndex(standardCard.getLastIndex());
               //创建答题卡--答题数加1
                incrementPeriodTestAnswerCardCount(paperId, syllabusId);
                paperUserMetaService.addPeriodTestPractice(userId, paperId, standardCard.getId(),syllabusId);
            }
            return standardCardVo;
        }
        //继续作答
        long currentPracticeId = getUserPracticeId(userMeta);
        AnswerCard answerCard = answerCardDao.findById(currentPracticeId);
        logger.info("创建阶段测试答题卡,params: id:{}, result:{}", currentPracticeId, JSONObject.toJSONString(answerCard));
        return StandardCardVo.builder().practiceId(answerCard.getId()).answers(answerCard.getAnswers()).practiceIdStr(answerCard.getId()+"")
                    .corrects(answerCard.getCorrects()).corrects(answerCard.getCorrects()).doubts(answerCard.getDoubts())
                   .name(answerCard.getName()).paper(paperBo).times(answerCard.getTimes()).remainTime(answerCard.getRemainingTime()).status(answerCard.getStatus()).lastIndex(answerCard.getLastIndex()).build();
    }
    
    /**
     * 阶段测试首页相关数据
     * @throws BizException 
     */
	@Override
	public PeriodTestDetailVo detail(int paperId, long uid, int subject, long syllabusId) throws BizException {
		Paper paper = paperService.findById(paperId);
		if (paper == null) {// 试卷未找到
			throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "试卷不存在");
		}
		if (paper instanceof EstimatePaper) {
			EstimatePaper periodTestPaper = (EstimatePaper) paper;
			PeriodTestDetailVo periodTestDetailVo = new PeriodTestDetailVo();
			// 转换为vo
			BeanUtils.copyProperties(periodTestPaper, periodTestDetailVo);
			periodTestDetailVo.setDescription(periodTestPaper.getDescrp());
			periodTestDetailVo.setPaperId(periodTestPaper.getId());
			// 获取交卷人数
			periodTestDetailVo.setSubmitCount(getSubmitCount(paperId,syllabusId));
			int status = PeriodTestStatus.NOT_START;
			PaperUserMeta userMeta = paperUserMetaService.findByPaperIdAndSyllabusId(uid, paperId, syllabusId);
			if (userMeta != null) {
				if (userMeta.getCurrentPracticeId() == -1) {
					// 用户已经交卷
					status = PeriodTestStatus.REPORT_AVAILABLE;
					// 此时可以查看报告
					List<Long> practiceIds = userMeta.getPracticeIds();
			        if (CollectionUtils.isNotEmpty(practiceIds)) {
			            periodTestDetailVo.setPracticeId(practiceIds.get(0));
                        periodTestDetailVo.setPracticeIdStr(practiceIds.get(0).toString());
			        }
				}else { // 当前答题卡未交卷
					status = PeriodTestStatus.CONTINUE_AVAILABLE;
					periodTestDetailVo.setPracticeId(userMeta.getCurrentPracticeId());
                    periodTestDetailVo.setPracticeIdStr(String.valueOf(userMeta.getCurrentPracticeId()));
				}
			} else {
				// 未创建答题卡 状态可能为未开始和进行中
				if (periodTestPaper.getStartTimeIsEffective() == 1) {
					// 时间有效
					long currentTimeMillis = System.currentTimeMillis();
					if (currentTimeMillis >= periodTestPaper.getOnlineTime()) {
						// 由于阶段测试不强制强制限定结束时间 因此只需判断到达开考时间即为正在进行
						status = PeriodTestStatus.ONLINE;
					}
				} else {
					// 时间无效
					status = PeriodTestStatus.ONLINE;
				}

			}
			// 获取考试状态
			periodTestDetailVo.setStatus(status);
			logger.info("id:{} return periodTestDetailVo:{}", paperId, periodTestDetailVo);
			return periodTestDetailVo;
		} else {
			throw new BizException(ErrorResult.create(1000001, "试卷类型不符"));
		}

	}

    @Override
    public int getSubmitCount(int paperId,long syllabusId) {
        String createAnswerCardCount = PeriodTestRedisKey.getAnswerCardCount(paperId,syllabusId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(createAnswerCardCount);
        if (null == value) {
            return DEFAULT_FINISHED_COUNT;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    /**
     * 自增1
     *
     * @param paperId
     */
    @Override
    public void incrementPeriodTestAnswerCardCount(int paperId,long syllabusId) {
        String createAnswerCardCount = PeriodTestRedisKey.getAnswerCardCount(paperId,syllabusId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.increment(createAnswerCardCount, 1);
    }

    /**
     * 获取用户答题卡ID
     *
     * @param userMeta
     * @return
     */
    public static long getUserPracticeId(PaperUserMeta userMeta) {
        long currentPracticeId = userMeta.getCurrentPracticeId();
        if (currentPracticeId > 0) {
            return currentPracticeId;
        }
        List<Long> practiceIds = userMeta.getPracticeIds();
        if (CollectionUtils.isNotEmpty(practiceIds)) {
            return practiceIds.get(0);
        }
        return -1;
    }
    
    /**
     * 获取用户指定阶段测试的考试状态
     * @param paper
     * @param uid
     * @throws BizException
     */
	private int getPaperStatus(EstimatePaper paper, long uid, long syllabusId) {
		int paperId = paper.getId();
		PaperUserMeta userMeta = paperUserMetaService.findByPaperIdAndSyllabusId(uid, paperId, syllabusId);
		// 已创建答题卡
		if (userMeta != null) {
			if (userMeta.getCurrentPracticeId() == -1) {
				// 用户已经交卷
				logger.info("试卷:{},用户id:{}对应试卷id:{}大纲id:{}已创建答题卡,试卷考试时间是否有效：{},试卷状态返回:6", paper.getName(), uid,
						paper.getId(), syllabusId, paper.getStartTimeIsEffective());
				return PeriodTestStatus.REPORT_AVAILABLE;
			} else { // 当前答题卡未交卷
				logger.info("试卷:{},用户id:{}对应试卷id:{}大纲id:{}已创建答题卡,试卷考试时间是否有效：{},试卷状态返回:6", paper.getName(), uid,
						paper.getId(), syllabusId, paper.getStartTimeIsEffective());
				return PeriodTestStatus.CONTINUE_AVAILABLE;
			}
		}
		// logger.info("用户id:{}对应试卷id:{}大纲id:{}未创建答题卡", uid, paper.getId(), syllabusId);
		// 未创建答题卡 状态可能为未开始和进行中
		if (paper.getStartTimeIsEffective() == 1) {
			// 时间有效
			long currentTimeMillis = System.currentTimeMillis();
			if (currentTimeMillis >= paper.getOnlineTime()) {
				// 由于阶段测试不强制强制限定结束时间 因此只需判断到达开考时间即为正在进行
				logger.info("试卷:{},用户id:{}对应试卷id:{}大纲id:{}未创建答题卡,试卷考试时间是否有效：{},试卷状态返回:2", paper.getName(), uid,
						paper.getId(), syllabusId, paper.getStartTimeIsEffective());
				return PeriodTestStatus.ONLINE;
			} else {
				logger.info("试卷:{},用户id:{}对应试卷id:{}大纲id:{}未创建答题卡,试卷考试时间是否有效：{},试卷状态返回:1", paper.getName(), uid,
						paper.getId(), syllabusId, paper.getStartTimeIsEffective());
				return PeriodTestStatus.NOT_START;
			}
		}
		logger.info("试卷:{},用户id:{}对应试卷id:{}大纲id:{}未创建答题卡,试卷考试时间是否有效：{},试卷状态返回:2", paper.getName(), uid, paper.getId(),
				syllabusId, paper.getStartTimeIsEffective());
		return PeriodTestStatus.ONLINE;
	}

	@Override
	public int getPaperStatus(Integer paperId, Integer uid, Integer syllabusId) throws BizException {
		Paper paper = paperService.findById(paperId);
		if (paper == null) {// 试卷未找到
			throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "试卷不存在");
		}
		if (paper instanceof EstimatePaper) {
			int status = getPaperStatus((EstimatePaper) paper, uid, syllabusId);
			return status;
		}
		// 默认返回未开始
		return PeriodTestStatus.NOT_START;
	}

	@Override
	public Map<String, Integer> getPaperStatusBath(Integer uid, Set<String> paperSyllabusSet) {
		Map<String, Integer> retMap = Maps.newHashMap();
		paperSyllabusSet.forEach(paperSyllabusStr -> {
			String[] paperSyllabusArray = paperSyllabusStr.split("_");
			if (paperSyllabusArray.length == 2) {
				Integer paperId = Integer.parseInt(paperSyllabusArray[0]);
				Integer syllabusId = Integer.parseInt(paperSyllabusArray[1]);
				Integer status;
				try {
					status = getPaperStatus(paperId, uid, syllabusId);
				} catch (BizException e) {
					status = PeriodTestStatus.NOT_START;
				}
				retMap.put(paperSyllabusStr, status);
			}
		});
		return retMap;
	}

	/**
	 * 获取指定试卷答题卡id
	 */
	@Override
	public long getPracticeByPaperId(Integer paperId, long uid, Integer syllabusId) {
		PaperUserMeta userMeta = paperUserMetaService.findByPaperIdAndSyllabusId(uid, paperId, syllabusId);
		if (userMeta != null) {
			return getUserPracticeId(userMeta);
		} else {
			return -1;
		}
	}

	public List<QuestionPointTree> getThreePoints(List<QuestionPointTree> pointTrees,List<QuestionPointTree> threePointTrees)
    {
        pointTrees.forEach(pointTree->{
            if(CollectionUtils.isNotEmpty(pointTree.getChildren())){
                getThreePoints(pointTree.getChildren(),threePointTrees);
            }else{
                threePointTrees.add(pointTree);
            }
        });
	    return threePointTrees;
    }

    public TeacherRemarkVo getTeacherRemark(StandardCard standardCard,AnswerCard answerCard){
        TeacherRemarkVo teacherRemark=new TeacherRemarkVo();
        final List<QuestionPointTree> pointTrees = questionPointDubboService.questionPointSummaryWithTotalNumber(standardCard.getPaper().getQuestions(), answerCard.getCorrects(), answerCard.getTimes());
        List<QuestionPointTree> threePoints= Lists.newArrayList();
        getThreePoints(pointTrees,threePoints);

        //知识点灵活运用的条数
        int elasticCount=0;
        //知识点了解条数
        int knowCount=0;
        //知识点掌握条数
        int knowWellCount=0;
        //知识点理解条数
        int understandCount=0;
        StringBuilder elasticName=new StringBuilder();
        StringBuilder knowName=new StringBuilder();
        StringBuilder knowWellName=new StringBuilder();
        StringBuilder understandName=new StringBuilder();
        for (int i=0;i<threePoints.size();i++){
            QuestionPointTree questionPointTree=threePoints.get(i);
            if (questionPointTree!=null){
                if (questionPointTree.getAccuracy()>=85&&questionPointTree.getAccuracy()<=100){
                    elasticCount++;
                    elasticName.append(questionPointTree.getName()).append("、");
                }else if(questionPointTree.getAccuracy()>=75&&questionPointTree.getAccuracy()<85){
                    knowWellCount++;
                    knowWellName.append(questionPointTree.getName()).append("、");
                }else if(questionPointTree.getAccuracy()>=55&&questionPointTree.getAccuracy()<=70){
                    understandCount++;
                    understandName.append(questionPointTree.getName()).append("、");
                }else if(questionPointTree.getAccuracy()>=40){
                    knowCount++;
                    knowName.append(questionPointTree.getName()).append("、");
                }
            }
        }
        teacherRemark.setPointCount(threePoints.size());
        teacherRemark.setElasticCount(elasticCount);
        teacherRemark.setTeacherName("华图教研团");
        if (!Strings.isNullOrEmpty(elasticName.toString())&&!"".equals(elasticName.toString())){
            teacherRemark.setElasticName(elasticName.toString().substring(0,elasticName.toString().lastIndexOf("、")));
        }else {
            teacherRemark.setElasticName(elasticName.toString());
        }
        teacherRemark.setKnowCount(knowCount);
        if (!Strings.isNullOrEmpty(knowName.toString())&&!"".equals(knowName.toString())){
            teacherRemark.setKnowName(knowName.toString().substring(0,knowName.toString().lastIndexOf("、")));
        }else {
            teacherRemark.setKnowName(knowName.toString());
        }
        teacherRemark.setKnowWellCount(knowWellCount);
        if (!Strings.isNullOrEmpty(knowWellName.toString())&&!"".equals(knowWellName.toString())){
            teacherRemark.setKnowWellName(knowWellName.toString().substring(0,knowWellName.toString().lastIndexOf("、")));
        }else {
            teacherRemark.setKnowWellName(knowWellName.toString());
        }
        teacherRemark.setUnderstandCount(understandCount);
        if (!Strings.isNullOrEmpty(understandName.toString())&&!"".equals(understandName.toString())){
            teacherRemark.setUnderstandName(understandName.toString().substring(0,understandName.toString().lastIndexOf("、")));
        }else {
            teacherRemark.setUnderstandName(understandName.toString());
        }
        return teacherRemark;
    }

    /**
     * 获取成绩排名前十的答题卡
     * @return
     */
    public PeriodTestRankVo getAnswerCards(AnswerCard answerCard){
        //自己的答题卡信息
        StandardCard standardCard=(StandardCard)answerCard;
        //重新计算排名
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String  paperPracticeIdSore=PaperRedisKeys.getPaperPracticeIdSore(standardCard.getPaper().getId())+"_"+standardCard.getSyllabusId();
        long total =handlerMetaService.getCardUserMetaNum(standardCard);
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", paperPracticeIdSore, total);
        AnswerCardUtil.periodTestReBuildCardMeta(standardCard.getCardUserMeta(),rank,total);

        ScoreRankDto scoreRankDto=new ScoreRankDto();
        UserDto userDto=userDubboService.findById(standardCard.getUserId());
        scoreRankDto.setExpendTime(standardCard.getExpendTime());
        scoreRankDto.setIcon(userDto.getAvatar());
        scoreRankDto.setRank((standardCard.getCardUserMeta() == null ? 0 : standardCard.getCardUserMeta().getRank()));
        scoreRankDto.setScore((int)standardCard.getScore());
        scoreRankDto.setSubmitTime(standardCard.getCreateTime());
        scoreRankDto.setUserId(userDto.getId());
        scoreRankDto.setUserName(userDto.getName());


        Set <String> scoreDesc=zSetOperations.reverseRangeByScore(paperPracticeIdSore,0,200,0,10);
        HashMap<Integer,Long> map=Maps.newHashMap();
        for (String answerCardId:scoreDesc){
            Long  scoreRank = ScoreSortUtil.getRank(zSetOperations, Long.parseLong(answerCardId) + "", paperPracticeIdSore, total);
            map.put(scoreRank.intValue(),Long.parseLong(answerCardId));
        }

        //查询试卷
        final Paper paper = paperService.findById(standardCard.getPaper().getId());
        EstimatePaper periodTestPaper = (EstimatePaper) paper;

        List<Long> ids=Lists.newArrayList();
        for (int i=0;i<scoreDesc.toArray().length;i++ ){
            ids.add(Long.parseLong(String.valueOf(scoreDesc.toArray()[i])));
        }
        //查询排名前十的答题卡
        List<ScoreRankDto> scoreTop=handlerMetaService.getPeriodReportCache(standardCard);
        if (CollectionUtils.isEmpty(scoreTop)){
            Criteria criteria = Criteria.where("type").is(AnswerCardType.FORMATIVE_TEST_ESTIMATE).and("_id").in(ids).and("status").is(AnswerCardStatus.FINISH);
            Query query = new Query(criteria);
            logger.info("query={}", query);
            List<AnswerCard> periodTestAnswers = mongoTemplate.find(query, AnswerCard.class);

            //根据分数排名，从zset中查询；取出答题卡Id，按顺序填充数据
            for (Map.Entry<Integer, Long> entry : map.entrySet()){
                periodTestAnswers.forEach(periodTestAnswer->{
                    if (entry.getValue().equals(periodTestAnswer.getId())){
                        StandardCard standardCard1=(StandardCard)periodTestAnswer;
                        Long rank1 = ScoreSortUtil.getRank(zSetOperations, standardCard1.getId() + "", paperPracticeIdSore, total);
                        AnswerCardUtil.periodTestReBuildCardMeta(standardCard1.getCardUserMeta(),rank1,total);

                        fillCardUserMeta(standardCard1);
                        UserDto user=userDubboService.findById(standardCard1.getUserId());
                        scoreTop.add(ScoreRankDto.builder().expendTime(standardCard1.getExpendTime()).icon(user.getAvatar())
                                .rank(standardCard1.getCardUserMeta()==null?0:standardCard1.getCardUserMeta().getRank())
                                .submitTime(standardCard1.getCreateTime()).userName(user.getNick()).userId(user.getId())
                                .score((int)standardCard1.getScore()).nickName(user.getName()).build());
                    }
                });
            }
            if (!CollectionUtils.isEmpty(scoreTop)){
                handlerMetaService.putPeriodReportCache(scoreTop,standardCard);
            }

        }
        //填充数据
        PeriodTestRankVo periodTestRankVo=new PeriodTestRankVo();
        periodTestRankVo.setComprehensiveRank(scoreTop);
        periodTestRankVo.setSelf(scoreRankDto);

        return periodTestRankVo;
    }
    public List<PointFocusDto>  getPointFocus(AnswerCard answerCard){
        StandardCard card=(StandardCard)answerCard;

        //答题卡中的试题
        List<Integer> questionIds=card.getPaper().getQuestions();

        //统计超过50s
        int passTime=0;
        //用时超过50秒
        List<Integer> timesIds=Lists.newArrayList();
        List<Integer> timesQuestionsIds=Lists.newArrayList();
        int [] timesAnswerstemp=new int[card.getTimes().length];
        HashMap<Integer,Integer> timeMap=Maps.newHashMap();
        for (int i=0;i<card.getTimes().length;i++){
            if(card.getTimes()[i]>50){
                timesAnswerstemp[passTime]=questionIds.get(i);
                timeMap.put(questionIds.get(i),i);
                timesIds.add(i);
                timesQuestionsIds.add(questionIds.get(i));
                passTime++;
            }
        }
        int[] questionIndexs=new int[timesIds.size()];
       for (int i=0;i<timesQuestionsIds.size();i++){
           questionIndexs[i]=timeMap.get(timesQuestionsIds.get(i));
       }
        int [] timesAnswers=new int[timesIds.size()];
        for(int i=0;i<timesAnswerstemp.length;i++){
            if (timesAnswerstemp[i]!=0){
                timesAnswers[i]=timesAnswerstemp[i];
            }
        }
        int[] correctOne=card.getCorrects();
        //获取时间大于50s的试题是否正确
        //超时是否正确
        int [] timeCorrects=new int[timesIds.size()];
        for(int i=0;i<timesIds.size();i++){
            for (int j=0;j<correctOne.length;j++){
                if (timesIds.get(i)==j){
                    timeCorrects[i]=correctOne[j];
                }
            }
        }
        List<QuestionDto> timeQuestions=Lists.newArrayList();
        List<PointFocusDto> pointFocus=Lists.newArrayList();
        QuestionDto timeQuestion=new QuestionDto();
        timeQuestion.setCorrects(timeCorrects);
        timeQuestion.setQuestionIds(timesAnswers);
        timeQuestion.setQuestionIndexs(questionIndexs);
        timeQuestions.add(timeQuestion);
        PointFocusDto timePass50=new PointFocusDto();
        timePass50.setQuestions(timeQuestions);
        timePass50.setTypeText(PeriodTestConstant.PASSFIFTYVALUE);
        timePass50.setTypeValue(PeriodTestConstant.PASSFIFTYTYPE);
        pointFocus.add(timePass50);

        //未作答
        int undoNum=0;
        List<Integer> undoIds=Lists.newArrayList();
        HashMap<Integer,Integer> undoMap=Maps.newHashMap();
        List<Integer> undoQuestionsIds=Lists.newArrayList();
        for (int j=0;j<correctOne.length;j++){
            if (correctOne[j]==0){
                undoIds.add(j);
                undoNum++;
                undoMap.put(questionIds.get(j),j);
                undoQuestionsIds.add(questionIds.get(j));
            }
        }
        int [] undoAnswers=new int[undoIds.size()];
        int[] undoIndexs=new int[undoIds.size()];

        for (int i=0;i<undoQuestionsIds.size();i++){
          undoAnswers[i]=undoQuestionsIds.get(i);
          undoIndexs[i]=undoMap.get(undoQuestionsIds.get(i));
        }
        //超时是否正确
        int [] undoCorrects=new int[undoIds.size()];
        //标记试题没有作答
        for(int i=0;i<undoIds.size();i++){
            undoCorrects[i]=0;
        }
        List<QuestionDto> undoQuestions=Lists.newArrayList();
        QuestionDto undoQuestion=new QuestionDto();
        undoQuestion.setCorrects(undoCorrects);
        undoQuestion.setQuestionIds(undoAnswers);
        undoQuestion.setQuestionIndexs(undoIndexs);
        undoQuestions.add(undoQuestion);
        PointFocusDto undoFocus=new PointFocusDto();
        undoFocus.setQuestions(undoQuestions);
        undoFocus.setTypeText(PeriodTestConstant.UNDOVALUE);
        undoFocus.setTypeValue(PeriodTestConstant.UNDOTYPE);
        pointFocus.add(undoFocus);

        //查询难度系数>0.5的试题
        List <Question> questionList= questionDubboService.findBatchV3(questionIds);
        List<Integer> difficultyIds=Lists.newArrayList();
        questionList.forEach(question -> {
            GenericQuestion  genericQuestion=(GenericQuestion)question;
            if(genericQuestion.getMeta().getPercents()[genericQuestion.getMeta().getRindex()]<=50){
                difficultyIds.add(question.getId());
            }
        });
        int[] difficultyIndex=new int[difficultyIds.size()];
        int [] difficultyAnswers=new int[difficultyIds.size()];

        for (int j=0;j<difficultyIds.size();j++){
              for(int i=0;i<questionIds.size();i++){
                    if(questionIds.get(i).equals(difficultyIds.get(j))){
                        difficultyIndex[j]=i;
                        difficultyAnswers[j]=questionIds.get(i);
                    }
              }

          }

        int [] difficultCorrects=new int[difficultyIds.size()];

        for(int i=0;i<difficultyIndex.length;i++){
            for (int j=0;j<correctOne.length;j++){
                if (difficultyIndex[i]==j){
                    difficultCorrects[i]=correctOne[j];
                }
            }
        }

        QuestionDto difficultyQuestion=new QuestionDto();
        difficultyQuestion.setCorrects(difficultCorrects);
        difficultyQuestion.setQuestionIds(difficultyAnswers);
        difficultyQuestion.setQuestionIndexs(difficultyIndex);
        List<QuestionDto> difficultyQuestions=Lists.newArrayList();
        difficultyQuestions.add(difficultyQuestion);
        PointFocusDto difficulty=new PointFocusDto();
        difficulty.setQuestions(difficultyQuestions);
        difficulty.setTypeText(PeriodTestConstant.DIFFCULTYVALUE);
        difficulty.setTypeValue(PeriodTestConstant.DIFFCULTYTYPE);
        pointFocus.add(difficulty);
        return pointFocus;
    }

    public List<SocreDistributionDto>  getSocreDistribution(AnswerCard answerCard,long syllabusId){
        StandardCard standardCard=(StandardCard)answerCard;
        Map<Object ,Object > map=redisTemplate.opsForHash().entries(PeriodTestRedisKey.getPeriodTestPaper(((StandardCard) answerCard).getPaper().getId(),syllabusId));
       logger.info("map {}",map);
        //自己得分
        int selfScore=(int) standardCard.getScore();
        //分数跟自己相同的人数
        int selfNum=0;
        //人数最多对应的分
        int maxNumScore=0;
        //人数临时变量
        int numTemp=0;
        //人数最多分的人数
        int scoreNum=0;
        //最高分
        int maxScore=0;
        //分数临时变量
        int scoreTemp=0;
        //最高分对应的人数
        int maxScoreNum=0;
        //最低分
        int minScore=0;
        //最低分对应的人数
        int minScoreNum=0;
        for (Map.Entry<Object , Object> entry : map.entrySet()) {
            numTemp=Integer.parseInt(entry.getValue().toString());
            
            Double double2=Double.parseDouble(entry.getKey().toString());
            scoreTemp=double2.intValue();
            if (scoreTemp>maxScore){
                maxScore=scoreTemp;
                maxScoreNum=Integer.parseInt(entry.getValue().toString());
            }
            
            if (numTemp >= scoreNum){
                scoreNum=numTemp;
                Double double1=Double.parseDouble(entry.getKey().toString());
                if(double1.intValue() > maxNumScore) {
                	maxNumScore= double1.intValue();
                }
            }

            Double double3=Double.parseDouble(entry.getKey().toString());
            if (double3.intValue()==0){
                minScoreNum=Integer.parseInt(entry.getValue().toString());
            }

            Double double4=Double.parseDouble(entry.getKey().toString());
            int myScore=double4.intValue();
            if (myScore==selfScore){
                selfNum=Integer.parseInt(entry.getValue().toString());
            }
        }


        List<SocreDistributionDto> scoreDistributionDtos=Lists.newArrayList();
        //自己
        SocreDistributionDto self=new SocreDistributionDto();
        self.setBeatRatio(standardCard.getCardUserMeta().getBeatRate());
        self.setCount(selfNum);
        self.setScore((int)answerCard.getScore());
        self.setIsSelf(true);
        scoreDistributionDtos.add(self);

        //分数为0的
        addWithCheckRedundant(scoreDistributionDtos,minScore,minScoreNum);

        //分数相同最多的
        addWithCheckRedundant(scoreDistributionDtos,maxNumScore,scoreNum);

        //最高分
        addWithCheckRedundant(scoreDistributionDtos,maxScore,maxScoreNum);
        return scoreDistributionDtos;
    }

    /**
     * @param scoreDistributionDtos
     * @param score
     * @param count
     * @return
     */
    private void addWithCheckRedundant(List<SocreDistributionDto> scoreDistributionDtos, int score,int count) {
        Optional<SocreDistributionDto> any = scoreDistributionDtos.stream().filter(i -> i.getScore() == score).findAny();

        if(!any.isPresent()){
            SocreDistributionDto socreDistributionDto=new SocreDistributionDto();
            socreDistributionDto.setBeatRatio(0);
            socreDistributionDto.setCount(count);
            socreDistributionDto.setScore(score);
            socreDistributionDto.setIsSelf(false);
            scoreDistributionDtos.add(socreDistributionDto);
        }
    }

    /**
     * 填充用户答题卡信息
     * @param answerCard
     */
    public void fillCardUserMeta(AnswerCard answerCard){
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            final CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta(standardCard);
            standardCard.setCardUserMeta(cardUserMeta);
        }
    }

	public Map<String, Object> getAnswerInTime(AnswerCard answerCard) {
		Map<String, Object> retMap = Maps.newHashMap();
		retMap.put("isAnswerInTime", false);
		StandardCard standardCard = (StandardCard) answerCard;
		final Paper paper = paperService.findById(standardCard.getPaper().getId());
		EstimatePaper periodTestPaper = (EstimatePaper) paper;
		retMap.put("startTimeIsEffective", periodTestPaper.getStartTimeIsEffective());
		if (periodTestPaper.getStartTimeIsEffective() == 1) {
			if (periodTestPaper.getEndTime() > (standardCard.getCardCreateTime())) {
				retMap.put("isAnswerInTime", true);
			}
		}
		return retMap;
	}

    /**
     * 将答题卡id 和强制交卷时间放入zset
     * @param standardCard
     */
	public void setPeriodTestSubmitTime(StandardCard standardCard, EstimatePaper paper, long syllabusId) {
		ZSetOperations zSetOperations = redisTemplate.opsForZSet();
		Long endTime = paper.getEndTime() + PeriodTestConstant.AFTERSEVENDAY;
		String setValueString = standardCard.getId() + "_" + syllabusId;
		zSetOperations.add(PeriodTestRedisKey.getPeriodTestAnswerCardUnfinshKey(), setValueString, endTime);
	}

    public String getPeriodTypeName(StandardCard standardCard){
        //练习类型
        String periodTestName=standardCard.getName();
        Paper paper = paperService.findById(standardCard.getPaper().getId());
        EstimatePaper periodTestPaper = (EstimatePaper) paper;
        Date date=new Date(periodTestPaper.getStartTime());
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd");
        String startTime=formatter.format(date);
        periodTestName=periodTestName+" "+startTime;
        return periodTestName;
    }

    /**
     * 获取交卷人数
     * @param paperId
     * @return
     */
    public int getSubmit(int paperId,long syllabusId){
        int submitNum=getSubmitCount(paperId,syllabusId);
        return submitNum;
    }

    /**
     * 获取班级数据
     */
    public CourseStatisticsInfo getCourseData(int paperId,long syllabusId,long courseId){
        HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
        Map<Object, Object> map = opsForHash.entries(PeriodTestRedisKey.getPeriodTestAccuracyNum(paperId,syllabusId));
        int ltFifty=0;
        int ltEighty=0;
        int gtEighty=0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (PeriodTestConstant.LTFIFTY.equals(entry.getKey())){
                ltFifty=Integer.parseInt(entry.getValue().toString());
            }
            if (PeriodTestConstant.LTEIGHTY.equals(entry.getKey())){
                ltEighty=Integer.parseInt(entry.getValue().toString());
            }
            if (PeriodTestConstant.GTEIGHTY.equals(entry.getKey())){
                gtEighty=Integer.parseInt(entry.getValue().toString());
            }
        }

        //交卷人数
        int submitNum= getSubmit(paperId,syllabusId);
        if (submitNum==0){
            submitNum=1;
        }
        int totalAcuuracy=0;
        Map<Object, Object> accuracyMap = opsForHash.entries(PeriodTestRedisKey.getPeriodTestQuestionAccuracy(paperId,syllabusId));
        for (Map.Entry<Object, Object> accuracyEntry : accuracyMap.entrySet()){
            totalAcuuracy=Integer.parseInt(accuracyEntry.getValue().toString());
        }
        int averageAccuracy= totalAcuuracy/submitNum;

        int accuracy=0;
        final ValueOperations valueOperations = redisTemplate.opsForValue();
        //答对的总题数
       // Integer rightNum=Integer.parseInt(valueOperations.get("course:practice:roomRightQuestionSum:"+courseId).toString());
        //答题总人数
        //Integer answerNum=Integer.parseInt(valueOperations.get("course:practice:roomAllUserSum:"+courseId).toString());
        //int qcount=0;
        //List<AnswerCard> answerCards=answerCardDao.getCardsByCourseTypeAndCourseId(courseId,2);
//        if (CollectionUtils.isNotEmpty(answerCards)){
//            qcount=answerCards.get(0).getAnswers().length;
//        }
//        if (qcount!=0){
//            accuracy=rightNum/(answerNum * qcount) * 100;
//        }
        logger.info("小于50：{},小于80：{},大于80：{},平均正确率：{}",(ltFifty/submitNum*100),(ltEighty/submitNum*100),(gtEighty/submitNum*100),averageAccuracy);
        return CourseStatisticsInfo.builder().ltFifty(ltFifty/submitNum*100).ltEighty(ltEighty/submitNum*100).gtEighty(gtEighty/submitNum*100).averageAccuracy(averageAccuracy).submitNum(submitNum).accuracy(accuracy).build();
    }

    public long getRank(StandardCard standardCard){
        //重新计算排名
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String  paperPracticeIdSore=PaperRedisKeys.getPaperPracticeIdSore(standardCard.getPaper().getId())+"_"+standardCard.getSyllabusId();
        long total =handlerMetaService.getCardUserMetaNum(standardCard);
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", paperPracticeIdSore, total);
        return rank;
    }

    public List<AnswerCard> getAnswerCardByType(int type){
        return answerCardDao.getCardByType(type);
    }

    /**
     * 获取指定试卷答题卡id
     */
    @Override
    public String getPracticeIdStrByPaperId(Integer paperId, long uid, Integer syllabusId) {
        PaperUserMeta userMeta = paperUserMetaService.findByPaperIdAndSyllabusId(uid, paperId, syllabusId);
        if (userMeta != null) {
            return getUserPracticeIdStr(userMeta);
        } else {
            return "";
        }
    }

    /**
     * 获取用户答题卡ID
     *
     * @param userMeta
     * @return
     */
    public static String getUserPracticeIdStr(PaperUserMeta userMeta) {
        long currentPracticeId = userMeta.getCurrentPracticeId();
        String practiceIdStr="";
        if (currentPracticeId > 0) {
            practiceIdStr=String.valueOf(currentPracticeId);
            return practiceIdStr;
        }
        List<Long> practiceIds = userMeta.getPracticeIds();
        if (CollectionUtils.isNotEmpty(practiceIds)) {
            practiceIdStr=String.valueOf(practiceIds.get(0));
            return practiceIdStr;
        }
        return practiceIdStr;
    }
}
