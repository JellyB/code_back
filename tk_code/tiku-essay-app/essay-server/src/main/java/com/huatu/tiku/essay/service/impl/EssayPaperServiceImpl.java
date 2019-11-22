package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.CommonErrors;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.CommonRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.PaperRedisKey;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.WhiteRedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.match.EssayMockTypeConstant;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant.EssayQuestionBizStatusEnum;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.MockTagEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.admin.EssayAnalyzeUtil;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.util.video.YunUtil;
import com.huatu.tiku.essay.vo.admin.AdminPaperVO;
import com.huatu.tiku.essay.vo.admin.AdminPaperWithQuestionVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionVO;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionVO;
import com.huatu.tiku.essay.vo.admin.answer.AdminPaperAnswerCountVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS;

/**
 * Created by huangqp on 2017\11\23 0023.
 */
@Slf4j
@Service
public class EssayPaperServiceImpl implements EssayPaperService {
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayAreaRepository essayAreaRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayStandardAnswerRuleRepository essayStandardAnswerRuleRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    EssayMaterialService essayMaterialService;
    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayQuestionMaterialRepository essayQuestionMaterialRepository;
    @Autowired
    BjyHandler bjyHandler;
    @Autowired
    EssayStandardAnswerFormatRepository essayStandardAnswerFormatRepository;
    @Autowired
    EssayStandardAnswerKeyPhraseRepository essayStandardAnswerKeyPhraseRepository;
    @Autowired
    EssayStandardAnswerKeyWordRepository essayStandardAnswerKeyWordRepository;
    @Autowired
    EssayStandardAnswerRuleSpecialStripRepository essayStandardAnswerRuleSpecialStripRepository;
    @Autowired
    EssayStandardAnswerRuleStripSegmentalRepository essayStandardAnswerRuleStripSegmentalRepository;
    @Autowired
    EssayStandardAnswerRuleWordNumRepository essayStandardAnswerRuleWordNumRepository;
    @Autowired
    EssayStandardAnswerSplitWordRepository essayStandardAnswerSplitWordRepository;
    @Autowired
    EssayCorrectImageRepository essayCorrectImageRepository;

    //缓存地区列表
    private static final Cache<String, LinkedList<EssayQuestionAreaVO>> areaListCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)//缓存时间
                    .maximumSize(2)
                    .build();


    @Override
    public List<EssayPaperVO> findPaperListByArea(long areaId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum, Pageable pageable) throws BizException {
        List<Long> areaIds = findAreaIds(areaId);
        log.info("areaIds = {}", areaIds);
        if (CollectionUtils.isEmpty(areaIds)) {
            log.info("地区id列表为空");
            throw new BizException(EssayErrors.NO_AREA_LIST);
        }
        //查询某一个地区下的所有试卷信息
        //缓存试卷基本信息，地区对照试卷id
        List<EssayPaperBase> essayPaperBaseList = Lists.newArrayList();
        //1.根据地区id获取试卷列表
        String paperListOfAreaKey = CommonRedisKeyConstant.getPaperListOfAreaKey(areaId);
        essayPaperBaseList = (List<EssayPaperBase>) redisTemplate.opsForValue().get(paperListOfAreaKey);
        //2.缓存数据为空，mysql查询，且放入缓存（失效时间5分钟）
        if (CollectionUtils.isEmpty(essayPaperBaseList)) {
            essayPaperBaseList = essayPaperBaseRepository.findByStatusAndBizStatusAndAreaIdIn(CHECK_PASS.getStatus(), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), areaIds, pageable);
            if (CollectionUtils.isNotEmpty(essayPaperBaseList)) {
                redisTemplate.opsForValue().set(paperListOfAreaKey, essayPaperBaseList);
                redisTemplate.expire(paperListOfAreaKey, 5, TimeUnit.MINUTES);
            }
        }
        //统计试卷id
        List<Long> paperIds = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(essayPaperBaseList)) {
            essayPaperBaseList.forEach(i -> {
                paperIds.add(i.getId());
            });
        } else {
            log.error("paper is not existed in area {}", areaId);
            return Lists.newArrayList();
        }

        //用户在白名单内，从缓存中获取试卷的id
        String whiteListKey = WhiteRedisKeyConstant.getWhiteList();
        Set<Integer> userList = redisTemplate.opsForSet().members(whiteListKey);

        //判断用户在白名单中
        checkUserIdAccess(areaId, userId, essayPaperBaseList, paperIds, userList);

        Map<Long, List<EssayPaperAnswer>> paperAnswerMap = convertPaperIds2PaperAnswerMap(userId, paperIds,modeTypeEnum);
        List<EssayPaperVO> papers = new ArrayList(essayPaperBaseList.size() * 4 / 3 + 1);

        //对试卷进行排序  ("paperYear","paperDate","areaId","subAreaId")
        sort(essayPaperBaseList);


        for (EssayPaperBase paperBase : essayPaperBaseList) {
            EssayPaperVO paper = EssayPaperVO.builder()
                    .areaId(paperBase.getAreaId())
                    .paperName(paperBase.getName()).limitTime(paperBase.getLimitTime()).score(paperBase.getScore())
                    .videoAnalyzeFlag(paperBase.getVideoAnalyzeFlag())
                    .build();
            paper.setPaperId(paperBase.getId());
            if (-1 != userId) {
                List<EssayPaperAnswer> answers = paperAnswerMap.get(paperBase.getId());
                if (!CollectionUtils.isEmpty(answers)) {
                    getUserPaperAnswerStatus(answers, paper);
                }
                paper.setCorrectSum(essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndAnswerCardType(paperBase.getId(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType()).intValue());

            }
            papers.add(paper);
        }
        return papers;
    }


    /**
     * 根据用户做过的该试卷的历史答题卡确定试卷最近的答题卡，和批改状态
     *
     * @param answers
     * @param paper
     */
    private void getUserPaperAnswerStatus(List<EssayPaperAnswer> answers, EssayPaperVO paper) {
        int times = 0;
        EssayPaperAnswer last = null;
        for (EssayPaperAnswer answer : answers) {
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == answer.getBizStatus()) {
                times++;
            }
            if (last == null || last.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                last = answer;
            }
        }
        paper.setCorrectNum(times);
        if (last != null) {
            paper.setRecentStatus(last.getBizStatus());
        } else {
            paper.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
    }

    /**
     * 根据用户做过的该试卷的历史答题卡确定试卷最近的答题卡，和批改状态
     * 包括人工批改对应的答题卡状态
     *
     * @param answers
     * @param paper
     */
    @Override
    public void buildManualCorrectExtendInfo(List<EssayPaperAnswer> answers, EssayPaperVO paper) {
        int correctNum = 0;// 智能批改次数
        int manualNum = 0;// 人工批改次数
        EssayPaperAnswer lastIntelligence = null;
        EssayPaperAnswer lastManual = null;
        for (EssayPaperAnswer answer : answers) {
            // 为批改完成状态
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == answer.getBizStatus()) {
                if (CorrectModeEnum.INTELLIGENCE.getMode() == answer.getCorrectMode()) {
                    // 智能批改
                    correctNum++;
                } else {
                    // 人工批改
                    manualNum++;
                }
            }
            // 筛选最后答题卡状态
            if (CorrectModeEnum.INTELLIGENCE.getMode() == answer.getCorrectMode()) {
                // 智能批改
                if (lastIntelligence == null || lastIntelligence.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastIntelligence = answer;
                }
            } else {
                // 人工批改
                if (lastManual == null || lastManual.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastManual = answer;
                }
            }
        }
        paper.setCorrectNum(correctNum);
        paper.setManualNum(manualNum);
        if (lastIntelligence != null) {
            paper.setRecentStatus(lastIntelligence.getBizStatus());
        } else {
            paper.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        if (lastManual != null) {
            paper.setManualRecentStatus(lastManual.getBizStatus());
        } else {
            paper.setManualRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }

        paper.setLastType(getLastAnswerCardType(lastIntelligence, lastManual));

    }

    /**
     * 获取最后一次修改的答题卡类型
     *
     * @param lastIntelligence
     * @param lastManual
     * @return
     */
    private Integer getLastAnswerCardType(EssayPaperAnswer lastIntelligence, EssayPaperAnswer lastManual) {
        if (lastIntelligence != null && lastIntelligence
                .getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus()) {
            lastIntelligence = null;
        }
        if (lastManual != null && lastManual.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED
                .getBizStatus()) {
            lastManual = null;
        }
        if (lastIntelligence != null && lastManual != null) {
            if (lastIntelligence.getGmtModify().compareTo(lastManual.getGmtModify()) > 0) {
                return lastIntelligence.getCorrectMode();
            } else {
                return lastManual.getCorrectMode();
            }
        } else if (lastIntelligence == null && lastManual != null) {
            return lastManual.getCorrectMode();
        } else if (lastIntelligence != null && lastManual == null) {
            return lastIntelligence.getCorrectMode();
        }
        return null;
    }


    @Override
    public Long countPapersByArea(long areaId, int userId) {
        List<Long> areaIds = findAreaIds(areaId);
        if (CollectionUtils.isEmpty(areaIds)) {
            log.info("地区id列表为空");
            throw new BizException(EssayErrors.NO_AREA_LIST);
        }
        Long count = essayPaperBaseRepository.countByStatusAndBizStatusAndAreaIdIn(CHECK_PASS.getStatus(), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), areaIds);
        log.info("area {} has {} papers", areaId, count);
        return count;
    }

    @Cacheable(value = "essayQuestionBelongPaperVOBeanCopies", sync = true)
    public LinkedList<EssayQuestionAreaVO> areaListCopies() {
        /*  guava 机器内存 缓存 数据 */
        log.info(">>>>>>>>从缓存中获取试题对应的area列表<<<<<<<<");
        LinkedList<EssayQuestionAreaVO> areaList = areaListCache.getIfPresent("areaList");
        if (CollectionUtils.isEmpty(areaList)) {
            log.info(">>>>>>>>缓存获取数据失败<<<<<<<<");
            List<EssayQuestionBelongPaperArea> essayQuestionBelongPaperAreaList = essayAreaRepository.findByPIdAndBizStatusAndStatusOrderBySortAsc(0, EssayAreaConstant.EssayAreaBizStatusEnum.ONLINE.getBizStatus(), EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());

            areaList = new LinkedList<>();
            for (EssayQuestionBelongPaperArea area : essayQuestionBelongPaperAreaList) {
                EssayQuestionAreaVO vo = EssayQuestionAreaVO.builder()
                        .id(area.getId())
                        .name(area.getName())
                        .build();
                List<EssayQuestionBelongPaperArea> subs = essayAreaRepository.findByPIdAndBizStatusAndStatusOrderBySortAsc(area.getId(), EssayAreaConstant.EssayAreaBizStatusEnum.ONLINE.getBizStatus(), EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());

                LinkedList<EssayQuestionAreaVO> subVOS = new LinkedList<>();
                for (EssayQuestionBelongPaperArea sub : subs) {
                    EssayQuestionAreaVO subVo = EssayQuestionAreaVO.builder()
                            .id(sub.getId())
                            .name(sub.getName())
                            .build();
                    subVOS.add(subVo);
                }
                vo.setEssayQuestionBelongPaperVOList(subVOS);
                areaList.add(vo);
            }
            areaListCache.put("areaList", areaList);
        }
        return areaList;
    }

    @Override
    public List<EssayQuestionAreaVO> findAreaList() {
        return areaListCopies();
    }


