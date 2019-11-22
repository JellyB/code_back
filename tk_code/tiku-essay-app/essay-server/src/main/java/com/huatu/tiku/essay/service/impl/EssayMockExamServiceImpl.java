package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.TreeBasedTable;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.error.EssayMockErrors;
import com.huatu.tiku.essay.constant.match.MatchRedisKeyConstant;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.vo.report.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.paper.SyncPaperService;
import com.huatu.tiku.essay.vo.admin.EssayMockExamVO;
import com.huatu.tiku.essay.vo.redis.EssayQuestionRedisVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.manager.AreaManager;
import com.huatu.tiku.essay.manager.PaperManager;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.service.task.AsyncMockServiceImpl;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.file.CharacterUtil;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.error.EssayMockErrors.UNCONNECTED_FIRST;

/**
 * Created by x6 on 2017/12/15.
 */
@Service
@Slf4j
public class EssayMockExamServiceImpl implements EssayMockExamService {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    AsyncMockServiceImpl asyncMockServiceImpl;
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    @Autowired
    private EssayAreaRepository essayAreaRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private SyncPaperService syncPaperService;

    @Value("${extra_time}")
    private long extraTime;
    @Value("${mock_redis_expire_time}")
    private int mockRedisExpireTime;

    /**
     * 可以提前进入考场查看试卷的时间
     */
    @Value("${enterLimitTime}")
    private int enterLimitTime;


    @Override
    public EssayMockExamVO queryMockPaper(long id) {

        //根据模考id查询模考信息
        EssayMockExamVO essayMockExamVO = new EssayMockExamVO();

        EssayMockExam essayMockExam = essayMockExamRepository.findOne(id);
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(id);

        BeanUtils.copyProperties(paperBase, essayMockExamVO);
        BeanUtils.copyProperties(essayMockExam, essayMockExamVO);

        return essayMockExamVO;
    }

    @Override
    public ResponseVO getLeftTime(int userId, long paperId) {

        long current = System.currentTimeMillis() / 1000;
        long endTime = 0;
        long startTime = 0;
        String mockDetailPrefix = RedisKeyConstant.getMockDetailPrefix(paperId);
        //获取结束时间
        EssayMockExam essayMockExam = (EssayMockExam) redisTemplate.opsForValue().get(mockDetailPrefix);
        if (null == essayMockExam) {
            essayMockExam = essayMockExamRepository.findOne(paperId);
        }

        String paperBaseKey = RedisKeyConstant.getPaperBaseKey(paperId);
        EssayPaperBase essayPaperBase = (EssayPaperBase) redisTemplate.opsForValue().get(paperBaseKey);
        if (null == essayPaperBase) {
            essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        }
        endTime = essayMockExam.getEndTime().getTime() / 1000;
        startTime = essayMockExam.getStartTime().getTime() / 1000;

        long left = endTime - current;
        if (current <= startTime) {
            left = essayPaperBase.getLimitTime();
        }
        ResponseVO responseVO = ResponseVO.builder()
                .leftTime(Integer.parseInt(left + ""))
                .build();
        return responseVO;
    }

    @Override
    public Boolean calculateButton(long mockId) {
        //当模考完成时，才可查看报告
        EssayMockExam mockExam = essayMockExamRepository.findOne(mockId);

        if (mockExam == null) {
            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        } else {
            return (EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus() == mockExam.getBizStatus());
        }

    }


    @Override
    public int paperCommit(UserSession userSession, PaperCommitVO paperCommitVO, int terminal) {
        //判断当前时间和考试开始时间
        long current = System.currentTimeMillis() / 1000;
        EssayMockExam essayMockExam = PaperManager.getMockDetail(paperCommitVO.getPaperBaseId(), redisTemplate, essayMockExamRepository, mockRedisExpireTime);

        if (null == essayMockExam || EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus() == essayMockExam.getBizStatus()
                || EssayMockExamConstant.EssayMockExamBizStatusEnum.CONNECTED.getBizStatus() == essayMockExam.getBizStatus()) {
            log.warn("模考暂未上线,mockId:{}", paperCommitVO.getPaperBaseId());
            throw new BizException(EssayMockErrors.MOCK_OFFLINE);
        }

        if (current <= essayMockExam.getStartTime().getTime() / 1000) {
            log.warn("申论模考未到作答时间,id:" + essayMockExam.getId());
            throw new BizException(EssayMockErrors.NOT_MOCK_CORRECT_TIME);
        }

        Long answerCardId = paperCommitVO.getAnswerCardId();
        Integer saveType = paperCommitVO.getSaveType();

        //从缓存中获取 答题卡相关信息
        String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperCommitVO.getPaperBaseId(), userSession.getId());
        EssayMockExamAnswerVO essayMockExamAnswerVO = PaperManager.getAnswerCard(paperCommitVO.getPaperBaseId(), userSession.getId(), redisTemplate, essayPaperAnswerRepository, essayQuestionAnswerRepository, mockRedisExpireTime);

        //essayMockExamAnswerVO判空
        if (null == essayMockExamAnswerVO || null == essayMockExamAnswerVO.getEssayPaperAnswer()) {
            log.warn("用户答题卡数据有误,mockId:{}，userId：{}", paperCommitVO.getPaperBaseId(), userSession.getId());
            throw new BizException(EssayMockErrors.MOCK_ANSWERCARD_NOT_EXIST);
        }
        //如果是开考前五分钟进入试卷，直接开始答题（交卷时没有答题卡id，根据userId和paperId查询用户的答题卡Id拼接到commitVO中）
        if (null == answerCardId || 0 == answerCardId) {
            //填充试卷答题卡id
            answerCardId = essayMockExamAnswerVO.getEssayPaperAnswer().getId();
            paperCommitVO.setAnswerCardId(answerCardId);
            //填充试题答题卡id
            List<PaperCommitAnswerVO> answerVOList = paperCommitVO.getAnswerList();
            List<EssayQuestionAnswer> answerList = essayMockExamAnswerVO.getEssayQuestionAnswerList();
            if (CollectionUtils.isNotEmpty(answerVOList)) {
                for (int i = 0; i < answerVOList.size(); i++) {
                    PaperCommitAnswerVO questionAnswerVO = answerVOList.get(i);
                    for (EssayQuestionAnswer questionAnswer : answerList) {
                        if (questionAnswer.getQuestionBaseId() == questionAnswerVO.getQuestionBaseId()) {
                            questionAnswerVO.setAnswerId(questionAnswer.getId());
                        }
                    }
//                    answerVOList.set(i, questionAnswerVO);
                }
            }
//            paperCommitVO.setAnswerList(answerVOList);
        }

        //1.将答题数据存入redis
        saveMockPaperAnswerToRedis(paperCommitVO, examAnswerKey, terminal);

        //2MQ:批改试卷 (答题卡id ,考试类型（模考、普通答题）  redis-key)
        if (AnswerSaveTypeConstant.COMMIT == saveType) {
            EssayMockVO essayMockVO = EssayMockVO.builder()
                    .answerCardId(answerCardId)
                    .mockRedisKey(examAnswerKey)
                    .build();

            log.info("=====进入批改试卷接口【模考】，发送消息到消息队列:" + essayMockVO + "=====");

            //0319 模考交卷，赠送金币
            //套题增加金币
            eventPublisher.publishEvent(RewardActionEvent.class,
                    this,
                    (event) -> event.setAction(RewardAction.ActionType.MATCH_ENTER)
                            .setUid(userSession.getId())
                            .setUname(userSession.getUname())
            );

            String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperCommitVO.getPaperBaseId());
            redisTemplate.opsForHash().put(userAnswerStatusKey, userSession.getId() + "", EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());

            rabbitTemplate.convertAndSend(SystemConstant.MOCK_ANSWER_CORRECT_ROUTING_KEY, essayMockVO);

