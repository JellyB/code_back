package com.huatu.tiku.banckend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.banckend.dao.manual.QuestionAdviceMapper;
import com.huatu.tiku.banckend.service.QuestionAdviceService;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.constants.RabbitKeyConstant;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.dto.QuestionAdviceVo;
import com.huatu.tiku.dto.request.BatchDealAdoption;
import com.huatu.tiku.dto.vo.QuestionAdviceBaseIds;
import com.huatu.tiku.entity.AdviceBean;
import com.huatu.tiku.entity.advice.QuestionAdvice;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.push.constant.CorrectFeedbackInfo;
import com.huatu.tiku.push.enums.CorrectDealEnum;
import com.huatu.tiku.response.area.AreaTreeResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeComponent;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.util.page.PageUtil;
import com.huatu.tiku.utils.MyAssert;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.util.QuestionUtil;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhengyi
 * @date 2018/9/13 1:25 PM
 **/
@Service
@Slf4j
public class QuestionAdviceServiceImpl extends BaseServiceImpl<QuestionAdvice> implements QuestionAdviceService {


    private final QuestionAdviceMapper questionAdviceMapper;
    private final AreaService areaService;

    private final QuestionSearchService questionSearchService;

    private final KnowledgeService knowledgeService;

    private final Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    private final RabbitTemplate rabbitTemplate;
    private final NewQuestionDao questionDao;

    private final KnowledgeComponent knowledgeComponent;

    @Autowired
    public QuestionAdviceServiceImpl(QuestionAdviceMapper questionAdviceMapper, AreaService areaService, QuestionSearchService questionSearchService, KnowledgeService knowledgeService, Jackson2JsonMessageConverter jackson2JsonMessageConverter, RabbitTemplate rabbitTemplate, NewQuestionDao questionDao, KnowledgeComponent knowledgeComponent) {
        super(QuestionAdvice.class);
        this.questionAdviceMapper = questionAdviceMapper;
        this.areaService = areaService;
        this.questionSearchService = questionSearchService;
        this.knowledgeService = knowledgeService;
        this.jackson2JsonMessageConverter = jackson2JsonMessageConverter;
        this.rabbitTemplate = rabbitTemplate;
        this.questionDao = questionDao;
        this.knowledgeComponent = knowledgeComponent;
    }