//    @Override
//    public EssayPaperQuestionVO findQuestionDetailByPaperId(long paperId, int userId) {
//        //试卷信息查询
//        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
//        if (essayPaperBase == null) {
//            log.info("试卷信息为空，paperId {}" + paperId);
//            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
//        }
//        //试题信息查询
//
//        //判断用户是否在白名单内（在白名单内，只过滤status）
//        List<EssayQuestionBase> questions = new LinkedList<>();
//
//        //用户在白名单内，从缓存中获取试卷的id
//        String whiteListKey = WhiteRedisKeyConstant.getWhiteList();
//        Set<Integer> userList = redisTemplate.opsForSet().members(whiteListKey);
//
//        //判断用户在白名单中
//        if (CollectionUtils.isNotEmpty(userList) && userList.contains(userId)) {
//            questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
//        } else {
//            questions = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
//        }
//
//        if (CollectionUtils.isEmpty(questions)) {
//            log.info("试卷下试题base信息为空，paperId {}" + paperId);
//            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
//        }
//        final List<Long> questionIds = new ArrayList<>(questions.size() * 4 / 3 + 1);
//        final List<Long> detailIds = new ArrayList<>(questions.size() * 4 / 3 + 1);
//
//        final Map<Long, Long> questionMap = Maps.newHashMap();
//        questions.forEach(question -> {
//            questionIds.add(question.getId());
//            detailIds.add(question.getDetailId());
//            questionMap.put(question.getId(), question.getDetailId());
//        });
//        EssayPaperQuestionVO result = new EssayPaperQuestionVO();
//        EssayPaperVO essayPaperVO = EssayPaperVO.builder()
//                .paperId(paperId).paperName(essayPaperBase.getName()).limitTime(essayPaperBase.getLimitTime())
//                .score(essayPaperBase.getScore()).build();
//        result.setEssayPaper(essayPaperVO);
//        Map<Long, EssayQuestionDetail> questionDetailMap = Maps.newHashMap();
//        // 试题详情缓存
//        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
//        if (CollectionUtils.isEmpty(questionDetails)) {
//            log.info("试卷下试题detail信息为空，paperId {}" + paperId);
//            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
//        }
//        questionDetails.forEach(questionDetail -> questionDetailMap.put(questionDetail.getId(), questionDetail));
//        //试卷答题卡处理
//        List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId,
//                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
//        Map<Long, EssayQuestionAnswer> answerMap = Maps.newHashMap();
//        if (CollectionUtils.isNotEmpty(paperAnswers)) {
//            EssayPaperAnswer essayPaperAnswer = paperAnswers.get(0);
//            //统计用户试卷的最近答题状态和批改次数
//            setEssayPaperInfo(paperAnswers, essayPaperVO);
//            long paperAnswerId = essayPaperAnswer.getId();
//            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == essayPaperAnswer.getBizStatus()) {
//                essayPaperVO.setAnswerCardId(paperAnswerId);
//                essayPaperVO.setLastIndex(essayPaperAnswer.getLastIndex());
//                essayPaperVO.setSpendTime(essayPaperAnswer.getSpendTime());
//                essayPaperVO.setUnfinishedCount(essayPaperAnswer.getUnfinishedCount());
//            }
//
//
//            List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByUserIdAndPaperAnswerIdIn(userId, paperAnswerId);
//            if (CollectionUtils.isNotEmpty(questionAnswers)) {
//                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
//                    //未交卷，返回用户答题信息
//                    if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == questionAnswer.getBizStatus()) {
//                        answerMap.put(questionAnswer.getQuestionBaseId(), questionAnswer);
//
//                    }
//
//                }
//            }
//        }
//        List<EssayQuestionVO> essayQuestionVOS = Lists.newArrayList();
//        for (EssayQuestionBase question : questions) {
//            EssayQuestionDetail questionDetail = questionDetailMap.get(questionMap.get(question.getId()));
//            EssayQuestionAnswer questionAnswer = answerMap.get(question.getId());
//            EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
//                    .limitTime(question.getLimitTime())//答题限时
//                    .questionBaseId(question.getId())//题目的baseId
//                    .sort(question.getSort());
//            if (questionDetail != null) {
//                builder.questionDetailId(questionDetail.getId())//试题的detailId
//                        .type(questionDetail.getType())//试题类型（04-02）
//                        .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
//                        .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
//                        .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
//                        .stem(questionDetail.getStem())//题干信息
//                        .score(questionDetail.getScore())//题目分数
//                        .answerComment(questionDetail.getAnswerComment())
//                        .correctRule(questionDetail.getCorrectRule())
//                        .topic(questionDetail.getTopic())
//                        .subTopic(questionDetail.getSubTopic())
//                        .callName(questionDetail.getCallName())
//                        /**
//                         * 临时替换落款日期和落款人字段（解决客户端展示问题）
//                         */
//                        .inscribedDate(questionDetail.getInscribedName())
//                        .inscribedName(questionDetail.getInscribedDate());//标准答案
//            }
//            if (questionAnswer != null) {
//                builder.inputWordNum(questionAnswer.getInputWordNum())
//                        .answerCardId(questionAnswer.getId())
//                        .content(questionAnswer.getContent())
//                        .questionBaseId(questionAnswer.getQuestionBaseId())
//                        .questionDetailId(questionAnswer.getQuestionDetailId())
//                        .spendTime(questionAnswer.getSpendTime());
//            }
//            essayQuestionVOS.add(builder.build());
//        }
//        result.setEssayQuestions(essayQuestionVOS);
//        return result;
//    }


    @Override
    public EssayPaperDetailVO findPaperAllDetail(long paperId) {
//        List<EssayMaterialVO> list = Lists.newArrayList();
//        for(int i=0;i<5;i++){
//            list.add(EssayMaterialVO.builder().id(new Long(i)).paperId(1L).sort(i).content("这是资料"+i+"的内容").build());
//        }
//        List<EssayQuestionVO> essayQuestions = Lists.newArrayList();
//        for(int j=0;j<5;j++){
//            final EssayMaterialVO material = list.get(j);
//            essayQuestions.add(EssayQuestionVO.builder().sort(j).answerRequire("答题要求"+j).bizStatus(1).inputWordNumMax(100)
//                    .inputWordNumMin(10).materials(new ArrayList(){{add(material);}}).limitTime(60).questionBaseId(1L).questionDetailId(1L)
//                    .stem("题干").build());
//        }
//        return EssayPaperDetailVO.builder()
//        .essayPaper(EssayPaperVO.builder().paperId(1L).paperName("试卷名称").limitTime(7200).build())
//        .essayMaterials(list).essayQuestions(essayQuestions).build();

        //查询试卷材料
        List<EssayMaterialVO> materialList = essayMaterialService.findMaterialsByPaperId(paperId);
        //试题id列表
        List<EssayQuestionBase> questionIdList = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                (paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

        //第一个题目详情
        if (CollectionUtils.isNotEmpty(questionIdList) && null != questionIdList.get(0)) {
            long questionBaseId = questionIdList.get(0).getId();
            //查询试题详情（题目+规则）


        }
        return null;
    }


    /**
     * 计算试卷批改次数和最后一次答题的状态
     *
     * @param paperAnswers
     * @param essayPaperVO
     */
    private void setEssayPaperInfo(List<EssayPaperAnswer> paperAnswers, EssayPaperVO essayPaperVO) {
        int correctNum = 0;
        for (int i = 0; i < paperAnswers.size(); i++) {
            EssayPaperAnswer answer = paperAnswers.get(i);
            if (i == 0) {
                essayPaperVO.setRecentStatus(answer.getBizStatus());
            }
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == answer.getBizStatus()) {
                correctNum++;
            }
        }
        essayPaperVO.setCorrectNum(correctNum);
    }

    public List<Long> findAreaIds(Long areaId) {

        List<Long> areaIds = Lists.newArrayList();
        if (areaId <= 0) {
            return areaIds;
        }
        areaIds.add(areaId);
        List<EssayQuestionBelongPaperArea> paperAreas = essayAreaRepository.findByPIdAndBizStatusAndStatusOrderBySortAsc(areaId, EssayAreaConstant.EssayAreaBizStatusEnum.ONLINE.getBizStatus(), EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(paperAreas)) {
            paperAreas.forEach(area -> areaIds.add(area.getId()));
        }

        return areaIds;
    }


    @Override
    public PageUtil<AdminPaperWithQuestionVO> findByConditions(String name, long areaId, String year, int status, int type, int bizStatus, Pageable pageable, int mockType, int tag, long questionId, long paperId, String admin) {

        //空分页对象
        PageUtil p = PageUtil.builder()
                .result(new LinkedList<>())
                .build();
        if (StringUtils.isNoneBlank(admin)) {
            String userKeyByJY = RedisKeyConstant.getJYUserKey();
            Boolean isJYflag = redisTemplate.opsForSet().isMember(userKeyByJY, admin);
            if (isJYflag) {
                //如果是教育后台用户 只查询上线的并且审核通过的
                bizStatus = EssayQuestionBizStatusEnum.ONLINE.getBizStatus();
                status = AdminPaperConstant.UP_TO_CHECK_PASS;
            }

        }
        //如果是模考&&选择了模考类型，先过滤出来模考id的范围
        List<Long> mockIdList = new LinkedList<>();
        if ((mockType > 0 || tag > 0) && type == AdminPaperConstant.MOCK_PAPER) {
            Specification mockSpecification = queryMockSpecific(mockType, tag);
            List<EssayMockExam> mockList = essayMockExamRepository.findAll(mockSpecification);
            if (CollectionUtils.isNotEmpty(mockList)) {
                for (EssayMockExam mock : mockList) {
                    mockIdList.add(mock.getId());
                }
            }
            if (CollectionUtils.isEmpty(mockIdList)) {
                return p;
            }
        }
        //根据试题id确定试卷id
        long tempPaperId = -1;
        if (-1 != questionId) {
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
            if (questionBase != null && EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus() == questionBase.getStatus()) {
                tempPaperId = questionBase.getPaperId();
            } else {
                return p;
            }
        }

        if (-1 != questionId && (paperId != -1 && tempPaperId != paperId)) {
            return p;
        }
        if (paperId == -1) {
            paperId = tempPaperId;
        }


        Specification specification = querySpecific(name, areaId, year, status, type, bizStatus, mockIdList, paperId);
        final Page<EssayPaperBase> paperList = essayPaperBaseRepository.findAll(specification, pageable);
        List<AdminPaperWithQuestionVO> resultList = assemAdminPaperWithQuestionVO(paperList);

        //如果是模考，查询出来需要填充mockType和tag
        if (type == AdminPaperConstant.MOCK_PAPER && CollectionUtils.isNotEmpty(resultList)) {
            Map<Long, EssayMockExam> mockMap = getAllMockMap();
            for (AdminPaperWithQuestionVO vo : resultList) {
                EssayMockExam essayMockExam = mockMap.get(vo.getId());
				if (essayMockExam != null) {
					vo.setMockType(essayMockExam.getMockType());
					vo.setTag(essayMockExam.getTag());
					vo.setCourseId(essayMockExam.getCourseId());
					vo.setCourseInfo(essayMockExam.getCourseInfo());
					vo.setInstruction(essayMockExam.getInstruction());
				} else {
					log.error("mock is not exist id is:{}", vo.getId());
				}
            }
        }
        long totalElements = paperList.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        p = PageUtil.builder()
                .result(resultList)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }


    /**
     * 查询所有可用的模考数据，放入map中
     *
     * @return
     */
    private Map<Long, EssayMockExam> getAllMockMap() {
        List<EssayMockExam> essayMockExams = essayMockExamRepository.findByStatus(EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
        Map<Long, EssayMockExam> mockMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(essayMockExams)) {
            for (EssayMockExam essayMockExam : essayMockExams) {
                mockMap.put(essayMockExam.getId(), essayMockExam);
            }
        }
        return mockMap;
    }

    /**
     * 通过试卷PO数据关联得到对应的试卷信息，组装一起返回
     *
     * @param paperList
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private List<AdminPaperWithQuestionVO> assemAdminPaperWithQuestionVO(Page<EssayPaperBase> paperList) {
        List<Long> paperIds = Lists.newLinkedList();
        paperList.forEach(i -> paperIds.add(i.getId()));
        Map<Long, List<AdminQuestionVO>> questionMap = Maps.newHashMap();
        List<EssaySimpleQuestionVO> questions = findQuestionListByPapers(paperIds);
        for (EssaySimpleQuestionVO question : questions) {
            AdminQuestionVO questionVO = new AdminQuestionVO();
            BeanUtils.copyProperties(question, questionVO);
            questionVO.setQuestionBaseId(question.getQuestionId());
            long paperId = question.getPaperId();
            if (questionMap.get(paperId) == null) {
                List<AdminQuestionVO> list = Lists.newLinkedList();
                list.add(questionVO);
                questionMap.put(paperId, list);
            } else {
                List<AdminQuestionVO> list = questionMap.get(paperId);
                list.add(questionVO);
            }
        }
        List<AdminPaperWithQuestionVO> resultList = Lists.newLinkedList();
        for (EssayPaperBase paperBase : paperList) {
            AdminPaperWithQuestionVO tmpPaper = new AdminPaperWithQuestionVO();
            BeanUtils.copyProperties(paperBase, tmpPaper);
            List<AdminQuestionVO> questionVOList = questionMap.get(paperBase.getId());
            if (CollectionUtils.isNotEmpty(questionVOList)) {
                questionVOList.sort((a, b) -> (a.getSort() - b.getSort()));
            }
            tmpPaper.setQuestions(questionVOList);
            resultList.add(tmpPaper);
        }
        return resultList;
    }


    private Specification querySpecific(String name, long areaId, String year, int status, int type, int bizStatus, List<Long> mockIdList, long paperId) {
        Specification querySpecific = new Specification<EssayPaperBase>() {
            @Override
            public Predicate toPredicate(Root<EssayPaperBase> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(mockIdList)) {
                    predicates.add((root.get("id").in(mockIdList)));
                }
                if (-1 != paperId) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), paperId));
                }

                if (StringUtils.isNotEmpty(year)) {
                    predicates.add(criteriaBuilder.equal(root.get("paperYear"), year));
                }
                if (-1 != areaId) {
                    predicates.add(criteriaBuilder.equal(root.get("areaId"), areaId));
                }
                if (0 != status) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                if (-1 != bizStatus) {
                    predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatus));
                }
                if (-1 != type) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                if (StringUtils.isNotEmpty(name)) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
                }
                predicates.add(criteriaBuilder.notEqual(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


    private Specification queryMockSpecific(int mockType, int tag) {
        Specification querySpecific = new Specification<EssayMockExam>() {
            @Override
            public Predicate toPredicate(Root<EssayMockExam> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();

                if (tag > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("tag"), tag));
                }

                if (mockType > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("mockType"), mockType));
                }

                predicates.add(criteriaBuilder.equal(root.get("status"), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


//    @Override
//    public long countByConditions(String name, long areaId, String year, int status, int type,List<Long> mockIdList,long paperId) {
//
//        Specification specification = querySpecific(name, areaId, year, status, type, -1,mockIdList,paperId);
//        long count = essayPaperBaseRepository.count(specification);
//        return count;
//    }


    @Override
    public AdminPaperVO addEssayPaper(AdminPaperVO paperVO) {
        EssayPaperBase essayPaperBase = new EssayPaperBase();
        int areaSort = 0;
        if (paperVO.getPaperId() > 0) {
            essayPaperBase = essayPaperBaseRepository.findOne(paperVO.getPaperId());
            if (EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() == essayPaperBase.getBizStatus()) {
                log.error("试卷状态不允许修改，paperId", paperVO.getPaperId());
                throw new BizException(EssayErrors.UPDATE_PAPER_ERROR_FOR_STATUS);
            }
        }
        BeanUtils.copyProperties(paperVO, essayPaperBase);
        long areaId = paperVO.getAreaId();
        EssayQuestionBelongPaperArea area = essayAreaRepository.findOne(areaId);
        if (area == null) {
            log.error("非法参数：areaId={}", areaId);
            ErrorResult result = EssayErrors.ESSAY_PARAM_ILLEGAL;
            throw new BizException(result);
        }
        essayPaperBase.setAreaId(area.getId());
        essayPaperBase.setAreaName(area.getName());
        areaSort = area.getSort();


        if (paperVO.getTypeId() > 0) {
            EssayQuestionBelongPaperArea subArea = essayAreaRepository.findOne(paperVO.getTypeId());
            if (subArea == null) {
                log.info("非法参数：areaId={}", areaId);
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
            }
            essayPaperBase.setSubAreaId(paperVO.getTypeId());
            essayPaperBase.setSubAreaName(subArea.getName());
            areaSort = subArea.getSort();
        }
        essayPaperBase.setType(AdminPaperConstant.TRUE_PAPER);


        essayPaperBase.setStatus(EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus());
        essayPaperBase.setBizStatus(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());
        essayPaperBase.setId(paperVO.getPaperId());
        //判断是否有相同试卷
        List<EssayPaperBase> similarPaper = essayPaperBaseRepository.findByStatusNotAndNameAndIdNot
                (EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus(), paperVO.getName(), essayPaperBase.getId());
        if (CollectionUtils.isNotEmpty(similarPaper)) {

            log.info("存在同名试卷，请修改试卷名称，name：{}", paperVO.getName());
            throw new BizException(EssayErrors.SAME_PAPER_EXIST);

        }
        essayPaperBase = essayPaperBaseRepository.save(essayPaperBase);

        //根据试卷查询试题信息,修改试卷的地区信息
        if (paperVO.getPaperId() > 0) {
            List<EssayQuestionBase> questionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus(paperVO.getPaperId(), new Sort(Sort.Direction.ASC, "sort"), EssayStatusEnum.NORMAL.getCode());
            for (EssayQuestionBase questionBase : questionBaseList) {
                questionBase.setAreaId(essayPaperBase.getAreaId());
                questionBase.setAreaName(essayPaperBase.getAreaName());
                questionBase.setAreaSort(areaSort);
                if (0 != essayPaperBase.getSubAreaId()) {
                    questionBase.setSubAreaId(essayPaperBase.getSubAreaId());
                    questionBase.setSubAreaName(essayPaperBase.getSubAreaName());
                }
                essayQuestionBaseRepository.save(questionBase);
            }
        }

        paperVO.setPaperId(essayPaperBase.getId());
        return paperVO;
    }


    /* BizStatus 0未上线  1上线中  2已下线 */
    /*Status -1 删除状态  1未审核  2审核中  3审核未通过  4 审核通过  */
    @Override
    public int modifyPaperStatusById(Integer type, long paperId) {

        int count = 0;
        //试卷操作类型 : 1提交审核  2上线  3下线   4审核通过 5审核未通过  -1删除
        LinkedList<Integer> oldBizStatusList = new LinkedList<>();
        LinkedList<Integer> oldStatusList = new LinkedList<>();

        EssayMockExam mockExam = essayMockExamRepository.findOne(paperId);

        if (type == AdminPaperConstant.UP_TO_CHECK) {
            simpleCheckStyle(paperId, AdminPaperConstant.UP_TO_CHECK);
            //提交审核
            oldBizStatusList.add(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());

            oldStatusList.add(EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus());
            oldStatusList.add(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_FAILURE.getStatus());
            count = essayPaperBaseRepository.modifyPaperStatus(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECKING.getStatus(), paperId, oldBizStatusList, oldStatusList);
        } else if (type == AdminPaperConstant.UP_TO_CHECK_FAILURE) {
            oldBizStatusList.add(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());
            oldStatusList.add(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECKING.getStatus());

            //审核未通过
            count = essayPaperBaseRepository.modifyPaperStatus
                    (EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_FAILURE.getStatus(), paperId, oldBizStatusList, oldStatusList);
        } else if (type == AdminPaperConstant.UP_TO_CHECK_PASS) {
            simpleCheckStyle(paperId, AdminPaperConstant.UP_TO_CHECK_PASS);
            oldBizStatusList.add(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());
            oldStatusList.add(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECKING.getStatus());

            // 审核通过
            count = essayPaperBaseRepository.modifyPaperStatus
                    (EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), CHECK_PASS.getStatus(), paperId, oldBizStatusList, oldStatusList);
            //只要审核通过，更新对应的pdf(试卷 MQ)
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);

            if (null != paperBase && paperBase.getType() == AdminPaperConstant.TRUE_PAPER) {
                EssayCreatePdfVO pdfVO = EssayCreatePdfVO.builder()
                        .id(paperId)
                        .type(EssayPdfTypeConstant.PAPER)
                        .build();
                log.info("发送MQ消息。生成PDF文件id:{},type{}", pdfVO.getId(), pdfVO.getType());
                rabbitTemplate.convertAndSend(SystemConstant.CREATE_PDF_ROUTING_KEY, pdfVO);
            }

        } else if (type == AdminPaperConstant.UP_TO_ONLINE) {
            simpleCheckStyle(paperId, AdminPaperConstant.UP_TO_ONLINE);
            oldBizStatusList.add(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());
            oldStatusList.add(CHECK_PASS.getStatus());
            //上线(试卷)
            count = essayPaperBaseRepository.modifyPaperStatus(
                    EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), CHECK_PASS.getStatus(), paperId, oldBizStatusList, oldStatusList);

            //上线(试题)
            essayQuestionBaseRepository.modifyStatusByPaperId(paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());
            /**
             * 0806 zhaoxi
             *  如果是申论单独模考，将bizStatus更新成"上线"状态
             *  如果是联合模考，行测模考状态变更时，更新申论的状态
             */
            if (null != mockExam && mockExam.getMockType() == EssayMockTypeConstant.ESSAY_SINGLE_MOCK) {
                mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.ONLINE.getBizStatus());
                essayMockExamRepository.save(mockExam);
            }

            //上线发送mq更新ES数据(真题)
            if (null == mockExam) {
                sendPaper2Search(paperId, AdminPaperConstant.UP_TO_ONLINE);
            }

            //查询试卷下所有试题，清空算法相关缓存，写入新的数据
            List<EssayQuestionBase> questionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

            questionBaseList.forEach(questionBase -> {
                //清除缓存中的标准答案
                String standardAnswerKey = RedisKeyConstant.getStandardAnswerKey(questionBase.getDetailId());
                redisTemplate.delete(standardAnswerKey);
                log.info("清除标准答案缓存成功，key值:" + standardAnswerKey);
            });

        } else if (type == AdminPaperConstant.UP_TO_OFFLINE) {

            /**
             * 0806 zhaoxi
             *  如果是申论单独模考，将bizStatus更新成"初始"状态
             *  如果是联合模考，行测模考状态变更时，更新申论的状态
             */
            if (null != mockExam && mockExam.getMockType() == EssayMockTypeConstant.ESSAY_SINGLE_MOCK) {
                mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus());
                essayMockExamRepository.save(mockExam);
            }

            //判断是否是模考且相关模考已上线(01.09 修改：已经关联模考的申论试卷也可以单独下线)