            //考试结束15分钟之内交卷  才放入共用缓存（避免15分钟之后交卷，影响到查看报告按钮）
            if (System.currentTimeMillis() <= essayMockExam.getStartTime().getTime() + TimeUnit.MINUTES.toMillis(extraTime)) {
                //将userId放入三方公用的set中
                String publicUserSetPrefix = RedisKeyConstant.getPublicUserSetPrefix(paperCommitVO.getPaperBaseId());
                redisTemplate.opsForSet().add(publicUserSetPrefix, userSession.getId());
            }
        }
        //2-MQ:将答题数据持久化存入MySQL(1.14 改成异步方法)
        asyncMockServiceImpl.saveMockAnswerToMySql(examAnswerKey);
        return 1;
    }

    //保存用户答题信息到redis
    private void saveMockPaperAnswerToRedis(PaperCommitVO paperCommitVO, String examAnswerKey, int terminal) {
        //1.从缓存中获取用户答题信息
        EssayMockExamAnswerVO essayMockExamAnswerVO = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);

        EssayPaperAnswer essayPaperAnswer = essayMockExamAnswerVO.getEssayPaperAnswer();
        List<EssayQuestionAnswer> essayQuestionAnswerList = essayMockExamAnswerVO.getEssayQuestionAnswerList();

		essayPaperAnswer.setSpendTime(paperCommitVO.getSpendTime() == null ? 0 : paperCommitVO.getSpendTime());
        essayPaperAnswer.setLastIndex(paperCommitVO.getLastIndex() == null ? 0 : paperCommitVO.getLastIndex());
        essayPaperAnswer.setUnfinishedCount(paperCommitVO.getUnfinishedCount());
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();
        Date date = new Date();
        if (CollectionUtils.isNotEmpty(answerList)) {
            for (int i = 0; i < answerList.size(); i++) {
                PaperCommitAnswerVO commitAnswerVO = answerList.get(i);
                if(null == commitAnswerVO || StringUtils.isBlank(commitAnswerVO.getContent())){
                    continue;
                }
//                EssayQuestionAnswer questionAnswer = essayQuestionAnswerList.get(i);
                for (EssayQuestionAnswer questionAnswer : essayQuestionAnswerList) {
                    if (null != questionAnswer && questionAnswer.getQuestionBaseId() == commitAnswerVO.getQuestionBaseId()) {
                        questionAnswer.setContent(commitAnswerVO.getContent());//学员作答答案
                        questionAnswer.setTerminal(terminal);//答题终端
                        questionAnswer.setSpendTime(commitAnswerVO.getSpendTime());//答题用时
                        questionAnswer.setInputWordNum(commitAnswerVO.getInputWordNum());//录入字数
                        questionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
                        questionAnswer.setPaperId(essayPaperAnswer.getPaperBaseId());

                        if (AnswerSaveTypeConstant.SAVE == paperCommitVO.getSaveType()) {
                            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                        } else {
                            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                            questionAnswer.setSubmitTime(date);
                            essayPaperAnswer.setSubmitTime(date);
                        }

                       // essayQuestionAnswerList.set(i, questionAnswer);

                        //continue;
                    }

                }

            }
           // essayMockExamAnswerVO.setEssayQuestionAnswerList(essayQuestionAnswerList);
        }
        //essayMockExamAnswerVO.setEssayPaperAnswer(essayPaperAnswer);
        redisTemplate.opsForValue().set(examAnswerKey, essayMockExamAnswerVO);
    }

    @Override
    public List<EssayMaterial> materialList(int userId, long paperId, int terminal) {

        //获取模考信息  优先取缓存
        EssayMockExam essayMockExam = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);

        //获取模考对应试卷信息 优先取缓存
        EssayPaperBase paperBase = PaperManager.getPaperBase(essayPaperBaseRepository, essayMockExamRepository, redisTemplate, paperId, mockRedisExpireTime);

        //判断模考状态 && 模考试卷状态
        if (null == essayMockExam || EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus() == essayMockExam.getBizStatus()
                || EssayMockExamConstant.EssayMockExamBizStatusEnum.CONNECTED.getBizStatus() == essayMockExam.getBizStatus()) {
            log.error("模考暂未上线,mockId:{}", paperId);
            throw new BizException(EssayMockErrors.MOCK_OFFLINE);
        }
        //  判断模考试卷状态 && 模考试卷状态
        if (null == paperBase || EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()) {
            log.error("模考试卷未上线,paperId:{}", paperId);
            throw new BizException(EssayMockErrors.MOCK_PAPER_OFFLINE);
        }

        //开考前五分钟才可以看试题-->修改为指定时间就可以看试题
        if (System.currentTimeMillis() <= essayMockExam.getStartTime().getTime() - TimeUnit.MINUTES.toMillis(enterLimitTime)) {
            log.error("开考前五分钟方可查看试题信息 paperId:{}", paperId);
            throw new BizException(EssayMockErrors.NOT_READY_YET);
        }

        //查询试卷材料(优先从缓存中获取)
        List<EssayMaterial> materialList = getPaperQuestionMaterialList(paperId);

        if (CollectionUtils.isEmpty(materialList)) {
            log.error("模考试卷材料列表为空。paperId:{}", paperId);
            throw new BizException(EssayMockErrors.MOCK_MATERIAL_NOT_EXIST);
        }

        //创建答题卡
        //1.检查用户是否有答题卡（没有的话，发MQ消息创建答题卡）
        //通过用户的答题卡状态判断
        String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperId);
        Object obj = redisTemplate.opsForHash().get(userAnswerStatusKey, userId + "");
        if (null != obj) {
            // 已经创建过答题卡，直接返回材料信息退出
        } else {
            //1.判断是否已经开考超过30分钟
            if (System.currentTimeMillis() - essayMockExam.getStartTime().getTime() >= TimeUnit.MINUTES.toMillis(30)) {
                log.error("您已经错过本次模考，请下次赶早。 paperId:{}，userid:{}", paperId, userId);
                throw new BizException(EssayMockErrors.MISSING_MATCH);
            }
            //2.没有创建过答题卡，把用户答题卡状态放入缓存
            redisTemplate.opsForHash().put(userAnswerStatusKey, userId + "", EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());

            //3.缓存中没有该用户的答题卡信息
            CreateAnswerCardVO answerCardVO = CreateAnswerCardVO.builder()
                    .paperBaseId(paperId)
                    .userId(userId)
                    .terminal(terminal)
                    .build();
            //4.发送消息队列，为用户创建答题卡
            rabbitTemplate.convertAndSend(SystemConstant.CREATE_ESSAY_MOCK_ANSWER_CARD_QUEUE, answerCardVO);
        }
        return materialList;
    }


    //获取材料列表（优先从缓存中取）
    private List<EssayMaterial> getPaperQuestionMaterialList(long paperId) {
        String key = RedisKeyConstant.getPaperMaterialKey(paperId);

        List<EssayMaterial> essayMaterialList = (List<EssayMaterial>) redisTemplate.opsForValue().get(key);

        if (CollectionUtils.isEmpty(essayMaterialList)) {
            essayMaterialList = essayMaterialRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                    (paperId, EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus(), EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(essayMaterialList)) {
                redisTemplate.opsForValue().set(key, essayMaterialList);
                //过期时间  提供方法手动清除
                redisTemplate.expire(key, mockRedisExpireTime, TimeUnit.MINUTES);
            }

        }
        return essayMaterialList;
    }

    //获取模考卷的题目详情
    public List<EssayQuestionRedisVO> getQuestionList(long paperId) {

        String paperQuestionKey = RedisKeyConstant.getPaperQuestionKey(paperId);
        List<EssayQuestionRedisVO> essayQuestionList = Lists.newArrayList();
        try {
            essayQuestionList = (List<EssayQuestionRedisVO>) redisTemplate.opsForValue().get(paperQuestionKey);
        } catch (Exception e) {
            log.debug("EssayQuestionRedisVO error,clear....,error={}", e.getMessage());
            redisTemplate.delete(paperQuestionKey);
        }
        if (CollectionUtils.isEmpty(essayQuestionList)) {
            essayQuestionList = new LinkedList<>();
            log.debug("从缓存中获取试题数据失败");
            // 查询模考试卷下的题目，创建试题的答题卡需要questionBaseId和questionDetailId(优先取缓存)
            List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                    (paperId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

            if (CollectionUtils.isNotEmpty(essayQuestionBaseList)) {
                for (EssayQuestionBase questionBase : essayQuestionBaseList) {
                    long baseId = questionBase.getId();
                    long detailId = questionBase.getDetailId();

                    EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(detailId);
                    EssayQuestionRedisVO questionRedisVO = EssayQuestionRedisVO.builder()
                            .baseId(baseId)
                            .detailId(detailId)
                            .sort(questionBase.getSort())
                            .answerRequire(questionDetail.getAnswerRequire())
                            .inputWordNumMax(questionDetail.getInputWordNumMax())
                            .inputWordNumMin(questionDetail.getInputWordNumMin())
                            .limitTime(questionBase.getLimitTime())
                            .questionYear(questionBase.getQuestionYear())
                            .type(questionDetail.getType())
                            .score(questionDetail.getScore())
                            .paperId(paperId)
                            .correctType(questionDetail.getCorrectType())
                            .build();
                    essayQuestionList.add(questionRedisVO);
                }
                redisTemplate.opsForValue().set(paperQuestionKey, essayQuestionList);
                redisTemplate.expire(paperQuestionKey, 30, TimeUnit.MINUTES);
            }

        }
        return essayQuestionList;

    }


    /**
     * 创建答题卡接口
     */
    @Override
    public EssayMockExamAnswerVO createMockAnswerCard(CreateAnswerCardVO createAnswerCardVO) {
        //用户id
        Integer userId = createAnswerCardVO.getUserId();
        //试卷id
        Long paperBaseId = createAnswerCardVO.getPaperBaseId();

        // 查询模考试卷的基本信息，创建试卷的答题卡需要(优先取缓存
        EssayPaperBase paperBase = PaperManager.getPaperBase(essayPaperBaseRepository, essayMockExamRepository, redisTemplate, paperBaseId, mockRedisExpireTime);

        if (null == paperBase || EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()) {
            log.error("模考试卷未上线,paperId:{}", paperBaseId);
            throw new BizException(EssayMockErrors.MOCK_PAPER_OFFLINE);
        }

        //查询用户模考的地区信息
        long areaId = getMockUserArea(paperBaseId, userId);

        log.info("开始为用户创建答题卡，userId：{}，paperBaseId：{},areaId:{}", userId, paperBaseId, areaId);
        EssayQuestionBelongPaperArea area = AreaManager.getEssayQuestionBelongPaperArea(essayAreaRepository, areaId);
        // 查询模考试卷下的题目，创建试题的答题卡需要questionBaseId和questionDetailId(优先取缓存)
        List<EssayQuestionRedisVO> questionList = getQuestionList(paperBaseId);

        //创建试卷答题卡
        EssayPaperAnswer essayPaperAnswerCard = EssayPaperAnswer.builder()
                .paperBaseId(paperBaseId)
                .areaId(areaId)
                .areaName((null == area) ? "" : area.getName())
                .score(paperBase.getScore())
                .userId(userId)
                .unfinishedCount(questionList.size())
                .type(AdminPaperConstant.MOCK_PAPER)//模考题
                //0226 名字是行测考试的名字-申论
                .name(paperBase.getName())
                .build();

        essayPaperAnswerCard.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
        essayPaperAnswerCard.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
        essayPaperAnswerCard.setCreator(userId + "");
        essayPaperAnswerCard = essayPaperAnswerRepository.save(essayPaperAnswerCard);

        //创建试题答题卡
        List<EssayQuestionAnswer> essayQuestionAnswerList = new LinkedList<>();
        for (EssayQuestionRedisVO questionVO : questionList) {
            log.debug("创建试题答题卡，paperBaseId：{}", essayPaperAnswerCard.getPaperBaseId());
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                    .userId(userId)
                    .terminal(createAnswerCardVO.getTerminal())
                    .questionType(questionVO.getType())
                    .areaId(areaId)
                    .areaName((null == area) ? "" : area.getName())
                    .questionBaseId(questionVO.getBaseId())
                    .questionYear(questionVO.getQuestionYear())
                    .questionDetailId(questionVO.getDetailId())
                    .score(questionVO.getScore())
                    .paperAnswerId(essayPaperAnswerCard.getId())//对应的是试卷答题卡的id
                    .paperId(essayPaperAnswerCard.getPaperBaseId())
                    .correctType(questionVO.getCorrectType())
                    .build();
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayQuestionAnswer.setCreator(userId + "");
            essayQuestionAnswerList.add(essayQuestionAnswer);
        }
        // 批量插入
        essayQuestionAnswerRepository.save(essayQuestionAnswerList);
        //把用户的答题卡缓存起来
        EssayMockExamAnswerVO examAnswerVO = EssayMockExamAnswerVO.builder()
                .essayPaperAnswer(essayPaperAnswerCard)
                .essayQuestionAnswerList(essayQuestionAnswerList)
                .build();

        //把用户答题卡信息存入redis（不设置失效时间，考试结束统一清除）
        String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperBaseId, userId);
        redisTemplate.opsForValue().set(examAnswerKey, examAnswerVO);
        return examAnswerVO;
    }


    /**
     * 查询试卷下试题信息
     *
     * @param userId
     * @param paperId
     * @return
     */
    @Override
    public EssayPaperQuestionVO questionList(int userId, long paperId, int terminal) {

        //先判断用户的答题卡信息是否已经创建(redis)
        EssayMockExamAnswerVO essayMockExamAnswerVO = PaperManager.getAnswerCard(paperId, userId, redisTemplate, essayPaperAnswerRepository, essayQuestionAnswerRepository, mockRedisExpireTime);

        //从缓存数据中取出试题答题卡
        List<EssayQuestionAnswer> essayQuestionAnswerVOList = null;
        EssayPaperAnswer paperAnswer = null;
        if (essayMockExamAnswerVO != null) {
            essayQuestionAnswerVOList = essayMockExamAnswerVO.getEssayQuestionAnswerList();
            paperAnswer = essayMockExamAnswerVO.getEssayPaperAnswer();
        }

        //取出试题列表
        List<EssayQuestionRedisVO> essayQuestionList = getQuestionList(paperId);

        //从缓存中获取试卷信息
        EssayPaperBase paperBase = PaperManager.getPaperBase(essayPaperBaseRepository, essayMockExamRepository, redisTemplate, paperId, mockRedisExpireTime);
        if (null == paperBase) {
            log.warn("模考试卷缺失,paperId:{}", paperId);
            throw new BizException(EssayMockErrors.MOCK_PAPER_LOST);
        }
        //试卷信息组装
        EssayPaperVO.EssayPaperVOBuilder paperBuilder = EssayPaperVO.builder()
                .limitTime(paperBase.getLimitTime())//答题限制时间
                .paperId(paperId)//试卷id
                .paperName(paperBase.getName())//试卷名称
                .score(paperBase.getScore());//试卷分数
        EssayMockExam mockDetail = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);
        if(mockDetail != null){
            paperBuilder.startTime(mockDetail.getStartTime().getTime())
                    .endTime(mockDetail.getEndTime().getTime());
        }

        if (paperAnswer != null) {
            paperBuilder.answerCardId(paperAnswer.getId())//试卷答题卡id
                    .lastIndex(paperAnswer.getLastIndex())//最后答题序号
                    .spendTime(paperAnswer.getSpendTime())//已作答时间
                    .unfinishedCount(paperAnswer.getUnfinishedCount());//未完成题目个数
        }
        EssayPaperVO essayPaperVO = paperBuilder.build();

        //试题答题卡信息和试题信息 组装
        List<EssayQuestionVO> essayQuestionVOList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(essayQuestionList)) {
            for (int i = 0; i < essayQuestionList.size(); i++) {
                EssayQuestionRedisVO question = essayQuestionList.get(i);
                EssayQuestionVO.EssayQuestionVOBuilder builder = EssayQuestionVO.builder()
                        .paperId(paperId)
                        .limitTime(question.getLimitTime())//答题限时
                        .questionBaseId(question.getBaseId())//题目的baseId
                        .sort(question.getSort())
                        .questionDetailId(question.getDetailId())//试题的detailId
                        .inputWordNumMax(question.getInputWordNumMax())//最多答题字数
                        .inputWordNumMin(question.getInputWordNumMin())//最少答题字数
                        .commitWordNumMax(question.getInputWordNumMax())
                        .answerRequire(question.getAnswerRequire());//答题要求 文字说明

                if (CollectionUtils.isNotEmpty(essayQuestionAnswerVOList)) {
                    for (EssayQuestionAnswer questionAnswerVO : essayQuestionAnswerVOList) {
                        //根据试题id相同，组装试题和答案信息
                        if (questionAnswerVO.getQuestionBaseId() == question.getBaseId()) {
                            builder.inputWordNum(questionAnswerVO.getInputWordNum())
                                    .answerCardId(questionAnswerVO.getId())
                                    .content(questionAnswerVO.getContent())
                                    .questionBaseId(questionAnswerVO.getQuestionBaseId())
                                    .questionDetailId(questionAnswerVO.getQuestionDetailId())
                                    .spendTime(questionAnswerVO.getSpendTime());
                            continue;
                        }
                    }
                }

                essayQuestionVOList.add(builder.build());
            }
        }
        return EssayPaperQuestionVO.builder()
                .essayQuestions(essayQuestionVOList)
                .essayPaper(essayPaperVO)
                .build();

    }

    /**
     * 保存答题卡（持久化MySql）
     */
    @Override
    public void saveMockPaperAnswer(String examAnswerKey) {

        //从缓存中查询数据 存入MySql
        EssayMockExamAnswerVO essayMockExamAnswerVO = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);

        if (null == essayMockExamAnswerVO) {
            log.warn("缓存中没有用户的答题卡信息，examAnswerKey :{}", examAnswerKey);
            throw new BizException(EssayErrors.NO_ANSWER_MSG_IN_REDIS);
        }
        //先判断试卷
        EssayPaperAnswer essayPaperAnswer = essayMockExamAnswerVO.getEssayPaperAnswer();
        List<EssayQuestionAnswer> essayQuestionAnswerList = essayMockExamAnswerVO.getEssayQuestionAnswerList();
        essayPaperAnswerRepository.updateById
                (essayPaperAnswer.getLastIndex(), essayPaperAnswer.getSpendTime(), essayPaperAnswer.getUnfinishedCount(), essayPaperAnswer.getBizStatus(), essayPaperAnswer.getId());


        for (EssayQuestionAnswer essayQuestionAnswer : essayQuestionAnswerList) {
            essayQuestionAnswer.setPaperId(essayPaperAnswer.getPaperBaseId());
            essayQuestionAnswerRepository.updateById
                    (CharacterUtil.removeFourChar(essayQuestionAnswer.getContent()), essayQuestionAnswer.getSpendTime(), essayQuestionAnswer.getInputWordNum(), essayQuestionAnswer.getBizStatus(), essayQuestionAnswer.getId());
        }
    }

    @Override
    public Map getHistory(int userId, int tag) {

        // 查询所有考完试的模考  缓存 3分钟过期
        List<EssayMockExam> allMockList = PaperManager.getAllFinishedMockExamList(redisTemplate, essayMockExamRepository);

        List<EssayMockExam> mockList = new ArrayList<>();

        //tag>0,按照标签查询；否则默认查询所有
        if (CollectionUtils.isNotEmpty(allMockList)) {
            if (tag > 0) {
                mockList = allMockList.stream().filter(mock -> mock.getTag() == tag).collect(Collectors.toList());
            } else {
                mockList = allMockList;
            }
        }

        //查询用户所有参加过的模考
        List<EssayPaperAnswer> userMockAnswerList = getUserMockAnswerCardList(userId);
        Map map = null;
        if (CollectionUtils.isNotEmpty(userMockAnswerList)) {
            Long lastMockId = userMockAnswerList.get(0).getPaperBaseId();
            //从缓存中获取历史数据
            String mockHistoryKey = RedisKeyConstant.getMockHistoryKey(userId, lastMockId);
            map = (Map) redisTemplate.opsForValue().get(mockHistoryKey);
            //缓存中历史数据为空
            if (true) {
                map = new HashMap();
                //从缓存中获取折线数据
                String mockLineKey = RedisKeyConstant.getMockLineKey(userId, lastMockId);
                Line line = (Line) redisTemplate.opsForValue().get(mockLineKey);

                final TreeBasedTable<Long, String, Number> basedTable = TreeBasedTable.create();
                List<MatchHistory> historyList = new LinkedList<>();
                for (EssayPaperAnswer mockPaperAnswer : userMockAnswerList) {
                    long paperId = mockPaperAnswer.getPaperBaseId();
                    EssayMockExam mock = null;
                    for (EssayMockExam mockExam : mockList) {
                        if (mockExam.getId() == paperId) {
                            mock = mockExam;
                            log.info("试题id 是:{},tag是:{}", mockExam.getId(), mock.getTag());
                            break;
                        }
                    }
                    if (mock != null) {
                        //统计折线数据
                        if (line == null) {
                            log.debug("line is null in redis,key ={}", mockLineKey);
                            //获取模考平均分
                            double avgScore = mock.getAvgScore();
                            //获取学员考试得分
                            double examScore = mockPaperAnswer.getExamScore();
                            basedTable.put(mock.getStartTime().getTime(), "模考得分", examScore);
                            basedTable.put(mock.getStartTime().getTime(), "全站平均得分", avgScore);
                        }
                        //查询模考参加人数
                        int count = mock.getExamCount();

                        MatchHistory matchHistory = MatchHistory.builder()
                                .total(count)//总人数
                                .name(mock.getName())//模考名称
                                .paperId(mock.getPracticeId())//行测模考id
                                .essayPaperId(mock.getId())//申论模考id
                                .startTime(mock.getStartTime().getTime())//开始时间
                                .courseId(mock.getCourseId())
                                .courseInfo(mock.getCourseInfo())
                                .build();

                        String practiceInfoKey = RedisKeyConstant.getPracticeInfoKey();
                        Object obj = redisTemplate.opsForHash().get(practiceInfoKey, mock.getPracticeId() + "");
                        if (null != obj) {
                            matchHistory.setName((String) obj + "-申论");
                        }

                        String userPracticeScoreKey = RedisKeyConstant.getUserPracticeScoreKey(paperId);
                        Double practiceScore = redisTemplate.opsForZSet().score(userPracticeScoreKey, userId);

                        if (practiceScore != null) {
                            matchHistory.setFlag(3);
                        } else {
                            matchHistory.setFlag(2);
                        }
                        historyList.add(matchHistory);
                    }

                }
                //倒序
                if (null != basedTable) {
                    line = table2LineSeries(basedTable);
                }

                map.put("line", line);
                map.put("list", historyList);

                redisTemplate.opsForValue().set(mockHistoryKey, map);
                redisTemplate.expire(mockHistoryKey, 3, TimeUnit.MINUTES);
            }
        }
        if (null == map) {
            map = new HashMap<>();
            map.put("line", new Line());
            map.put("list", ListUtils.EMPTY_LIST);
        }
        return map;
    }


    /**
     * 保存模考信息
     *
     * @param mockExamVO
     * @return
     */
    @Override
    public EssayMockExam saveMockPaper(EssayMockExamVO mockExamVO) {

        //保存试卷信息
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(mockExamVO.getId());
        EssayMockExam mockExam = essayMockExamRepository.findOne(mockExamVO.getId());

        if (null == essayPaperBase) {
            essayPaperBase = new EssayPaperBase();
        } else {
            //判断是否已经绑定行测考试
            if (0 != mockExam.getPracticeId()) {
                log.warn(UNCONNECTED_FIRST.getMessage());
                throw new BizException(UNCONNECTED_FIRST);
            }
        }
        BeanUtils.copyProperties(mockExamVO, essayPaperBase);
        essayPaperBase.setType(AdminPaperConstant.MOCK_PAPER);
        essayPaperBase.setStatus(EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus());
        essayPaperBase.setBizStatus(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus());
        essayPaperBase = essayPaperBaseRepository.save(essayPaperBase);
        //保存模考信息
        if (null == mockExam) {
            mockExam = new EssayMockExam();
        }

        BeanUtils.copyProperties(mockExamVO, mockExam);
        mockExam.setId(essayPaperBase.getId());
        mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus());
        mockExam.setStatus(EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());

        essayMockExamRepository.save(mockExam);
        syncPaperService.syncPaperInfo(mockExam.getId());
        return mockExam;
    }

    @Override
    public EssayMockExam updateMockStatus(int type, long practiceId, long id) {
        //操作类型     1关联 2上线    4解除绑定 5下线

        //根据行测id查询关联的申论
        List<EssayMockExam> mockExamList = essayMockExamRepository.findByPracticeId(practiceId);

        EssayMockExam mockExam = essayMockExamRepository.findOne(id);

        //4解除绑定
        if (type == 4) {
            EssayMockExam oldMockExam = mockExamList.get(0);
            //解除绑定，清空绑定信息8/
            if (id != oldMockExam.getId()) {
                //原有申论的行测id清空，状态置为INIT(0, "未关联")
                oldMockExam.setPracticeId(0);
                oldMockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus());
                essayMockExamRepository.save(oldMockExam);
                return null;
            }
        }
        if (null == mockExam) {
            log.error(EssayMockErrors.MOCK_ID_NOT_EXIST.getMessage());
            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        }
        //5下线
        if (type == 5) {
            mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.CONNECTED.getBizStatus());
            essayMockExamRepository.save(mockExam);
            return mockExam;
        }
        //判断申论是否已经绑定过其他行测
        if (0 != mockExam.getPracticeId() && practiceId != mockExam.getPracticeId()) {
            log.info(EssayMockErrors.MOCK_ALREADY_CONNECTED.getMessage());
            throw new BizException(EssayMockErrors.MOCK_ALREADY_CONNECTED);
        }
        //之前关联过申论，判断申论id是否变更
        if (type == 1 && CollectionUtils.isNotEmpty(mockExamList)) {
            EssayMockExam oldMockExam = mockExamList.get(0);
            //用户变更了行测关联的申论，将之前的申论模考置为初始状态
            if (id != oldMockExam.getId()) {
                //原有申论的行测id清空，状态置为INIT(0, "未关联")
                oldMockExam.setPracticeId(0);
                oldMockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.INIT.getBizStatus());
                essayMockExamRepository.save(oldMockExam);
            }
        }
        //如果试卷未上线，不可进行操作
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(id);
        if (null == paperBase) {
            log.error(EssayMockErrors.MOCK_ID_NOT_EXIST.getMessage());
            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        }
        if (EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus() != paperBase.getStatus()) {
            log.error(EssayMockErrors.MOCK_PAPER_UNCHECK.getMessage());
            throw new BizException(EssayMockErrors.MOCK_PAPER_UNCHECK);
        }
        if (EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()) {
            log.error(EssayMockErrors.MOCK_PAPER_OFFLINE.getMessage());
            throw new BizException(EssayMockErrors.MOCK_PAPER_OFFLINE);
        }

        //上线试卷 或 修改试卷
        int count = essayMockExamRepository.updateMockStatus(type, practiceId, id);
        if (count != 1) {
            log.error(EssayMockErrors.MOCK_CONNECTED.getMessage());
            throw new BizException(EssayMockErrors.MOCK_CONNECTED);
        }
        return mockExam;
    }


    /*模考大赛成绩报告
     * @param id
     * @param paperId
     * @return
     */
    @Override
    public MockScoreReportVO getReport(int userId, long paperId, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        EssayMockExam currentMock = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);
        if (currentMock == null) {
            log.error("2018402 paper error paperID:{} ", paperId);
            throw new BizException(EssayMockErrors.NO_REPORT);
        }
        if (System.currentTimeMillis() <= currentMock.getEndTime().getTime() + TimeUnit.MINUTES.toMillis(extraTime)) {
            //  log.info("申论报告暂未生成");
            throw new BizException(EssayMockErrors.NO_REPORT);
        }

        //从缓存中获取成绩报告
        String mockExamReportPrefix = RedisKeyConstant.getMockExamReportPrefix(paperId, userId);
        MockScoreReportVO mockScoreReportVO = null;
        try {
            mockScoreReportVO = (MockScoreReportVO) redisTemplate.opsForValue().get(mockExamReportPrefix);
            if (0L == mockScoreReportVO.getAnswerCardId()) {
                redisTemplate.delete(mockExamReportPrefix);
                mockScoreReportVO = null;
            }
        } catch (Exception e) {
            log.debug("mockScoreReportVO error,clear .....,error={}", e.getMessage());
            redisTemplate.delete(mockExamReportPrefix);
        }

        if (mockScoreReportVO == null || mockScoreReportVO.getTotalRank() == 0) {
            //折线数据
            String mockLineKey = RedisKeyConstant.getMockLineKey(userId, paperId);
            Line line = (Line) redisTemplate.opsForValue().get(mockLineKey);
            final TreeBasedTable<Long, String, Number> basedTable = TreeBasedTable.create();
            List<EssayPaperAnswer> paperAnswerList = new LinkedList<>();
            //全站最高分(先从缓存中取。再从MySql中取)
            double maxScore = 0;
            //全站总考试人数(先从缓存中取。再从MySql中取)
            int totalCount = 0;
            //用户报名地区
            long areaId = 1L;
            long submitTime = 0L;
            if (line == null) {
                //查询所有考完试的模考  缓存 3分钟过期
                List<EssayMockExam> mockList = PaperManager.getAllFinishedMockExamList(redisTemplate, essayMockExamRepository);

                //如果不包含当前模考，清空缓存，重新查询
                LinkedList<Long> mockIdList = new LinkedList<>();
                for (EssayMockExam mockExam : mockList) {
                    mockIdList.add(mockExam.getId());
                }
                if (!mockIdList.contains(paperId)) {
                    redisTemplate.delete(RedisKeyConstant.MOCK_FINISHED_EXAM_ID_LIST);
                    mockList = PaperManager.getAllFinishedMockExamList(redisTemplate, essayMockExamRepository);
                }
                //查询用户参加过的模考(根据type(模考)和userId查询)
                List<EssayPaperAnswer> userMockAnswerList = getUserMockAnswerCardList(userId);
                String userMockAnswerListKey = RedisKeyConstant.getUserMockAnswerListKey(userId);
                //如果不包含当前模考，清空缓存，重新查询
                LinkedList<Long> paperAnswerIdList = new LinkedList<>();
                for (EssayPaperAnswer paperAnswer : userMockAnswerList) {
                    paperAnswerIdList.add(paperAnswer.getPaperBaseId());
                }
                if (!paperAnswerIdList.contains(paperId)) {
                    redisTemplate.delete(userMockAnswerListKey);
                    userMockAnswerList = getUserMockAnswerCardList(userId);
                }


                for (EssayPaperAnswer mockPaperAnswer : userMockAnswerList) {
                    //从历次模考的答题卡list中，取出本次模考答题卡
                    if (mockPaperAnswer.getPaperBaseId() == paperId) {
                        areaId = mockPaperAnswer.getAreaId();
                        paperAnswerList.add(mockPaperAnswer);
                        Date time = mockPaperAnswer.getSubmitTime();
                        if(null != time){
                            submitTime = time.getTime();
                        }else{
                            submitTime = mockPaperAnswer.getGmtModify().getTime();
                        }
                    }

                    //折线图只展示此次考试及之前的信息
                    if (mockPaperAnswer.getPaperBaseId() > paperId) {
                        continue;
                    }

                    long paperBaseIdTemp = mockPaperAnswer.getPaperBaseId();
                    for (EssayMockExam mockExam : mockList) {
                        //如果是本次模考取出最高分和参加考试的总人数
                        if (mockExam.getId() == paperId) {
                            maxScore = mockExam.getMaxScore();
                            totalCount = mockExam.getExamCount();
                            submitTime = Math.min(submitTime,mockExam.getEndTime().getTime());
                        }
                        if (mockExam.getId() == paperBaseIdTemp) {
                            //获取模考平均分
                            double avgScore = mockExam.getAvgScore();
                            basedTable.put(mockExam.getStartTime().getTime(), "模考得分", mockPaperAnswer.getExamScore());
                            basedTable.put(mockExam.getStartTime().getTime(), "全站平均得分", avgScore);
                            continue;
                        }
                    }
                    //统计折线数据
                    //    log.info("line is null in redis,key ={}", mockLineKey);
                }
                line = table2LineSeries(basedTable);
            }

            //全站排名（根据zset判断）
            String essayUserScoreKey = RedisKeyConstant.getEssayUserScoreKey(paperId);
            Long totalRank = redisTemplate.opsForZSet().reverseRank(essayUserScoreKey, userId);
            Long total = redisTemplate.opsForZSet().size(essayUserScoreKey);
            if (null == totalRank) {
                totalRank = Long.parseLong("" + total);
            } else {
                totalRank = totalRank + 1;
            }
            //地区排名
            String essayUserAreaScoreKey = RedisKeyConstant.getEssayUserAreaScoreKey(paperId, areaId);
            Long areaRank = redisTemplate.opsForZSet().reverseRank(essayUserAreaScoreKey, userId);
            //根据用户申论成绩的zset的size计算得出参加考试的人数)
            long mockAreaTotalCount = redisTemplate.opsForZSet().size(essayUserAreaScoreKey);

            if (null == areaRank) {
                areaRank = mockAreaTotalCount;
            } else {
                areaRank = areaRank + 1;
            }
            log.info("用户id：{}，地区排名:{}/{},总排名：{}/{}", userId, areaRank, mockAreaTotalCount, totalRank, totalCount);

            //更新mysql中用户的地区和全站排名(没有更新过才更新)
            if (CollectionUtils.isNotEmpty(paperAnswerList)) {
                EssayPaperAnswer essayPaperAnswer = paperAnswerList.get(0);
                if (essayPaperAnswer.getAreaRank() == 0 || essayPaperAnswer.getTotalRank() == 0) {
                    essayPaperAnswer.setAreaRank(areaRank.intValue());
                    essayPaperAnswer.setTotalRank(totalRank.intValue());
                    essayPaperAnswerRepository.save(essayPaperAnswer);
                }
            }
            //查询用户答题信息
            mockScoreReportVO = getPaperAnswerReport(userId, paperId, paperAnswerList, terminal,modeTypeEnum);
            mockScoreReportVO.setLine(line);
            mockScoreReportVO.setAreaEnrollCount((int) mockAreaTotalCount);
            mockScoreReportVO.setTotalCount(totalCount);
            mockScoreReportVO.setMaxScore(maxScore);
            mockScoreReportVO.setTotalRank(totalRank.intValue());
            mockScoreReportVO.setAreaRank(areaRank.intValue());
            mockScoreReportVO.setSubmitTime(submitTime);
            if (0L == mockScoreReportVO.getAnswerCardId()) {
                List<EssayPaperAnswer> mockPaperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId, AdminPaperConstant.MOCK_PAPER,
                        EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),modeTypeEnum.getType());
                if (CollectionUtils.isNotEmpty(mockPaperAnswerList)) {
                    mockScoreReportVO.setAnswerCardId(mockPaperAnswerList.get(0).getId());

                }

            }
            redisTemplate.opsForValue().set(mockExamReportPrefix, mockScoreReportVO);
            redisTemplate.expire(mockExamReportPrefix, 5, TimeUnit.MINUTES);

        }
        return mockScoreReportVO;
    }

    /**
     * 批改完成
     *
     * @param finish
     * @param modeTypeEnum
     */
    @Override
    public void correctFinish(String finish, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        if (StringUtils.isEmpty(finish)) {
            log.warn("参数异常。答题卡批改完成消息队列处理失败");
            throw new BizException(EssayMockErrors.MQ_MSG_ERROR);
        }
        String[] params = finish.split("and");
        int userId = Integer.parseInt(params[0]);
        double examScore = Double.parseDouble(params[1]);
        String paperIdStr = params[2];
        log.info("cone:examSore:{},userId={},paperIdStr={}", examScore, userId, paperIdStr);
        long paperId = Long.parseLong(paperIdStr);

        //将用户成绩报告中试卷和试题相关信息 放入缓存
        MockScoreReportVO paperAnswerReport = getPaperAnswerReport(userId, paperId, null, 0, modeTypeEnum);
        long areaId = paperAnswerReport.getAreaId();
        // 将用户id从三方共用的set中移除
        String publicUserSetPrefix = RedisKeyConstant.getPublicUserSetPrefix(paperId);
        redisTemplate.opsForSet().remove(publicUserSetPrefix, userId);
        log.info("cone:set:remove:{}->{}", publicUserSetPrefix, userId);
        //修改用户答题卡状态
        String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperId);
        redisTemplate.opsForHash().put(userAnswerStatusKey, userId + "", EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        log.info("cone:hash:put:{}->{}", userAnswerStatusKey, userId, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());


        //申论成绩
        String essayUserScoreKey = RedisKeyConstant.getEssayUserScoreKey(paperId);
        Double score = redisTemplate.opsForZSet().score(essayUserScoreKey, userId);
        log.info("cone:zset:getScore:{}->{},{}", essayUserScoreKey, userId, score);
        if (score == null) {
            score = 0D;
        }
        redisTemplate.opsForZSet().add(essayUserScoreKey, userId, examScore);
        log.info("cone:zset:add:{}->{},{}", essayUserScoreKey, userId, examScore);
        //申论总成绩
        String essayScoreSumKey = RedisKeyConstant.getEssayScoreSumKey(paperId);
        double scoreMin = examScore - score;
        redisTemplate.opsForValue().increment(essayScoreSumKey, scoreMin);
        log.info("cone:value:sum:{}->{}", essayScoreSumKey, examScore - score);
        //申论成绩（地区）
        String mockUserAreaScoreKey = RedisKeyConstant.getEssayUserAreaScoreKey(paperId, areaId);
        redisTemplate.opsForZSet().add(mockUserAreaScoreKey, userId, examScore);
        log.info("cone:zset:add:{}->{},{}", mockUserAreaScoreKey, userId, examScore);
        //模考总成绩(根据用户查询行测成绩，能查到再算出总成绩，放入对应的Zset)
        //查询行测成绩
        String userPracticeScoreKey = RedisKeyConstant.getUserPracticeScoreKey(paperId);
        Double practiceScore = redisTemplate.opsForZSet().score(userPracticeScoreKey, userId);
        log.info("cone:zset:getScore:{}->{},{}", userPracticeScoreKey, userId, practiceScore);
        if (null != practiceScore) {
            //用户总成绩
            String mockUserTotalScoreKey = RedisKeyConstant.getMockUserTotalScoreKey(paperId);
            Double totalScore = redisTemplate.opsForZSet().score(mockUserTotalScoreKey, userId);
            //模考总成绩
            String mockScoreSumKey = RedisKeyConstant.getMockScoreSumKey(paperId);

            if (totalScore != null && totalScore != 0) {
                redisTemplate.opsForValue().increment(mockScoreSumKey, scoreMin);
                log.info("cone:value:sum:{}->{}", mockScoreSumKey, scoreMin);
            } else if (totalScore == null || totalScore == 0) {
                redisTemplate.opsForValue().increment(mockScoreSumKey, examScore + practiceScore);
                log.info("cone:value:sum:{}->{}", mockScoreSumKey, examScore + practiceScore);
            }
            log.info("cone:zset:getScore:{}->{},{}", mockUserTotalScoreKey, userId, mockUserAreaScoreKey);
            redisTemplate.opsForZSet().add(mockUserTotalScoreKey, userId, examScore + practiceScore);
            log.info("cone:zset:add:{}->{},{}", mockUserTotalScoreKey, userId, examScore + practiceScore);
            //总成绩（地区）
            String mockUserAreaTotalScoreKey = RedisKeyConstant.getMockUserAreaTotalScoreKey(paperId, areaId);
            redisTemplate.opsForZSet().add(mockUserAreaTotalScoreKey, userId, examScore + practiceScore);
            log.info("cone:zset:add:{}->{},{}", mockUserAreaTotalScoreKey, userId, examScore + practiceScore);

        }

    }

    /**
     * 查询模考信息
     *
     * @param paperId
     * @return
     */
    @Override
    public EssayMockExam getMock(long paperId) {

        EssayMockExam essayMockExam = essayMockExamRepository.findOne(paperId);

        return essayMockExam;

    }


    /**
     * 查询成绩报告中试卷和试题相关信息
     */
    private MockScoreReportVO getPaperAnswerReport(int userId, long paperId, List<EssayPaperAnswer> paperAnswerList, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        MockScoreReportVO mockScoreReportVO = new MockScoreReportVO();
        //MySQL查询答题卡信息
        if (CollectionUtils.isEmpty(paperAnswerList)) {
            paperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndBizStatusAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                    (userId,
                            paperId,
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                            EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                            AdminPaperConstant.MOCK_PAPER,
                            modeTypeEnum.getType()
                    );
        }
        //试卷报告信息
        if (CollectionUtils.isNotEmpty(paperAnswerList)) {
            EssayPaperAnswer essayPaperAnswer = paperAnswerList.get(0);
            BeanUtils.copyProperties(essayPaperAnswer, mockScoreReportVO);
            mockScoreReportVO.setAnswerCardId(essayPaperAnswer.getId());

            //试题报告信息
            List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndUserIdAndStatus(essayPaperAnswer.getId(),
                    userId,
                    EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus()
                    );
            //试卷的试题信息(走缓存)
            List<EssayQuestionRedisVO> questionList = getQuestionList(paperId);
            if (CollectionUtils.isNotEmpty(questionAnswerList)) {
                LinkedList<MockQuestionAnswerVO> mockQuestionAnswerVOList = new LinkedList<>();
                for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
                    MockQuestionAnswerVO mockQuestionAnswerVO = new MockQuestionAnswerVO();
                    BeanUtils.copyProperties(questionAnswer, mockQuestionAnswerVO);
                    for (EssayQuestionRedisVO base : questionList) {
                        if (base.getBaseId() == questionAnswer.getQuestionBaseId()) {
                            mockQuestionAnswerVO.setType(base.getType());
                            mockQuestionAnswerVO.setSort(base.getSort());
                            //!!! 修复IOS的bug，模考查看详情的时候，试题类型取了Sort字段
                            /**
                             * 7.0已修复，去掉这里的逻辑
                             */
//                            if(terminal == 0){
//                                terminal = questionAnswer.getTerminal();
//                            }
//                            if(terminal == TerminalType.IPHONE  || terminal == TerminalType.IPHONE_IPAD ){
//                                mockQuestionAnswerVO.setSort(mockQuestionAnswerVO.getType());
//                            }
                            /**
                             * 7.0 不展示二级类目，所以id大于5的（即2综合分析的下级，只展示综合分析）
                             * up by: zhaoxi
                             */
                            if (mockQuestionAnswerVO.getType() > 5) {
                                mockQuestionAnswerVO.setType(2);
                            }
                            mockQuestionAnswerVOList.add(mockQuestionAnswerVO);
                        }
                    }
                }
                mockScoreReportVO.setQuestionList(mockQuestionAnswerVOList);
            }
        }
        return mockScoreReportVO;
    }


    /**
     * 查看用户模考报名地区
     *
     * @param paperBaseId
     * @param userId
     * @return
     */
    private long getMockUserArea(Long paperBaseId, Integer userId) {
        String mockUserAreaKey = RedisKeyConstant.getMockUserAreaPrefix(paperBaseId);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Object areaIdObj = hashOperations.get(mockUserAreaKey, userId + "");
        if (areaIdObj != null) {
            return Long.parseLong(areaIdObj.toString());
        }
        //默认报名北京
        return 1;

    }


    /**
     * 查询模考平均分
     */
    private double getAvgScore(long paperId) {
        //根据模考查询平均分
        String mockDetailPrefixKey = RedisKeyConstant.getMockDetailPrefix(paperId);
        EssayMockExam mockExam = (EssayMockExam) redisTemplate.opsForValue().get(mockDetailPrefixKey);
        double avgScore = 0;
        if (null != mockExam) {
            avgScore = mockExam.getAvgScore();
        } else {
            mockExam = essayMockExamRepository.findOne(paperId);
            avgScore = mockExam.getAvgScore();
            redisTemplate.opsForValue().set(mockDetailPrefixKey, mockExam);
            redisTemplate.expire(mockDetailPrefixKey, 1, TimeUnit.DAYS);
        }
        return avgScore;
    }