    @Override
    public Object list(AdviceBean advice, int page, int size) {
        //set pageable
        PageHelper.startPage(page, size);
        //get base question id for question and knowledge
        PageInfo<QuestionAdviceBaseIds> questionPageInfo = PageHelper.startPage(page, size).doSelectPageInfo(() -> questionAdviceMapper.getList(advice));
        List<QuestionAdviceBaseIds> list = questionPageInfo.getList();
        List<Long> questionIds = Lists.newArrayList();
        List<Long> knowledgeIds = Lists.newArrayList();
        Map<Long, Long> baseIdMap = Maps.newHashMap();
        Function<Long, Long> transNull = (i -> {
            if (null == i) {
                return -1L;
            }
            return i;
        });
        if (CollectionUtils.isNotEmpty(list)) {
            questionIds.addAll(list.stream().map(QuestionAdviceBaseIds::getQuestionId).map(transNull::apply).collect(Collectors.toList()));
            knowledgeIds.addAll(list.stream().map(QuestionAdviceBaseIds::getKnowledgeId).map(transNull::apply).collect(Collectors.toList()));
            System.out.println("知识点为空的试题：" + list.stream().filter(i -> null == i.getKnowledgeId()).map(i -> i.getQuestionId()).collect(Collectors.toList()));
            baseIdMap.putAll(list.stream().collect(Collectors.toMap(i -> transNull.apply(i.getQuestionId()), i -> transNull.apply(i.getKnowledgeId()))));
        }

        MyAssert.BaseAssert(() -> questionIds.size() > 0, new BizException(ErrorResult.create(1000233, "搜索结果为空")));
        //get question simple info by question ids
        List<QuestionSimpleInfo> questionSimpleInfos = questionSearchService.listAllByQuestionId(questionIds);
        if (CollectionUtils.isNotEmpty(questionSimpleInfos)) {
            Function<List<QuestionSimpleInfo>,List<Long>> getQuestionIds = (questionSimpleInfoList->{
                List<Long> ids = Lists.newArrayList();
                for (QuestionSimpleInfo questionSimpleInfo : questionSimpleInfoList) {
                    ids.add(questionSimpleInfo.getId());
                    if(CollectionUtils.isNotEmpty(questionSimpleInfo.getChildren())){
                        ids.addAll(questionSimpleInfo.getChildren().stream().map(QuestionSimpleInfo::getId).collect(Collectors.toList()));
                    }
                }
                return ids;
            });
            List<Long> ids = getQuestionIds.apply(questionSimpleInfos);
            List<Long> collect = questionIds.stream().filter(i -> !ids.contains(i)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                List<Question> questions = questionDao.findByIds(collect.stream().map(Long::intValue).collect(Collectors.toList()));
                List<QuestionSimpleInfo> tempQuestions = questions.stream().map(getTransQuestionSimpleInfo()::apply).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(tempQuestions)){
                    questionSimpleInfos.addAll(tempQuestions);
                }
                log.info("需要通过mongo获取试题信息：{}", collect);
            }
        }
        questionSimpleInfos.sort(Comparator.comparing((info) -> {
            int index = questionIds.indexOf(info.getId());
            if (-1 != index) {
                return index;
            }
            if (CollectionUtils.isEmpty(info.getChildren())) {
                return -1;
            }
            return info.getChildren().stream()
                    .map(childrenInfo -> questionIds.indexOf(childrenInfo.getId()))
                    .findAny()
                    .orElse(-1);
        }));
        //get knowledge name and id
        Map<Long, String> knowledgeNameByIds = getKnowledgeNameByIds(knowledgeIds);
        //recursive entity
        List<QuestionAdviceVo> questionAdviceVos = getQuestionAdviceVo(questionSimpleInfos, baseIdMap, knowledgeNameByIds);
        //get question advice by questionIds
        Example example = new Example(QuestionAdvice.class);
        example.and().andIn("questionId", questionIds);
        example.and().andEqualTo("bizStatus", advice.getBizStatus());
        List<QuestionAdvice> userErrorDescriptions = selectByExample(example);
        //get area kv
        Map<Long, String> areaCollect = areaService.areaList().stream().collect(Collectors.toMap(AreaTreeResp::getId, AreaTreeResp::getName));
        //assembly data
        recursiveSetAdviceQuestionInfo(questionAdviceVos, userErrorDescriptions, areaCollect);
        return PageUtil.builder().total(questionPageInfo.getTotal()).result(questionAdviceVos).totalPage(questionPageInfo.getPages()).next(questionPageInfo.getNextPage()).build();
    }

    /**
     * 转化mongo试题信息为pandora查询试题返回对象
     *
     * @return
     */
    public Function<Question, QuestionSimpleInfo> getTransQuestionSimpleInfo() {
        return (question -> {
            BizStatusEnum bizStatusEnum = question.getStatus() == QuestionStatus.AUDIT_SUCCESS ? BizStatusEnum.PUBLISH : BizStatusEnum.NO_PUBLISH;
            String stem = "";
            List<String> choices = Lists.newArrayList();
            String answer = "";
            String analyze = "";
            String extend = "";
            List<Long> knowledgeIds = Lists.newArrayList();
            List<String> knowledgeName = Lists.newArrayList();
            if (question instanceof GenericQuestion) {
                stem = ((GenericQuestion) question).getStem();
                choices.addAll(((GenericQuestion) question).getChoices());
                answer = QuestionUtil.getAnswerName(((GenericQuestion) question).getAnswer());
                analyze = ((GenericQuestion) question).getAnalysis();
                extend = ((GenericQuestion) question).getExtend();
            } else if (question instanceof GenericSubjectiveQuestion) {
                stem = ((GenericSubjectiveQuestion) question).getStem();
                answer = ((GenericSubjectiveQuestion) question).getReferAnalysis();
                analyze = ((GenericSubjectiveQuestion) question).getReferAnalysis();
                extend = ((GenericSubjectiveQuestion) question).getExtend();
            }
            List<KnowledgeInfo> pointList = question.getPointList();
            if (CollectionUtils.isNotEmpty(pointList)) {
                knowledgeIds.addAll(pointList.stream().map(i -> i.getPoints()).map(i -> i.get(i.size() - 1)).map(Long::new).collect(Collectors.toList()));
                knowledgeName.addAll(knowledgeIds.stream()
                        .map(knowledgeComponent::getParentUtilRoot)
                        .map((knowledgeList) -> {
                                    Collections.reverse(knowledgeList);
                                    return knowledgeList.stream()
                                            .map(Knowledge::getName)
                                            .collect(Collectors.joining("-"));

                                }
                        )
                        .collect(Collectors.toList()));
            }
            return QuestionSimpleInfo.builder().id(new Long(question.getId()))
                    .questionType(question.getType())
                    .questionTypeName(QuestionInfoEnum.QuestionTypeEnum.create(question.getType()).getName())
                    .bizStatus(bizStatusEnum.getValue())
                    .bizStatusName(bizStatusEnum.getTitle())
                    .availFlag(QuestionInfoEnum.AvailableEnum.AVAILABLE.getCode())
                    .availFlagName(QuestionInfoEnum.AvailableEnum.AVAILABLE.getName())
                    .missFlag(QuestionInfoEnum.CompleteEnum.COMPLETE.getCode())
                    .missFlagName(QuestionInfoEnum.CompleteEnum.COMPLETE.getName())
                    .source(question.getFrom())
                    .stem(stem)
                    .choices(choices)
                    .answer(answer)
                    .mode(question.getMode())
                    .modeName(question.getMode() == QuestionMode.QUESTION_TRUE ? PaperInfoEnum.ModeEnum.TRUE_PAPER.getName() : PaperInfoEnum.ModeEnum.TEST_PAPER.getName())
                    .analyze(analyze)
                    .extend(extend)
                    .materialContent(question.getMaterials())
                    .knowledgeIds(knowledgeIds)
                    .knowledgeName(knowledgeName)
                    .status(question.getStatus() == QuestionStatus.DELETED ? StatusEnum.DELETE.getValue() : StatusEnum.NORMAL.getValue())
                    .build();
        });
    }

    @Override
    public Object batchUpdateUserAdvice(BatchDealAdoption batchDealAdoption) {
        QuestionAdvice questionAdvice = new QuestionAdvice();
        questionAdvice.setResultContent(batchDealAdoption.getResultContent());
        questionAdvice.setBizStatus(2);
        questionAdvice.setGold(batchDealAdoption.getGold());
        questionAdvice.setChecker(batchDealAdoption.getChecker());
        MyAssert.BaseAssert(() -> batchDealAdoption.getIds() != null && batchDealAdoption.getIds().size() > 0, new BizException(ErrorResult.create(1000234, "id不能为空")));
        MyAssert.BaseAssert(() -> batchDealAdoption.getIds() != null && batchDealAdoption.getGold() < 50, new BizException(ErrorResult.create(1000234, "纠错金币不能大于50")));
        Example example = new Example(QuestionAdvice.class);
        example.and().andEqualTo("bizStatus", 3);
        example.and().andIn("id", batchDealAdoption.getIds());
        int num = updateByExampleSelective(questionAdvice, example);
        if (num == 0) {
            return num;
        } else {
            asynSendMessage(batchDealAdoption.getIds());
        }
        return num;
    }

    private void asynSendMessage(List<Long> ids) {
        Example example = new Example(QuestionAdvice.class);
        example.and().andEqualTo("bizStatus", 2);
        example.and().andIn("id", ids);
        List<QuestionAdvice> questionAdvices = selectByExample(example);
        String questionIds = questionAdvices.stream().map(x -> x.getQuestionId().toString()).reduce((id1, id2) -> id1 + "," + id2).orElseThrow(() -> new BizException(ErrorResult.create(1000234, "id不能为空")));
        //get source
        Map<Object, Object> questionSourceMap = questionAdviceMapper.getQuestionTagInfo(questionIds).stream().collect(Collectors.toMap(k -> k.get("questionId"), v -> v.get("source")));
        List<CorrectFeedbackInfo> correctFeedbackInfos = questionAdvices.stream().map(questionAdviceStream -> {
            CorrectFeedbackInfo correctFeedbackInfo = new CorrectFeedbackInfo();
            correctFeedbackInfo.setBizId(questionAdviceStream.getId());
            correctFeedbackInfo.setDealDate(new Date());
            correctFeedbackInfo.setQuestionId(questionAdviceStream.getQuestionId());
            correctFeedbackInfo.setReply(questionAdviceStream.getResultContent());
            correctFeedbackInfo.setUserId(questionAdviceStream.getUserId());
            correctFeedbackInfo.setGold(questionAdviceStream.getGold());
            correctFeedbackInfo.setSource(String.valueOf(questionSourceMap.get((long) questionAdviceStream.getQuestionId())));
            correctFeedbackInfo.setDealDate(questionAdviceStream.getGmtCreate());
            if (questionAdviceStream.getChecker() == 2) {
                correctFeedbackInfo.setStatus(CorrectDealEnum.IGNORE);
            } else if (questionAdviceStream.getChecker() == 1) {
                correctFeedbackInfo.setStatus(CorrectDealEnum.NORMAL);
            }
            return correctFeedbackInfo;
        }).collect(Collectors.toList());
        correctFeedbackInfos.stream().filter(correctFeedbackInfo -> correctFeedbackInfo.getGold() >= 0).forEach(correctFeedbackInfo -> {
            String message = JSONObject.toJSONString(correctFeedbackInfo);
            rabbitTemplate.convertAndSend(RabbitKeyConstant.NOTICE_FEEDBACK_CORRECT, message);
        });
        addGold(questionAdvices);
    }


    /**
     * add gold
     */
    private void addGold(List<QuestionAdvice> questionAdvices) {
        questionAdvices.forEach(questionAdvice -> {
            try {
                if (questionAdvice.getUsername() == null) {
                    return;
                }
                if (questionAdvice.getGold() <= 0) {
                    return;
                }
                log.info("add gold->>gold:{}", questionAdvice.getGold());
                RewardMessage rewardMessage = RewardMessage.builder()
                        .experience(questionAdvice.getGold())
                        .gold(questionAdvice.getGold())
                        .action(RewardAction.ActionType.ANSWER_CORRECTION.name())
                        .uname(questionAdvice.getUsername())
                        .bizId(questionAdvice.getUsername() + questionAdvice.getGold() + System.currentTimeMillis())
                        .timestamp(System.currentTimeMillis())
                        .uid(questionAdvice.getUserId().intValue()).build();
                rabbitTemplate.send(RabbitConsts.QUEUE_REWARD_ACTION, jackson2JsonMessageConverter.toMessage(rewardMessage, new MessageProperties()));
            } catch (Exception e) {
                log.error("添加金币调用异常  username->{},userId->{}", questionAdvice.getUsername(), questionAdvice.getUserId());
                e.printStackTrace();
            }
        });
    }

    private List<QuestionAdviceVo> getQuestionAdviceVo(List<QuestionSimpleInfo> questionSimpleInfos, Map<Long, Long> baseIdMap, Map<Long, String> knowledgeNameByIds) {
        List<QuestionAdviceVo> questionAdviceVos;
        questionAdviceVos = new ArrayList<>();
        for (QuestionSimpleInfo questionSimpleInfo : questionSimpleInfos) {
            QuestionAdviceVo questionAdviceVo = new QuestionAdviceVo();
            BeanUtils.copyProperties(questionSimpleInfo, questionAdviceVo);
            questionAdviceVo.setId(questionSimpleInfo.getId());
            questionAdviceVo.setFrom(questionSimpleInfo.getSource());
            questionAdviceVo.setAnalysis(questionSimpleInfo.getAnalyze());
            questionAdviceVo.setPointsName(knowledgeNameByIds.get(baseIdMap.get(questionAdviceVo.getId())));
            questionAdviceVo.setChildren(null);
            questionAdviceVo.setStatus(questionSimpleInfo.getStatus());
            if (questionSimpleInfo.getChildren() != null && questionSimpleInfo.getChildren().size() > 0) {
                List<QuestionAdviceVo> questionAdviceVoChildren = getQuestionAdviceVo(questionSimpleInfo.getChildren(), baseIdMap, knowledgeNameByIds);
                questionAdviceVo.setChildren(questionAdviceVoChildren);
            }
            questionAdviceVos.add(questionAdviceVo);
        }
        return questionAdviceVos;
    }

    private void recursiveSetAdviceQuestionInfo(List<QuestionAdviceVo> questionAdviceVos, List<QuestionAdvice> userErrorDescriptions, Map<Long, String> areaCollect) {
        questionAdviceVos.stream().filter(Objects::nonNull).forEach(questionAdviceVo -> {
            if (questionAdviceVo == null) {
                return;
            }
            List<QuestionAdvice> questionAdviceContainer = new ArrayList<>();
            userErrorDescriptions.forEach(questionAdvice -> {
                if (questionAdvice.getQuestionId().toString().equals(questionAdviceVo.getId().toString())) {
                    questionAdvice.setAreaName(areaCollect.get(questionAdvice.getQuestionArea().longValue()));
                    questionAdviceContainer.add(questionAdvice);
                }
            });
            questionAdviceVo.setUserErrorDescriptions(questionAdviceContainer);
            if (questionAdviceVo.getChildren() != null && questionAdviceVo.getChildren().size() != 0) {
                recursiveSetAdviceQuestionInfo(questionAdviceVo.getChildren(), userErrorDescriptions, areaCollect);
            }
        });
    }

    private Map<Long, String> getKnowledgeNameByIds(List<Long> ids) {
        Map<Long, String> map = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(ids)) {
            return map;
        }
        List<Knowledge> knowledgeList = knowledgeService.findAll();
        for (Long id : ids) {
            String s = assertKnowledgeName(knowledgeList, id);
            map.put(id, s);
        }
        return map;
    }

    private String assertKnowledgeName(List<Knowledge> knowledgeList, Long id) {
        Knowledge knowledge = knowledgeList.stream().filter(i -> i.getId().equals(id)).findAny().orElse(null);
        if (knowledge == null) {
            return id + "";
        }
        if (knowledge.getParentId() != 0) {
            return assertKnowledgeName(knowledgeList, knowledge.getParentId()) + "-" + knowledge.getName();
        }
        return knowledge.getName();
    }

}