//            if(null != essayMockExam && EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus()  != essayMockExam.getBizStatus()){
//                throw new BizException(EssayMockErrors.MOCK_CONNECTED);
//            }
            oldBizStatusList.add(EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus());
            oldStatusList.add(CHECK_PASS.getStatus());
            //下线  (下线之后 ,恢复为初始状态)
            count = essayPaperBaseRepository.modifyPaperStatus
                    (EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(), paperId, oldBizStatusList, oldStatusList);

//            //下线(试题)  12.17 改成 不做处理
//            essayQuestionBaseRepository.modifyStatusByPaperId(paperId,EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
            //如果这道题没有被关联置为下线
            List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus(paperId, new Sort(Sort.Direction.ASC, "sort"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(essayQuestionBaseList)) {
                for (EssayQuestionBase questionBase : essayQuestionBaseList) {
                    List<EssaySimilarQuestion> byQuestionBaseIdAndStatus = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionBase.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
                    if (CollectionUtils.isEmpty(byQuestionBaseIdAndStatus)) {
                        questionBase.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
                        essayQuestionBaseRepository.save(questionBase);
                    }
                }
            }
            //发送消息同步试卷信息到ES(真题)
            if (null == mockExam) {
                sendPaper2Search(paperId, AdminPaperConstant.UP_TO_OFFLINE);
            }
        }
        //删除
        if (type == AdminPaperConstant.UP_TO_DELETE) {
            count = essayPaperBaseRepository.modifyPaperToDelete(paperId);
            essayQuestionBaseRepository.modifyQuestionToDeleteByPaperId(paperId);
            //删除 对应的单题组数据
            List<Long> questionBaseIdList = essayQuestionBaseRepository.findQuestionBaseIdByPaperId(paperId);
            if (CollectionUtils.isNotEmpty(questionBaseIdList)) {
                essaySimilarQuestionRepository.upToDeleteByQuestionId(questionBaseIdList);
            }

            /**
             * 0806 zhaoxi  模考不能删除
             *  如果是申论单独模考或者没有绑定行测模考，将Status更新成"删除"状态
             */
//            if(null != mockExam && (mockExam.getMockType() == EssayMockTypeConstant.ESSAY_SINGLE_MOCK || mockExam.getPracticeId() == 0)){
//                mockExam.setStatus(EssayMockExamConstant.EssayMockExamStatusEnum.DELETED.getStatus());
//                essayMockExamRepository.save(mockExam);
//            }

        }

        if (count != 1) {
            throw new BizException(EssayErrors.PAPER_STATUS_ERROR);
        }

        return count;
    }

    /**
     * 检查试卷是否满足审核通过的条件
     *
     * @param paperId
     * @param status
     */
    private void simpleCheckStyle(long paperId, int status) {
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);
        double score = paperBase.getScore();
        double totalScore = 0D;
        //校验试卷材料是否存在
        List<EssayMaterial> materialList = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc
                (paperId, EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(materialList)) {
            log.warn("改试卷尚未关联材料");
            throw new BizException(EssayErrors.PAPER_MATERIAL_NOT_EXIST);
        }


        List<EssayQuestionBase> essayQuestionBases = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId,
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(essayQuestionBases)) {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
        }
        List<Long> detailIds = Lists.newLinkedList();
        for (EssayQuestionBase essayQuestionBase : essayQuestionBases) {
            detailIds.add(essayQuestionBase.getDetailId());
            //根据baseId查询试题下可用材料
            List<EssayQuestionMaterial> questionMaterials = essayQuestionMaterialRepository.findByQuestionBaseIdAndStatus
                    (essayQuestionBase.getId(), EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());

            if (CollectionUtils.isEmpty(questionMaterials)) {
                log.warn("改试卷尚未关联材料");
                throw new BizException(EssayErrors.QUESTION_MATERIAL_NOT_EXIST);
            }
        }
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isNotEmpty(questionDetails)) {
            for (EssayQuestionDetail questionDetail : questionDetails) {
                totalScore += questionDetail.getScore();
            }
        }
        if (score != totalScore) {
            throw new BizException(EssayErrors.SCORE_PAPER_NOT_MATCH_QUESTIONS);
        }
        List<EssayStandardAnswerRule> rules = essayStandardAnswerRuleRepository.findByQuestionDetailIdInAndBizStatusAndStatus(detailIds,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        Set<Long> detailIdsWithNumLimit = Sets.newHashSet();
        Map<Long, EssayStandardAnswerRule> ruleMap = Maps.newHashMap();
        for (EssayStandardAnswerRule rule : rules) {
            if (1 == rule.getType()) {
                detailIdsWithNumLimit.add(rule.getQuestionDetailId());
                ruleMap.put(rule.getQuestionDetailId(), rule);
            }
        }
        if (detailIds.size() > detailIdsWithNumLimit.size()) {
            log.info("试题字数限制规则必须存在");
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_NUM_LIMIT);
        }
        //上线时将字数限制规则中的字数上下限赋予试题
        if (status == AdminPaperConstant.UP_TO_ONLINE) {
            for (EssayQuestionDetail questionDetail : questionDetails) {
                EssayStandardAnswerRule essayStandardAnswerRule = ruleMap.get(questionDetail.getId());
                questionDetail.setInputWordNumMin(essayStandardAnswerRule.getMinNum());
                questionDetail.setInputWordNumMax(essayStandardAnswerRule.getMaxNum());
            }
            essayQuestionDetailRepository.save(questionDetails);
        }
    }

    @Override
    public List<AdminSingleQuestionVO> findQuestionListByPaper(long paperId, boolean redisFlag) {
        LinkedList<AdminSingleQuestionVO> list = new LinkedList<>();
        String questionInfoListKey = PaperRedisKey.getQuestionInfoListKey(paperId);

        //如果走缓存，优先从缓存中获取，命中失败查询mySql
        if (redisFlag) {
            list = (LinkedList<AdminSingleQuestionVO>) redisTemplate.opsForValue().get(questionInfoListKey);
            if (CollectionUtils.isNotEmpty(list)) {
                return list;
            } else {
                list = Lists.newLinkedList();
            }
        }

        List<EssayQuestionBase> baseList = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc
                (paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

        for (EssayQuestionBase base : baseList) {
            AdminSingleQuestionVO questionVO = new AdminSingleQuestionVO();
            BeanUtils.copyProperties(base, questionVO);
            questionVO.setQuestionBaseId(base.getId());
            questionVO.setQuestionDetailId(base.getDetailId());
            //根据detailId查询题干信息
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(base.getDetailId());
            if (null != questionDetail) {
                questionVO.setStem(questionDetail.getStem());
            }
            list.add(questionVO);
        }
        //走缓存，并且数据非空，放入缓存（5分钟失效）
        if (redisFlag && CollectionUtils.isNotEmpty(list)) {
            redisTemplate.opsForValue().set(questionInfoListKey, list, 5, TimeUnit.MINUTES);
        }
        return list;
    }

    @Override
    public List<EssaySimpleQuestionVO> findQuestionListByPapers(List<Long> ids) {
        List<EssayQuestionBase> questionBases = essayQuestionBaseRepository.findByPaperIdInAndStatusNot(ids, EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());
        List<Long> detailIds = Lists.newLinkedList();
        questionBases.forEach(i -> detailIds.add(i.getDetailId()));
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        Map<Long, EssayQuestionDetail> detailMap = Maps.newHashMap();
        questionDetails.forEach(i -> detailMap.put(i.getId(), i));
        List<EssaySimpleQuestionVO> resultList = Lists.newLinkedList();
        for (EssayQuestionBase questionBase : questionBases) {
            resultList.add(EssaySimpleQuestionVO.builder()
                    .questionId(questionBase.getId())
                    .questionDetailId(questionBase.getDetailId())
                    .paperId(questionBase.getPaperId())
                    .sort(questionBase.getSort())
                    .stem(detailMap.get(questionBase.getDetailId()).getStem())
                    .bizStatus(questionBase.getBizStatus())
                    .status(questionBase.getStatus())
                    .videoId(questionBase.getVideoId())
                    .videoUrl(getVideoUrl(questionBase.getVideoId()))
                    .build());

        }
        return resultList;
    }

    private String getVideoUrl(Integer videoId) {
        if (videoId == null || videoId == 0) {
            return "";
        }
        String token = bjyHandler.getToken(videoId);
        return YunUtil.getVideoUrl(videoId, token);

    }

    @Override
    public int deleteQuestion(long questionBaseId, long paperId) {
        //删除操作
        int delete = essayQuestionBaseRepository.deleteQuestionBase(questionBaseId);
        //判断试卷下 是否还有题目（没有的话，试卷下线）
        long count = essayQuestionBaseRepository.countByPaperIdAndBizStatusAndStatus(paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (count == 0) {
            essayPaperBaseRepository.modifyPaperToOffline
                    (EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(), paperId);
        }

        return delete;
    }

    @Override
    public EssayPaperBase findPaperInfoById(long paperId) {
        return essayPaperBaseRepository.findOne(paperId);
    }

    @Override
    public void resetPaperStatus(long paperId) {
        if (paperId > 0) {
            essayPaperBaseRepository.modifyPaperToOffline(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(),
                    EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(),
                    paperId);
        }
    }

    @Override
    public List<EssayQuestionAreaVO> findAreaListNoBiz(String admin) {
        List<EssayQuestionBelongPaperArea> essayQuestionBelongPaperAreaList = essayAreaRepository
                .findByPIdAndStatusOrderBySortAsc(0, EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());
        Boolean isJYflag = false;
        if (StringUtils.isNoneBlank(admin)) {
            String userKeyByJY = RedisKeyConstant.getJYUserKey();
            isJYflag = redisTemplate.opsForSet().isMember(userKeyByJY, admin);
        }
        LinkedList<EssayQuestionAreaVO> areaList = new LinkedList<>();
        for (EssayQuestionBelongPaperArea area : essayQuestionBelongPaperAreaList) {
            if (isJYflag && area.getId() == 9999) {
                continue;
            }
            EssayQuestionAreaVO vo = EssayQuestionAreaVO.builder().id(area.getId()).name(area.getName()).build();
            List<EssayQuestionBelongPaperArea> subs = essayAreaRepository.findByPIdAndStatusOrderBySortAsc(area.getId(),
                    EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());
            LinkedList<EssayQuestionAreaVO> subVOS = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(subs)) {
                for (EssayQuestionBelongPaperArea sub : subs) {
                    EssayQuestionAreaVO subVo = EssayQuestionAreaVO.builder().id(sub.getId()).name(sub.getName())
                            .build();
                    subVOS.add(subVo);
                }
                vo.setEssayQuestionBelongPaperVOList(subVOS);
            } else {
                vo.setEssayQuestionBelongPaperVOList(null);
            }

            areaList.add(vo);
        }

        return areaList;
    }

    @Override
    public EssayUpdateVO addWhitePaper(Long paperId) {

        EssayUpdateVO vo = new EssayUpdateVO();
        vo.setFlag(false);

        //根据试卷id查询试卷base信息
        EssayPaperBase paper = essayPaperBaseRepository.findOne(paperId);

        if (null == paper) {
            log.warn("试卷id错误，不存在对应试卷。paperId：{}", paperId);
            throw new BizException(EssayErrors.PAPER_NOT_EXIST);
        } else if (paper.getStatus() == CHECK_PASS.getStatus()
                && paper.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()) {
            log.warn("该试卷已在app正常展示，无需加入白名单。paperId：{}", paperId);
            throw new BizException(EssayErrors.PAPER_SHOW_NORMAL);
        }

        //将试卷id放入对应地区的白名单中
        long areaId = paper.getAreaId();
        //查询仅白名单用户可见的试卷id
        String whitePaperListKey = WhiteRedisKeyConstant.getWhitePaperList(areaId);
        Long add = redisTemplate.opsForSet().add(whitePaperListKey, paperId);
        if (1 != add) {
            log.warn("该试卷已加至白名单，请勿重复添加。paperId：{}", paperId);
            throw new BizException(EssayErrors.WHITE_PAPER_EXIST);
        } else {
            vo.setFlag(true);
        }
        return vo;
    }

    @Override
    public EssayPaperQuestionVO findQuestionDetailByPaperIdV1(long paperId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //试卷信息查询
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        if (essayPaperBase == null) {
            log.info("试卷信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        //试题信息查询

        //判断用户是否在白名单内（在白名单内，只过滤status）
        List<EssayQuestionBase> questions = new LinkedList<>();

        //用户在白名单内，从缓存中获取试卷的id
        String whiteListKey = WhiteRedisKeyConstant.getWhiteList();
        Set<Integer> userList = redisTemplate.opsForSet().members(whiteListKey);

        //判断用户在白名单中
        if (CollectionUtils.isNotEmpty(userList) && userList.contains(userId)) {
            questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        } else {
            questions = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        }

        if (CollectionUtils.isEmpty(questions)) {
            log.info("试卷下试题base信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
        }
        final List<Long> questionIds = new ArrayList<>(questions.size() * 4 / 3 + 1);
        final List<Long> detailIds = new ArrayList<>(questions.size() * 4 / 3 + 1);

        final Map<Long, Long> questionMap = Maps.newHashMap();
        questions.forEach(question -> {
            questionIds.add(question.getId());
            detailIds.add(question.getDetailId());
            questionMap.put(question.getId(), question.getDetailId());
        });
        EssayPaperQuestionVO result = new EssayPaperQuestionVO();
        EssayPaperVO essayPaperVO = EssayPaperVO.builder()
                .paperId(paperId).paperName(essayPaperBase.getName()).limitTime(essayPaperBase.getLimitTime())
                .score(essayPaperBase.getScore()).build();
        result.setEssayPaper(essayPaperVO);
        Map<Long, EssayQuestionDetail> questionDetailMap = Maps.newHashMap();
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isEmpty(questionDetails)) {
            log.info("试卷下试题detail信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        questionDetails.forEach(questionDetail -> questionDetailMap.put(questionDetail.getId(), questionDetail));
        //试卷答题卡处理
        List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(
                userId,
                paperId,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                modeTypeEnum.getType());
        Map<Long, EssayQuestionAnswer> answerMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(paperAnswers)) {
            EssayPaperAnswer essayPaperAnswer = paperAnswers.get(0);
            //统计用户试卷的最近答题状态和批改次数
            setEssayPaperInfo(paperAnswers, essayPaperVO);
            long paperAnswerId = essayPaperAnswer.getId();
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == essayPaperAnswer.getBizStatus()) {
                essayPaperVO.setAnswerCardId(paperAnswerId);
                essayPaperVO.setLastIndex(essayPaperAnswer.getLastIndex());
                essayPaperVO.setSpendTime(essayPaperAnswer.getSpendTime());
                essayPaperVO.setUnfinishedCount(essayPaperAnswer.getUnfinishedCount());
            }


            List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByUserIdAndPaperAnswerIdIn(userId, paperAnswerId);
            if (CollectionUtils.isNotEmpty(questionAnswers)) {
                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                    //未交卷，返回用户答题信息
                    if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == questionAnswer.getBizStatus()) {
                        answerMap.put(questionAnswer.getQuestionBaseId(), questionAnswer);

                    }

                }
            }
        }
        List<EssayQuestionVO> essayQuestionVOS = Lists.newArrayList();
        for (EssayQuestionBase question : questions) {
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                    (question.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + question.getDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            EssayStandardAnswer essayStandardAnswer = standardAnswerList.get(0);
            EssayQuestionDetail questionDetail = questionDetailMap.get(questionMap.get(question.getId()));
            EssayQuestionAnswer questionAnswer = answerMap.get(question.getId());
            EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
                    .limitTime(question.getLimitTime())//答题限时
                    .questionBaseId(question.getId())//题目的baseId
                    .sort(question.getSort());
            if (questionDetail != null) {
                builder.questionDetailId(questionDetail.getId())//试题的detailId
                        .type(questionDetail.getType())//试题类型（04-02）
                        .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                        .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                        .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                        .stem(questionDetail.getStem())//题干信息
                        .score(questionDetail.getScore())//题目分数
                        .answerComment(essayStandardAnswer.getAnswerComment())
                        .correctRule(questionDetail.getCorrectRule())
                        //答案类型(0 参考答案  1标准答案)(V1单个答案根据阅卷规则判断)
                        .answerFlag(StringUtils.isNotEmpty(questionDetail.getCorrectRule()) ? 1 : 0)
                        .topic(essayStandardAnswer.getTopic())
                        .subTopic(essayStandardAnswer.getSubTopic())
                        .callName(essayStandardAnswer.getCallName())
                        /**
                         * 临时替换落款日期和落款人字段（解决客户端展示问题）
                         */
                        .inscribedDate(essayStandardAnswer.getInscribedName())
                        .inscribedName(essayStandardAnswer.getInscribedDate());//标准答案
            }
            if (questionAnswer != null) {
                builder.inputWordNum(questionAnswer.getInputWordNum())
                        .answerCardId(questionAnswer.getId())
                        .content(questionAnswer.getContent())
                        .questionBaseId(questionAnswer.getQuestionBaseId())
                        .questionDetailId(questionAnswer.getQuestionDetailId())
                        .spendTime(questionAnswer.getSpendTime());
            }
            essayQuestionVOS.add(builder.build());
        }
        result.setEssayQuestions(essayQuestionVOS);
        return result;
    }


    @Override
    public EssayPaperQuestionVO findQuestionDetailByPaperIdV2(long paperId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //试卷信息查询
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        if (essayPaperBase == null) {
            log.info("试卷信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        //如果是模考，判断是否是已结束
        boolean answerFlag = true;
        if (essayPaperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
            EssayMockExam essayMockExam = essayMockExamRepository.findOne(paperId);
            if (essayMockExam.getEndTime().getTime() > System.currentTimeMillis()) {
                answerFlag = false;
            }

        }
        //试题信息查询
        //判断用户是否在白名单内（在白名单内，只过滤status）
        List<EssayQuestionBase> questions = new LinkedList<>();

        //用户在白名单内，从缓存中获取试卷的id
        String whiteListKey = WhiteRedisKeyConstant.getWhiteList();
        Set<Integer> userList = redisTemplate.opsForSet().members(whiteListKey);

        //判断用户在白名单中
        if (CollectionUtils.isNotEmpty(userList) && userList.contains(userId)) {
            questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        } else {
            questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        }

        if (CollectionUtils.isEmpty(questions)) {
            log.info("试卷下试题base信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
        }
        final List<Long> questionIds = new ArrayList<>(questions.size() * 4 / 3 + 1);
        final List<Long> detailIds = new ArrayList<>(questions.size() * 4 / 3 + 1);

        final Map<Long, Long> questionMap = Maps.newHashMap();
        questions.forEach(question -> {
            questionIds.add(question.getId());
            detailIds.add(question.getDetailId());
            questionMap.put(question.getId(), question.getDetailId());
        });
        EssayPaperQuestionVO result = new EssayPaperQuestionVO();
        EssayPaperVO essayPaperVO = EssayPaperVO.builder()
                .paperId(paperId).paperName(essayPaperBase.getName()).limitTime(essayPaperBase.getLimitTime())
                .score(essayPaperBase.getScore()).build();
        result.setEssayPaper(essayPaperVO);
        Map<Long, EssayQuestionDetail> questionDetailMap = Maps.newHashMap();
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isEmpty(questionDetails)) {
            log.info("试卷下试题detail信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        questionDetails.forEach(questionDetail -> questionDetailMap.put(questionDetail.getId(), questionDetail));
        //试卷答题卡处理
        List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId, AdminPaperConstant.TRUE_PAPER,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),modeTypeEnum.getType());
        Map<Long, EssayQuestionAnswer> answerMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(paperAnswers)) {
            EssayPaperAnswer essayPaperAnswer = paperAnswers.get(0);
            //统计用户试卷的最近答题状态和批改次数
            setEssayPaperInfo(paperAnswers, essayPaperVO);
            long paperAnswerId = essayPaperAnswer.getId();
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == essayPaperAnswer.getBizStatus()) {
                essayPaperVO.setAnswerCardId(paperAnswerId);
                essayPaperVO.setLastIndex(essayPaperAnswer.getLastIndex());
                essayPaperVO.setSpendTime(essayPaperAnswer.getSpendTime());
                essayPaperVO.setUnfinishedCount(essayPaperAnswer.getUnfinishedCount());
            }


            List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByUserIdAndPaperAnswerIdIn(userId, paperAnswerId);
            if (CollectionUtils.isNotEmpty(questionAnswers)) {
                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                    //未交卷，返回用户答题信息
                    if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == questionAnswer.getBizStatus()) {
                        answerMap.put(questionAnswer.getQuestionBaseId(), questionAnswer);

                    }

                }
            }
        }
        List<EssayQuestionVO> essayQuestionVOS = Lists.newArrayList();
        for (EssayQuestionBase question : questions) {
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                    (question.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + question.getDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            if (!answerFlag) {
                standardAnswerList.forEach(answer -> {
                    answer.setAnswerComment("<p>模考暂未结束，请在模考结束后查看答案。</p>");
                    answer.setCallName("");
                    answer.setSubTopic("");
                    answer.setTopic("");
                    answer.setInscribedDate("");
                    answer.setInscribedName("");
                });
            }
            EssayQuestionDetail questionDetail = questionDetailMap.get(questionMap.get(question.getId()));
            EssayQuestionAnswer questionAnswer = answerMap.get(question.getId());
            EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
                    .limitTime(question.getLimitTime())//答题限时
                    .questionBaseId(question.getId())//题目的baseId
                    .sort(question.getSort());
            if (questionDetail != null) {
                builder.questionDetailId(questionDetail.getId())//试题的detailId
                        .type(questionDetail.getType())//试题类型（04-02）
                        .inputWordNumMax(questionDetail.getInputWordNumMax())//最多录入字数（录入的字数）
                        .commitWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数（提交的字数）
                        .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                        .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                        .stem(questionDetail.getStem())//题干信息
                        .score(questionDetail.getScore())//题目分数
                        .correctRule(questionDetail.getCorrectRule())
                        .answerList(standardAnswerList);

            }
            if (questionAnswer != null) {
                builder.inputWordNum(questionAnswer.getInputWordNum())
                        .answerCardId(questionAnswer.getId())
                        .content(questionAnswer.getContent())
                        .questionBaseId(questionAnswer.getQuestionBaseId())
                        .questionDetailId(questionAnswer.getQuestionDetailId())
                        .spendTime(questionAnswer.getSpendTime());
            }
            essayQuestionVOS.add(builder.build());
        }
        result.setEssayQuestions(essayQuestionVOS);
        return result;
    }

    @Override
    public EssayPaperQuestionVO findQuestionDetailByPaperIdV4(long paperId, int userId, Integer type, Integer bizStatus, Long cardId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        // 试卷信息查询
        EssayAnswerConstant.EssayAnswerBizStatusEnum essayAnswerBizStatusEnum = EssayAnswerConstant.EssayAnswerBizStatusEnum.create(bizStatus);
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        if (essayPaperBase == null) {
            log.info("试卷信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        // 如果是模考，判断是否是已结束
        boolean answerFlag = true;
        if (essayPaperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
            EssayMockExam essayMockExam = essayMockExamRepository.findOne(paperId);
            if (essayMockExam.getEndTime().getTime() > System.currentTimeMillis()) {
                answerFlag = false;
            }

        }
        // 试题信息查询
        List<EssayQuestionBase> questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId,
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

        if (CollectionUtils.isEmpty(questions)) {
            log.info("试卷下试题base信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
        }
        final List<Long> questionIds = new ArrayList<>(questions.size() * 4 / 3 + 1);
        final List<Long> detailIds = new ArrayList<>(questions.size() * 4 / 3 + 1);

        final Map<Long, Long> questionMap = Maps.newHashMap();
        questions.forEach(question -> {
            questionIds.add(question.getId());
            detailIds.add(question.getDetailId());
            questionMap.put(question.getId(), question.getDetailId());
        });
        EssayPaperQuestionVO result = new EssayPaperQuestionVO();
        EssayPaperVO essayPaperVO = EssayPaperVO.builder().paperId(paperId).paperName(essayPaperBase.getName())
                .answerCardId(cardId).limitTime(essayPaperBase.getLimitTime()).score(essayPaperBase.getScore()).correctMode(type).build();
        result.setEssayPaper(essayPaperVO);
        Map<Long, EssayQuestionDetail> questionDetailMap = Maps.newHashMap();
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isEmpty(questionDetails)) {
            log.info("试卷下试题detail信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        questionDetails.forEach(questionDetail -> questionDetailMap.put(questionDetail.getId(), questionDetail));
        EssayPaperAnswer essayPaperAnswer = null;
        Map<Long, EssayQuestionAnswer> answerMap = Maps.newHashMap();
        switch (modeTypeEnum){
            case NORMAL:
                essayPaperAnswer = getNormalAnswer(userId, paperId, type,essayAnswerBizStatusEnum,cardId);
                break;
            case COURSE:
                essayPaperAnswer = getCourseAnswer(cardId);
        }
        if (null != essayPaperAnswer) {
            Integer otherCorrectType;// 另外一种批改类型
            if (type == CorrectModeEnum.INTELLIGENCE.getMode()) {
                essayPaperVO.setRecentStatus(essayPaperAnswer.getBizStatus());
                otherCorrectType = CorrectModeEnum.MANUAL.getMode();
            } else {
                essayPaperVO.setManualRecentStatus(essayPaperAnswer.getBizStatus());
                otherCorrectType = CorrectModeEnum.INTELLIGENCE.getMode();
            }
            // 查询另外一种答题卡状态
            List<EssayPaperAnswer> otherPaperAnswers = essayPaperAnswerRepository
                    .findByUserIdAndPaperBaseIdAndCorrectModeAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId,
                            otherCorrectType, AdminPaperConstant.TRUE_PAPER,
                            EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),modeTypeEnum.getType());
            if (CollectionUtils.isNotEmpty(otherPaperAnswers)) {
                EssayPaperAnswer otherEssayPaperAnswer = otherPaperAnswers.get(0);
                if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == otherEssayPaperAnswer
                        .getBizStatus()) {
                    essayPaperVO.setOtherAnswerCardId(otherEssayPaperAnswer.getId());
                }
            }
            long paperAnswerId = essayPaperAnswer.getId();
            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == essayPaperAnswer
                    .getBizStatus()) {
                essayPaperVO.setAnswerCardId(paperAnswerId);
                essayPaperVO.setLastIndex(essayPaperAnswer.getLastIndex());
                essayPaperVO.setSpendTime(essayPaperAnswer.getSpendTime());
                essayPaperVO.setUnfinishedCount(essayPaperAnswer.getUnfinishedCount());
            }
            List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository
                    .findByPaperAnswerIdAndUserIdAndStatus(paperAnswerId, userId, EssayStatusEnum.NORMAL.getCode());
            if (CollectionUtils.isNotEmpty(questionAnswers)) {
                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                    // 未交卷，返回用户答题信息
                    if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == questionAnswer
                            .getBizStatus() || essayAnswerBizStatusEnum == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN) {
                        answerMap.put(questionAnswer.getQuestionBaseId(), questionAnswer);
                    }
                }
            }
        }
        List<EssayQuestionVO> essayQuestionVOS = Lists.newArrayList();
        for (EssayQuestionBase question : questions) {
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository
                    .findByQuestionIdAndStatusOrderByIdAsc(question.getDetailId(),
                            EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + question.getDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            if (!answerFlag) {
                standardAnswerList.forEach(answer -> {
                    answer.setAnswerComment("<p>模考暂未结束，请在模考结束后查看答案。</p>");
                    answer.setCallName("");
                    answer.setSubTopic("");
                    answer.setTopic("");
                    answer.setInscribedDate("");
                    answer.setInscribedName("");
                });
            }
            EssayQuestionDetail questionDetail = questionDetailMap.get(questionMap.get(question.getId()));
            EssayQuestionAnswer questionAnswer = answerMap.get(question.getId());
            EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
                    .limitTime(question.getLimitTime())// 答题限时
                    .questionBaseId(question.getId())// 题目的baseId
                    .sort(question.getSort());
            if (questionDetail != null) {
                builder.questionDetailId(questionDetail.getId())// 试题的detailId
                        .type(questionDetail.getType())// 试题类型（04-02）
                        .inputWordNumMax(questionDetail.getInputWordNumMax())// 最多录入字数（录入的字数）
                        .commitWordNumMax(questionDetail.getInputWordNumMax())// 最多答题字数（提交的字数）
                        .inputWordNumMin(questionDetail.getInputWordNumMin())// 最少答题字数
                        .answerRequire(questionDetail.getAnswerRequire())// 答题要求 文字说明
                        .stem(questionDetail.getStem())// 题干信息
                        .score(questionDetail.getScore())// 题目分数
                        .correctRule(questionDetail.getCorrectRule()).answerList(standardAnswerList);

            }
            if (questionAnswer != null) {
                builder.inputWordNum(questionAnswer.getInputWordNum()).answerCardId(questionAnswer.getId())
                        .content(questionAnswer.getContent()).questionBaseId(questionAnswer.getQuestionBaseId())
                        .questionDetailId(questionAnswer.getQuestionDetailId())
                        .spendTime(questionAnswer.getSpendTime());
            }
            EssayQuestionVO essayQuestionVO = builder.build();
            if (StringUtils.isNoneBlank(essayQuestionVO.getAnswerTask())
                    && StringUtils.isNoneBlank(essayQuestionVO.getAnswerRange())
                    && StringUtils.isNoneBlank(essayQuestionVO.getAnswerDetails())) {
                /**
                 * @create huang 2018-4-21
                 * @from 试卷分析内容使用任务，细节，范围三个字段拼接
                 */
                essayQuestionVO.setAnalyzeQuestion(EssayAnalyzeUtil.assertAnalyze(essayQuestionVO.getAnswerTask(),
                        essayQuestionVO.getAnswerRange(), essayQuestionVO.getAnswerDetails()));
            }
            if (type != CorrectModeEnum.INTELLIGENCE.getMode()) {
                if (questionAnswer != null) {
                    // 需要返回imglist相关信息
                    List<CorrectImage> imglist = essayCorrectImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(
                            questionAnswer.getId(), EssayStatusEnum.NORMAL.getCode());
                    if (CollectionUtils.isNotEmpty(imglist)) {
                        List<CorrectImageVO> collect = imglist.stream().map(i -> {
                            CorrectImageVO correctImageVO = new CorrectImageVO();
                            BeanUtils.copyProperties(i, correctImageVO);
                            return correctImageVO;
                        }).collect(Collectors.toList());
                        essayQuestionVO.setUserMeta(collect);
                    }
                }
            }
            essayQuestionVOS.add(essayQuestionVO);
        }
        result.setEssayQuestions(essayQuestionVOS);
        return result;
    }

    private EssayPaperAnswer getCourseAnswer(Long cardId) {
        return essayPaperAnswerRepository.findOne(cardId);
    }

    private EssayPaperAnswer getNormalAnswer( int userId, long paperId, Integer type, EssayAnswerConstant.EssayAnswerBizStatusEnum essayAnswerBizStatusEnum, Long cardId) {
        // 试卷答题卡处理
        List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository
                .findByUserIdAndPaperBaseIdAndCorrectModeAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId, type,
                        AdminPaperConstant.TRUE_PAPER, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                        EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
        EssayPaperAnswer essayPaperAnswer = null;
        if (null != essayAnswerBizStatusEnum && essayAnswerBizStatusEnum == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN) {
            if (cardId.longValue() > 0) {
                essayPaperAnswer = essayPaperAnswerRepository.findOne(cardId);
            }
        } else {
            if (CollectionUtils.isNotEmpty(paperAnswers)) {
                essayPaperAnswer = paperAnswers.get(0);
            }
        }
        return essayPaperAnswer;
    }


    /**
     * 发送MQ消息，更新试卷信息
     *
     * @param paperId
     */
    @Override
    public void sendPaper2Search(Long paperId, int type) {
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        if (null == essayPaperBase) {
            log.error("paper is existed in mysql ,id = {}", paperId);
            return;
        }
        //课后作业的试卷不需要发送到es
		if (essayPaperBase.getAreaId() == 9997) {
			log.error("paper type is exercise ,id = {}", paperId);
			return;
		}
        //材料信息
        List<EssayMaterial> essayMaterials = essayMaterialRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                (paperId, EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus(), EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus());
        List<Map<String, Object>> materialList = new LinkedList<>();
        essayMaterials.forEach(i -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", i.getId());
            map.put("content", i.getContent());
            map.put("sort", i.getSort());


            materialList.add(map);
        });
        //题干信息
        List<EssayQuestionBase> questionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        LinkedList<Long> questionDetailIdList = new LinkedList<>();
        questionBaseList.forEach(i -> questionDetailIdList.add(i.getDetailId()));
        List<EssayQuestionDetail> essayQuestionDetailList = essayQuestionDetailRepository.findByIdIn(questionDetailIdList);
        List<Map<String, Object>> stemList = new LinkedList<>();
        questionBaseList.forEach(i -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("baseId", i.getId());
            map.put("detailId", i.getDetailId());
            map.put("sort", i.getSort());


            essayQuestionDetailList.forEach(j -> {
                if (i.getDetailId() == j.getId()) {
                    map.put("content", j.getStem());
                }
            });
            stemList.add(map);
        });

        Map map = Maps.newHashMap();
        map.put("index", "essay-paper");
        map.put("type", "paper");
        //下线操作
        if (type == AdminPaperConstant.UP_TO_OFFLINE) {
            map.put("operation", "delete");
            //上线操作
        } else if (type == AdminPaperConstant.UP_TO_ONLINE) {
            map.put("operation", "save_clean");
        }
        //拼接试卷查询对象
        EssayPaperSearchVO essayPaperVO = EssayPaperSearchVO.builder()
                .id(paperId)
                .paperId(paperId)
                .paperName(essayPaperBase.getName())
                .materialList(materialList)
                .stemList(stemList)
                .videoAnalyzeFlag(essayPaperBase.getVideoAnalyzeFlag())
                .areaId(essayPaperBase.getAreaId())
                .subAreaId(essayPaperBase.getSubAreaId())
                .areaName(StringUtils.isNoneEmpty(essayPaperBase.getAreaName()) ? essayPaperBase.getAreaName() : "")
                .subAreaName(StringUtils.isNoneEmpty(essayPaperBase.getSubAreaName()) ? essayPaperBase.getSubAreaName() : "")
                .build();

        map.put("data", essayPaperVO);
        //新版搜索引擎数据上传
        rabbitTemplate.convertAndSend("pandora_search", "com.ht.essay.search", JSON.toJSONString(map));
        log.info("发送搜索引擎队列信息：{}", JSON.toJSON(map));
    }

    @Override
    public int resetQuestion(long questionBaseId) {
        EssayQuestionBase base = essayQuestionBaseRepository.findOne(questionBaseId);
        if (null == base) {
            log.info("试题id错误,questionBaseId:{},paperId:{}", questionBaseId);
        }
        long detailId = base.getDetailId();
        //清空题目算法
        delQuestionRuleByDetailId(detailId, true);
        //清空题目答案
        deleteQuestionAnswer(detailId);
        //清除答案
        return 0;
    }

    @Override
    public int delQuestionRuleByDetailId(long detailId, boolean clearAll) {
        essayStandardAnswerKeyPhraseRepository.delByQuestionDetailId(detailId);
        essayStandardAnswerKeyWordRepository.delByQuestionDetailId(detailId);
        if (clearAll) {
            essayStandardAnswerFormatRepository.delByQuestionDetailId(detailId);
            essayStandardAnswerRuleSpecialStripRepository.delByQuestionDetailId(detailId);
            essayStandardAnswerRuleStripSegmentalRepository.delByQuestionDetailId(detailId);
            essayStandardAnswerRuleWordNumRepository.delByQuestionDetailId(detailId);
            essayStandardAnswerRuleRepository.delByQuestionDetailId(detailId);
        }
        return 0;
    }

    @Override
    public Object getGuFenPapers() {

        String essayGuFenPaperListKey = RedisKeyConstant.getEssayGuFenPaperListKey();

        Set<EssayPaperBase> essayGuFenPaperList = redisTemplate.opsForSet().members(essayGuFenPaperListKey);

        List<EssayPaperVO> papers = new ArrayList(essayGuFenPaperList.size() * 4 / 3 + 1);

        for (EssayPaperBase paperBase : essayGuFenPaperList) {
            EssayPaperVO paper = EssayPaperVO.builder()
                    .paperName(paperBase.getName()).limitTime(paperBase.getLimitTime()).score(paperBase.getScore())
                    .videoAnalyzeFlag(paperBase.getVideoAnalyzeFlag())
                    .areaId(paperBase.getAreaId())
                    .build();
            paper.setPaperId(paperBase.getId());
            paper.setCorrectSum(0);
            papers.add(paper);
            Collections.sort(papers, new Comparator<EssayPaperVO>() {
                @Override
                public int compare(EssayPaperVO arg0, EssayPaperVO arg1) {
                    int result = arg0.getPaperName().compareTo(arg1.getPaperName());

                    return result;
                }
            });
        }
        return PageUtil.builder().result(papers).next(0).build();
    }

    @Override
    public List<Object> findByAreaOrName(String name, long areaId) {
        List<EssayPaperBase> papers = essayPaperBaseRepository.findByAreaIdAndBizStatusAndStatus(areaId, EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), CHECK_PASS.getStatus());
        if (CollectionUtils.isEmpty(papers)) {
            return Lists.newArrayList();
        }
        Function<EssayPaperBase, HashMap> trans = (essayPaperBase -> {
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("id", essayPaperBase.getId());
            map.put("name", essayPaperBase.getName());
            map.put("score", essayPaperBase.getScore());
            map.put("time", essayPaperBase.getLimitTime());
            map.put("status", essayPaperBase.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() ? 2 : 1);
            map.put("modifyTime", essayPaperBase.getGmtModify() == null ? essayPaperBase.getGmtCreate().getTime() : essayPaperBase.getGmtModify().getTime());
            return map;
        });
        if (StringUtils.isNotBlank(name)) {
            return papers.stream().filter(i -> i.getName().indexOf(name) > -1).map(trans::apply).collect(Collectors.toList());
        }
        return papers.stream().map(trans::apply).collect(Collectors.toList());
    }

    @Override
    public Object findInfoByIdForEdu(long paperId) {
        EssayPaperBase one = essayPaperBaseRepository.findOne(paperId);
        Function<EssayPaperBase, HashMap> trans = (essayPaperBase -> {
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("id", essayPaperBase.getId());
            map.put("name", essayPaperBase.getName());
            map.put("score", essayPaperBase.getScore());
            map.put("time", essayPaperBase.getLimitTime());
            map.put("status", essayPaperBase.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() ? 2 : 1);
            map.put("modifyTime", essayPaperBase.getGmtModify() == null ? essayPaperBase.getGmtCreate().getTime() : essayPaperBase.getGmtModify().getTime());
            return map;
        });
        if (null != one) {
            return trans.apply(one);
        }
        throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
    }

    @Override
    public List<EssayPaperBase> findAll() {
        return essayPaperBaseRepository.findAll();
    }

    private int deleteQuestionAnswer(long questionDetailId) {
        return essayStandardAnswerRepository.delByQuestionDetailId(questionDetailId);
    }


    @Override
    public EssayPaperQuestionVO findQuestionDetailByPaperIdForDf(long paperId) {

        // 试卷信息查询
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        if (essayPaperBase == null) {
            log.info("试卷信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }

        // 试题信息查询
        List<EssayQuestionBase> questions = new LinkedList<>();

        questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId,
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

        if (CollectionUtils.isEmpty(questions)) {
            log.info("试卷下试题base信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_IN_PAPER);
        }
        final List<Long> questionIds = new ArrayList<>(questions.size());
        final List<Long> detailIds = new ArrayList<>(questions.size());

        final Map<Long, Long> questionMap = Maps.newHashMap();
        questions.forEach(question -> {
            questionIds.add(question.getId());
            detailIds.add(question.getDetailId());
            questionMap.put(question.getId(), question.getDetailId());
        });
        EssayPaperQuestionVO result = new EssayPaperQuestionVO();
        EssayPaperVO essayPaperVO = EssayPaperVO.builder().paperId(paperId).paperName(essayPaperBase.getName())
                .limitTime(essayPaperBase.getLimitTime()).score(essayPaperBase.getScore()).build();
        result.setEssayPaper(essayPaperVO);
        Map<Long, EssayQuestionDetail> questionDetailMap = Maps.newHashMap();
        List<EssayQuestionDetail> questionDetails = essayQuestionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isEmpty(questionDetails)) {
            log.info("试卷下试题detail信息为空，paperId {}" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        questionDetails.forEach(questionDetail -> questionDetailMap.put(questionDetail.getId(), questionDetail));

        List<EssayQuestionVO> essayQuestionVOS = Lists.newArrayList();
        for (EssayQuestionBase question : questions) {
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository
                    .findByQuestionIdAndStatusOrderByIdAsc(question.getDetailId(),
                            EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + question.getDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }

            EssayQuestionDetail questionDetail = questionDetailMap.get(questionMap.get(question.getId()));
            EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
                    .limitTime(question.getLimitTime())// 答题限时
                    .questionBaseId(question.getId())// 题目的baseId
                    .sort(question.getSort());
            if (questionDetail != null) {
                builder.questionDetailId(questionDetail.getId())// 试题的detailId
                        .type(questionDetail.getType())// 试题类型（04-02）
                        .inputWordNumMax(questionDetail.getInputWordNumMax())// 最多录入字数（录入的字数）
                        .commitWordNumMax(questionDetail.getInputWordNumMax())// 最多答题字数（提交的字数）
                        .inputWordNumMin(questionDetail.getInputWordNumMin())// 最少答题字数
                        .answerRequire(questionDetail.getAnswerRequire())// 答题要求 文字说明
                        .stem(questionDetail.getStem())// 题干信息
                        .score(questionDetail.getScore())// 题目分数
                        .correctRule(questionDetail.getCorrectRule()).answerList(standardAnswerList);

            }

            essayQuestionVOS.add(builder.build());
        }
        result.setEssayQuestions(essayQuestionVOS);
        return result;

    }

    @Override
    public List<EssayPaperVO> findPaperListByAreaV2(long areaId, int userId, Pageable pageable, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {
        List<Long> areaIds = findAreaIds(areaId);
        log.info("areaIds = {}", areaIds);
        if (CollectionUtils.isEmpty(areaIds)) {
            throw new BizException(EssayErrors.NO_AREA_LIST);
        }
        //查询某一个地区下的所有试卷信息
        //缓存试卷基本信息，地区对照试卷id
        List<EssayPaperBase> essayPaperBaseList = Lists.newArrayList();
        //1.根据地区id获取试卷列表
        String paperListOfAreaKey = CommonRedisKeyConstant.getPaperListOfAreaKey(areaId);
        essayPaperBaseList = (List<EssayPaperBase>) redisTemplate.opsForValue().get(paperListOfAreaKey);
        //2.缓存数据为空，mysql查询，且放入缓存（失效时间5分钟）
        if (CollectionUtils.isEmpty(essayPaperBaseList)) {
            essayPaperBaseList = essayPaperBaseRepository.findByStatusAndBizStatusAndAreaIdIn(CHECK_PASS.getStatus(), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), areaIds, pageable);
            if (CollectionUtils.isNotEmpty(essayPaperBaseList)) {
                redisTemplate.opsForValue().set(paperListOfAreaKey, essayPaperBaseList);
                redisTemplate.expire(paperListOfAreaKey, 5, TimeUnit.MINUTES);
            }
        }
        //统计试卷id
        List<Long> paperIds = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(essayPaperBaseList)) {
            essayPaperBaseList.forEach(i -> {
                paperIds.add(i.getId());
            });
        } else {
            log.error("paper is not existed in area {}", areaId);
            return Lists.newArrayList();
        }

        //用户在白名单内，从缓存中获取试卷的id
        String whiteListKey = WhiteRedisKeyConstant.getWhiteList();
        Set<Integer> userList = redisTemplate.opsForSet().members(whiteListKey);
        checkUserIdAccess(areaId, userId, essayPaperBaseList, paperIds, userList);


        Map<Long, List<EssayPaperAnswer>> paperAnswerMap = convertPaperIds2PaperAnswerMap(userId, paperIds, modeTypeEnum);
        List<EssayPaperVO> papers = new ArrayList(essayPaperBaseList.size() * 4 / 3 + 1);
        sort(essayPaperBaseList);

        for (EssayPaperBase paperBase : essayPaperBaseList) {
            EssayPaperVO paper = EssayPaperVO.builder()
                    .areaId(paperBase.getAreaId())
                    .paperName(paperBase.getName()).limitTime(paperBase.getLimitTime()).score(paperBase.getScore())
                    .videoAnalyzeFlag(paperBase.getVideoAnalyzeFlag())
                    .build();
            paper.setPaperId(paperBase.getId());
           if (-1 != userId) {
               List<EssayPaperAnswer> answers = paperAnswerMap.get(paperBase.getId());
               if (!CollectionUtils.isEmpty(answers)) {
                   buildManualCorrectExtendInfo(answers, paper);

               }
           }
            // 获取总量
            List<AdminPaperAnswerCountVO> paperCorrectCountInfo = essayPaperAnswerRepository
                    .getPaperCorrectCountInfo(paper.getPaperId(),modeTypeEnum.getType());
            Integer manualSum = 0;
            for (AdminPaperAnswerCountVO adminPaperAnswerCountVO : paperCorrectCountInfo) {
                if (CorrectModeEnum.INTELLIGENCE.getMode() == adminPaperAnswerCountVO.getCorrectMode()) {
                    paper.setCorrectSum(Integer.parseInt(adminPaperAnswerCountVO.getCount() + ""));
                } else {
                    manualSum += Integer.parseInt(adminPaperAnswerCountVO.getCount() + "");
                }
            }
            paper.setManualSum(manualSum);
            papers.add(paper);
        }
        return papers;
    }

    /**
     * 判断用户在白名单中
     *
     * @param areaId
     * @param userId
     * @param essayPaperBaseList
     * @param paperIds
     * @param userList
     */
    private void checkUserIdAccess(long areaId, int userId, List<EssayPaperBase> essayPaperBaseList, List<Long> paperIds, Set<Integer> userList) {
        //判断用户在白名单中
        if (CollectionUtils.isNotEmpty(userList) && userList.contains(userId)) {
            //查询仅白名单用户可见的试卷id
            String whitePaperListKey = WhiteRedisKeyConstant.getWhitePaperList(areaId);
            Set<Integer> whitePaperList = redisTemplate.opsForSet().members(whitePaperListKey);

            //试卷列表不为空
            if (CollectionUtils.isNotEmpty(whitePaperList)) {
                //将试卷id加入id列表
                //将paperBase加入base列表
                for (Integer whitePaperId : whitePaperList) {
                    if (!paperIds.contains(whitePaperId)) {
                        paperIds.add(whitePaperId.longValue());
                        essayPaperBaseList.add(essayPaperBaseRepository.findOne(whitePaperId.longValue()));
                    }
                }
            }
        }
    }

    /**
     * 对试卷进行排序  ("paperYear","paperDate","areaId","subAreaId")
     *
     * @param essayPaperBaseList
     */
    private void sort(List<EssayPaperBase> essayPaperBaseList) {
        Collections.sort(essayPaperBaseList, new Comparator<EssayPaperBase>() {
            @Override
            public int compare(EssayPaperBase arg0, EssayPaperBase arg1) {
                int result = arg1.getPaperYear().compareTo(arg0.getPaperYear());
                if (0 == result) {
                    result = arg1.getPaperDate().compareTo(arg0.getPaperDate());
                    if (0 == result) {
                        result = (int) (arg1.getSubAreaId() - arg0.getSubAreaId());
                    }
                }
                return result;
            }
        });
    }

    @NotNull
    @Override
    public Map<Long, List<EssayPaperAnswer>> convertPaperIds2PaperAnswerMap(int userId, List<Long> paperIds, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        Map<Long, List<EssayPaperAnswer>> paperAnswerMap = Maps.newHashMap();
       if (-1 != userId) {
        List<EssayPaperAnswer> essayPaperAnswers = essayPaperAnswerRepository.findByUserIdAndStatusAndPaperBaseIdInAndAnswerCardType(userId,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(), paperIds, EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
        for (EssayPaperAnswer essayPaperAnswer : essayPaperAnswers) {
            long paperId = essayPaperAnswer.getPaperBaseId();
            if (paperAnswerMap.get(paperId) == null) {
                List<EssayPaperAnswer> subPaperAnswers = Lists.newArrayList();
                subPaperAnswers.add(essayPaperAnswer);
                paperAnswerMap.put(paperId, subPaperAnswers);
            } else {
                List<EssayPaperAnswer> subPaperAnswers = paperAnswerMap.get(paperId);
                subPaperAnswers.add(essayPaperAnswer);
            }
          }
        }
        return paperAnswerMap;
    }


    /**
     * 根据用户做过的该试卷的历史答题卡确定试卷最近的答题卡，和批改状态
     * 包括人工批改对应的答题卡状态
     *
     * @param answers
     * @param responseExtendVO
     */
    @Override
    public void dealPaperResponseExtendInfo(List<EssayPaperAnswer> answers, ResponseExtendVO responseExtendVO) {
        EssayPaperAnswer lastIntelligence = null;
        EssayPaperAnswer lastManual = null;
        for (EssayPaperAnswer answer : answers) {
            // 筛选最后答题卡状态
            if (CorrectModeEnum.INTELLIGENCE.getMode() == answer.getCorrectMode()) {
                // 智能批改
                if (lastIntelligence == null || lastIntelligence.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastIntelligence = answer;
                }
            } else {
                // 人工批改
                if (lastManual == null || lastManual.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastManual = answer;
                }
            }
        }
        if (lastIntelligence != null) {
            responseExtendVO.setRecentStatus(lastIntelligence.getBizStatus());
        } else {
            responseExtendVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        if (lastManual != null) {
            responseExtendVO.setManualRecentStatus(lastManual.getBizStatus());
        } else {
            responseExtendVO.setManualRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        responseExtendVO.setLastType(getLastAnswerCardType(lastIntelligence, lastManual));
    }

    @Override
    public PageUtil<HashMap<String, Object>> findByConditionsForEdu(String name, int status, long startTime, long endTime, int tagId, String paperId, PageRequest pageable) {
        PageUtil p = PageUtil.builder()
                .result(new LinkedList<>())
                .build();
        Specification specification = querySpecific(name, status, startTime, endTime, tagId,paperId);
        Page<EssayMockExam> all = essayMockExamRepository.findAll(specification, pageable);
        if (CollectionUtils.isEmpty(all.getContent())) {
            return p;
        }
        long totalElements = all.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        p = PageUtil.builder()
                .result(all.getContent().stream().map(this::assemblingEduInfo).collect(Collectors.toList()))
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }

    private HashMap<String, Object> assemblingEduInfo(EssayMockExam essayMockExam) {
        HashMap<String, Object> map = Maps.newHashMap();
        Long id = essayMockExam.getId();
        map.put("id", id);
        map.put("bizStatus", essayMockExam.getBizStatus());
        switch (essayMockExam.getBizStatus()) {
            case 0:
            case 1:
                map.put("bizStatusName", "初始化");
                break;
            case 2:
                map.put("bizStatusName", "上线");
                break;
            case 3:
            case 4:
                map.put("bizStatusName", "已结束");
        }
        map.put("name", essayMockExam.getName());
        map.put("gmtModify", essayMockExam.getGmtModify());
        map.put("creatorId", essayMockExam.getCreator());
        map.put("gmtCreate", essayMockExam.getGmtCreate());
        map.put("startTime", essayMockExam.getStartTime());
        map.put("endTime", essayMockExam.getEndTime());
        map.put("tag",essayMockExam.getTag());
        map.put("courseId",essayMockExam.getCourseId());
        String tagName = Arrays.stream(MockTagEnum.values()).filter(i -> i.getFlag())
                .filter(i->i.getCode() == essayMockExam.getTag()).map(MockTagEnum::getName).findFirst().orElse("未知");
        map.put("tagName", tagName);
        map.put("participants", essayMockExam.getExamCount());
        EssayPaperBase paperBase = findPaperInfoById(id);
        map.put("score",paperBase.getScore());
        map.put("applicants", essayMockExam.getEnrollCount());
        return map;
    }


    private Specification querySpecific(String name, int bizStatus, long startTime, long endTime, int tagId, String paperId) {
        Specification querySpecific = new Specification<EssayMockExam>() {
            @Override
            public Predicate toPredicate(Root<EssayMockExam> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();


                predicates.add(criteriaBuilder.equal(root.get("status"), EssayStatusEnum.NORMAL.getCode()));
                if(StringUtils.isNotBlank(paperId)){
                    List<Long> ids = Arrays.stream(paperId.split(",")).filter(NumberUtils::isDigits).map(Long::parseLong).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(ids)){
                        predicates.add(root.get("id").in(ids));
                    }
                }

                if (bizStatus >= 0) {
                    List<Integer> init = Lists.newArrayList(EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus(),EssayMockExamConstant.EssayMockExamBizStatusEnum.CONNECTED.getBizStatus());
                    List<Integer> finished = Lists.newArrayList(EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus(), EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus());
                    if(init.contains(bizStatus)){
                        predicates.add(root.get("bizStatus").in(init));
                    }else if(bizStatus == EssayMockExamConstant.EssayMockExamBizStatusEnum.ONLINE.getBizStatus()){
                        predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatus));
                    }else if(finished.contains(bizStatus)){
                        predicates.add(root.get("bizStatus").in(bizStatus));
                    }

                }
                if (tagId > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("tag"), tagId));
                }
                if (startTime > 0 && endTime > 0) {
                    predicates.add(criteriaBuilder.between(root.get("startTime"), new Date(startTime),new Date(endTime)));
                }
                if (startTime > 0 && endTime < 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), new Date(startTime)));
                }
                if (endTime > 0 && startTime < 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), new Date(endTime)));
                }
                if (StringUtils.isNotEmpty(name)) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }
}