//
//    /**
//     * 查询学员模考得分
//     */
//    private double getExamScore(long paperId, int userId) {
//        Double examScore = 0D;
//        //去缓存中取用户的答题卡数据(用户得分)
//        String userAnswerScoreKey = RedisKeyConstant.getEssayUserScoreKey(paperId);
//        examScore = redisTemplate.opsForZSet().score(userAnswerScoreKey, userId);
//        if (examScore == null || examScore.intValue() == 0) {
//            List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndAnswerCardTypeOrderByGmtCreateDesc(userId, paperId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
//            if (CollectionUtils.isNotEmpty(paperAnswerList)) {
//                EssayPaperAnswer essayPaperAnswer = paperAnswerList.get(0);
//                examScore = essayPaperAnswer.getExamScore();
//            }
//            if (examScore != null && examScore.intValue() != 0) {
//                redisTemplate.opsForZSet().add(userAnswerScoreKey, userId, examScore);
//            }
//        }
//        if (null == examScore) {
//            examScore = 0D;
//        }
//        return examScore;
//    }

//    /**
//     * 查询全站最高分 （结束后15分钟计算得出平均分，存入redis）
//     */
//    private double getMaxScore(long paperId) {
//
//        //优先从缓存中取
//        double maxScore = 0;
//        String mockDetailKey = RedisKeyConstant.getMockDetailPrefix(paperId);
//        EssayMockExam mockExam = (EssayMockExam) redisTemplate.opsForValue().get(mockDetailKey);
//
//        if (null != mockExam && mockExam.getMaxScore() != 0) {
//            maxScore = mockExam.getMaxScore();
//        } else {
//            mockExam = essayMockExamRepository.findOne(paperId);
//            maxScore = mockExam.getMaxScore();
//            redisTemplate.opsForValue().set(mockDetailKey, mockExam);
//        }
//        return maxScore;
//    }

//
//    /**
//     * 查询总考试人数
//     */
//    private int getTotalCount(long paperId) {
//
//        //优先从缓存中取
//        int count = 0;
//        String mockDetailKey = RedisKeyConstant.getMockDetailPrefix(paperId);
//        EssayMockExam mockExam = (EssayMockExam) redisTemplate.opsForValue().get(mockDetailKey);
//
//        if (null != mockExam && mockExam.getMaxScore() != 0) {
//            count = mockExam.getExamCount();
//        } else {
//            mockExam = essayMockExamRepository.findOne(paperId);
//            count = mockExam.getExamCount();
//            redisTemplate.opsForValue().set(mockDetailKey, mockExam);
//        }
//        return count;
//    }

    /**
     * 数据转换成 line
     *
     * @param table
     * @return
     */
    private static final Line table2LineSeries(TreeBasedTable<Long, String, ? extends Number> table) {
        final Set<String> columnKeySet = table.columnKeySet();
        final Set<Long> rowKeySet = table.rowKeySet();
        List<LineSeries> seriesList = new ArrayList<>(rowKeySet.size());
        for (Long dateStamp : rowKeySet) {
            List data = new ArrayList(columnKeySet.size());
            List<String> strData = new ArrayList<>(columnKeySet.size());
            for (String column : columnKeySet) {
                Number number = table.get(dateStamp, column);
                if (number == null) {//为空则进行初始化
                    number = Double.valueOf(0);
                }
                data.add(number);
                strData.add(String.valueOf(number));
            }

            final LineSeries lineSeries = LineSeries.builder()
                    .name(DateFormatUtils.format(dateStamp, "M-d"))
                    .data(data)
                    .strData(strData)
                    .build();
            seriesList.add(lineSeries);
        }

        final Line line = Line.builder()
                .categories(Lists.newArrayList(columnKeySet))
                .series(seriesList)
                .build();

        return line;
    }


    /**
     * 查询往期试卷
     *
     * @param userId
     * @param modeTypeEnum
     * @return
     */
    @Override
    public PageUtil<EssayPaperVO> getHistoryPaperList(int userId, int page, int pageSize, int tag, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int count = essayMockExamRepository.countByBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus());
        List<EssayPaperVO> paperVOList = Lists.newArrayList();

        if (count > 0) {
            String pastMockKey = MatchRedisKeyConstant.getPastMockKey(page, tag);
            log.info("往期模考pastMockKey是 :{}", pastMockKey);
            paperVOList = (List<EssayPaperVO>) redisTemplate.opsForValue().get(pastMockKey);

            if (CollectionUtils.isEmpty(paperVOList)) {
                PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtModify");

                //先查询所有已经完成的试卷列表
                List<EssayMockExam> essayMockExamList = new ArrayList<>();
                //tag>0,按照tag 查询;否则默认查询所有的
                if (tag > 0) {
                    essayMockExamList = essayMockExamRepository.findByBizStatusAndTag(EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus(), tag, pageRequest);
                } else {
                    essayMockExamList = essayMockExamRepository.findByBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus(), pageRequest);
                }
                //校验状态
                paperVOList = new LinkedList<>();

                for (EssayMockExam mockExam : essayMockExamList) {
                    EssayPaperBase paperBase = essayPaperBaseRepository.findOne(mockExam.getId());

                    String paperName = "";
                    //模考试卷名称
                    String practiceInfoKey = RedisKeyConstant.getPracticeInfoKey();
                    Object obj = redisTemplate.opsForHash().get(practiceInfoKey, mockExam.getPracticeId() + "");
                    if (null != obj) {
                        paperName = obj.toString() + "-申论";
                    } else {
                        paperName = paperBase.getName();
                    }
                    //根据试卷id查询试卷下题目数
                    int totalCount = essayQuestionBaseRepository.countByPaperId(mockExam.getId());
                    EssayPaperVO paperVO = EssayPaperVO.builder().paperName(paperName)
                            .totalCount(totalCount)
                            .limitTime(paperBase.getLimitTime())
                            .paperId(mockExam.getId())
                            .correctSum(essayPaperAnswerRepository.countByPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardType(paperBase.getId(),
                                    AdminPaperConstant.TRUE_PAPER,
                                    EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                    modeTypeEnum.getType()).intValue())
                            .courseId(mockExam.getCourseId())
                            .courseInfo(mockExam.getCourseInfo())
                            .build();
                    paperVOList.add(paperVO);
                }
                if (CollectionUtils.isNotEmpty(paperVOList)) {
                    redisTemplate.opsForValue().set(pastMockKey, paperVOList, 5, TimeUnit.MINUTES);
                }
            }

            if (CollectionUtils.isNotEmpty(paperVOList)) {
                for (EssayPaperVO paperVO : paperVOList) {
                    //根据试卷id查询用户作答记录
                    List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc
                            (userId, paperVO.getPaperId(), AdminPaperConstant.TRUE_PAPER, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                                    modeTypeEnum.getType());
                    if (CollectionUtils.isNotEmpty(answerList)) {

                        if (answerList.size() != 1) {
                            paperVO.setCorrectNum(answerList.size());
                        } else {
                            EssayPaperAnswer essayPaperAnswer = answerList.get(0);
                            paperVO.setRecentStatus(essayPaperAnswer.getBizStatus());
                            if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == essayPaperAnswer.getBizStatus()) {
                                paperVO.setCorrectNum(1);
                            }
                        }
                    }
                }
            }
        }


        PageUtil pageUtil = PageUtil.builder().result(paperVOList).next(((int) count) > page * pageSize ? 1 : 0).build();
        return pageUtil;
    }


    @Override
    public Object handCommit(int userId, long paperId) {

        String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperId);
        //修改为交卷状态
        redisTemplate.opsForHash().put(userAnswerStatusKey, userId + "", EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());

        String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
        EssayMockExamAnswerVO essayMockExamAnswerVO = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);

        EssayMockVO essayMockVO = EssayMockVO.builder()
                .answerCardId(essayMockExamAnswerVO.getEssayPaperAnswer().getId())
                .paperId(paperId)
                .examType(AdminPaperConstant.MOCK_PAPER)
                .mockRedisKey(examAnswerKey)
                .userId(userId)
                .build();

        log.info("=====进入批改试卷接口【模考】，发送消息到消息队列:" + essayMockVO + "=========");
        rabbitTemplate.convertAndSend(SystemConstant.MOCK_ANSWER_CORRECT_ROUTING_KEY, essayMockVO);
        //考试结束15分钟之内交卷  才放入共用缓存（避免影响到查看报告按钮）
        EssayMockExam essayMockExam = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);

        if (System.currentTimeMillis() <= essayMockExam.getEndTime().getTime() + TimeUnit.MINUTES.toMillis(extraTime)) {
            //将userId放入三方公用的set中
            String publicUserSetPrefix = RedisKeyConstant.getPublicUserSetPrefix(paperId);
            redisTemplate.opsForSet().add(publicUserSetPrefix, userId);
        }
        //2-MQ:将答题数据持久化存入MySQL
        asyncMockServiceImpl.saveMockAnswerToMySql(examAnswerKey);


        return "手动提交答题卡成功";
    }

    /**
     * 查询用户参加过模考的答题卡信息（时间降序）
     *
     * @param userId
     * @return
     */
    private List<EssayPaperAnswer> getUserMockAnswerCardList(int userId) {

        String userMockAnswerListKey = RedisKeyConstant.getUserMockAnswerListKey(userId);

        List<EssayPaperAnswer> userMockAnswerList;
        Object object = redisTemplate.opsForValue().get(userMockAnswerListKey);
        if (object != null) {
            return (List<EssayPaperAnswer>) object;
        } else {
            userMockAnswerList = essayPaperAnswerRepository.findByUserIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtCreateDesc
                    (userId, AdminPaperConstant.MOCK_PAPER,
                            EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                            EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());

            redisTemplate.opsForValue().set(userMockAnswerListKey, userMockAnswerList);
            redisTemplate.expire(userMockAnswerListKey, 10, TimeUnit.MINUTES);
        }
        return userMockAnswerList;
    }

    /**
     * 处理字符串分数
     *
     * @param essayAnswerCardVO
     * @return
     */
    public EssayAnswerCardVO dealAnswerCardScore(EssayAnswerCardVO essayAnswerCardVO) {
        if (null != essayAnswerCardVO) {
            essayAnswerCardVO.setScoreStr(essayAnswerCardVO.getScore() + "");
            double average = essayAnswerCardVO.getCardUserMeta().getAverage();
            double max = essayAnswerCardVO.getCardUserMeta().getMax();
            essayAnswerCardVO.getCardUserMeta().setAverageStr(average + "");
            essayAnswerCardVO.getCardUserMeta().setMaxStr(max + "");
        }
        return essayAnswerCardVO;
    }

    @Override
    public EssayMockExam getMockDetail(long paperId) {
        EssayMockExam essayMockExam = PaperManager.getMockDetail(paperId, redisTemplate, essayMockExamRepository, mockRedisExpireTime);
        return essayMockExam;
    }
    
}
