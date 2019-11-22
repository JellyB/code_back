package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.CommonErrors;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.dto.ImageSortDto;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum.DelayStatusEnum;
import com.huatu.tiku.essay.manager.PaperManager;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.service.impl.correct.UserOrderUtil;
import com.huatu.tiku.essay.service.task.AsyncFileSaveServiceImpl;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.util.sensors.SensorsUtils;
import com.huatu.tiku.essay.util.sign.OCRSign;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.correct.PhotoDistinguishVo;
import com.huatu.tiku.essay.vo.file.YoutuMQVO;
import com.huatu.tiku.essay.vo.file.YoutuVO;
import com.huatu.tiku.essay.vo.redis.EssayQuestionRedisVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.*;
import static com.huatu.tiku.essay.util.file.FunFileUtils.MANUAL_CORRECT_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.MANUAL_CORRECT_SAVE_URL;


@Service
@Slf4j
public class UserAnswerServiceImpl implements UserAnswerService {
    @Autowired
    SensorsAnalytics sensorsAnalytics;
    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    private EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    private EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;
    @Autowired
    private EssayUserAnswerQuestionDetailedScoreRepository essayUserAnswerQuestionDetailedScoreRepository;
    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    private EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    private EssayCorrectImageRepository essayCorrectImageRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Value("${answer_correct_url}")
    private String answerCorrectUrl;
    @Value("${answer_correct_url_V2}")
    private String answerCorrectUrlV2;
    @Value("${max_correct_time}")
    private Integer maxCorrectTime;
    @Value("${mock_redis_expire_time}")
    private int mockRedisExpireTime;
    @Value("${paper_report_start_time}")
    private long paperReportStartTime;
    @Autowired
    private UploadFileUtil uploadFileUtil;
    @Autowired
    private EssayPhotoAnswerRepository essayPhotoAnswerRepository;
    @Autowired
    private AsyncFileSaveServiceImpl asyncFileSaveService;
    @Autowired
    private BjyHandler bjyHandler;
    @Value("${OCRAppID}")
    private long OCRAppID;
    @Value("${OCRSecretID}")
    private String OCRSecretID;
    @Value("${OCRSecretKey}")
    private String OCRSecretKey;

    @Autowired
    private UserCorrectGoodsServiceV4 userCorrectGoodsServiceV4;

    @Autowired
    private CorrectOrderService correctOrderService;
    @Autowired
    private QuestionTypeService questionTypeService;
    @Autowired
    private CorrectOrderRepository correctOrderRepository;


    //该用户该题是否可以再次进行批改
    private static final int CAN_CORRECT = 0;
    private static final int CANNOT_CORRECT = 1;
    private static final String ESSAY_GOODS_FREE_KEY = "essay_goods_free";
    private static final int ESSAY_GOODS_FREE = 1;
    private static final int ESSAY_GOODS_NOT_FREE = 0;

    @Autowired
    private EssayCorrectFreeUserRepository essayCorrectFreeUserRepository;
    @Autowired
    private RestTemplate restTemplate;

    //缓存题目对应的材料列表
    private static final Cache<Integer, List<EssayAnswerVO>> singleCorrectListCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)//缓存时间
                    .maximumSize(2)
                    .build();

    @Override
    public ResponseVO createAnswerCard(int userId, CreateAnswerCardVO createAnswerCardVO, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        Integer type = createAnswerCardVO.getType();//题目类型   0单题  1套题
        Long questionBaseId = createAnswerCardVO.getQuestionBaseId();//题目的baseId
        Long paperBaseId = createAnswerCardVO.getPaperBaseId();//试卷baseId

        ResponseVO vo = new ResponseVO();
        //判断是是单题还是试卷(答题卡类型  0 单题  1 试卷  )
        long answerId = 0;
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            //判断用户是否该题已存在答题卡（未完成）
            int unfinishedCount = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(userId,
                    questionBaseId,
                    0,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                    modeTypeEnum.getType());
            if (0 != unfinishedCount) {
                log.info("该题存在未完成的答题卡，答题卡创建失败");
                throw new BizException(EssayErrors.UNFINISHED_PAPER);
            }

            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
            if (null == questionBase) {
                log.info("试卷不存在 ,questionBaseId {}" + questionBaseId);
                throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
            }
            long questionDetailId = questionBase.getDetailId();//题目的detailId
            EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(questionDetailId);
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                    .userId(userId)
                    .terminal(terminal)
                    .questionType(essayQuestionDetail.getType())
                    .areaId(questionBase.getAreaId())
                    .areaName(questionBase.getAreaName())
                    .questionBaseId(questionBaseId)
                    .questionYear(questionBase.getQuestionYear())
                    .questionDetailId(questionDetailId)
                    .score(essayQuestionDetail.getScore())
                    .paperId(0)//单题答题卡对应的paperId是0
                    .labelStatus(0)
                    .subScore(-1D)
                    .correctType(essayQuestionDetail.getCorrectType())
                    .subScoreRatio(0D)
                    .build();
            essayQuestionAnswer.setCreator(userId + "");
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayQuestionAnswerRepository.save(essayQuestionAnswer);
            answerId = essayQuestionAnswer.getId();
        } else {

            //判断用户是否该试卷已存在答题卡（未完成）
            int unfinishedCount = essayPaperAnswerRepository.countByUserIdAndPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc
                    (userId,
                            paperBaseId,
                            AdminPaperConstant.TRUE_PAPER,
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                            modeTypeEnum.getType());

            if (0 != unfinishedCount) {
                log.info("该题存在未完成的答题卡，答题卡创建失败");
                throw new BizException(EssayErrors.UNFINISHED_PAPER);
            }
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperBaseId);
            if (null == paperBase) {
                log.info("答题卡创建失败,试卷信息获取为空 ==>>,userId = {},terminal => {},createAnswerCardVO => {}", userId, terminal, createAnswerCardVO);
                throw new BizException(ErrorResult.create(5000513, "获取试卷信息失败"));
            }
            //如果是估分试卷，不允许答题
            if (9999 == paperBase.getAreaId()) {
                log.info("估分试卷暂不支持作答，paperBaseId：{}", paperBaseId);
                throw new BizException(EssayErrors.GUFEN_CANOT_ANSWER);
            }
            //如果是模考，判断是否是已结束
            if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
                EssayMockExam essayMockExam = essayMockExamRepository.findOne(paperBaseId);
                if (essayMockExam.getEndTime().getTime() > System.currentTimeMillis()) {
                    log.info("当前模考暂未结束，暂时不可答题");
                    throw new BizException(EssayErrors.MOCK_NOT_FINISH_CANOT_ANSWER);
                }
            }
            int questionCountByPaper = essayQuestionBaseRepository.countByPaperId(paperBaseId);
            EssayPaperAnswer essayPaperAnswer = EssayPaperAnswer.builder()
                    .paperBaseId(paperBaseId)
                    .areaId(paperBase.getAreaId())
                    .areaName(paperBase.getAreaName())
                    .name(paperBase.getName())
                    .score(paperBase.getScore())
                    .userId(userId)
                    .unfinishedCount(questionCountByPaper)
                    .type(AdminPaperConstant.TRUE_PAPER)//真题
                    .build();
            if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
                //0226 名字是行测考试的名字-申论
                EssayPaperBase mock = PaperManager.getPaperBase(essayPaperBaseRepository, essayMockExamRepository, redisTemplate, paperBaseId, mockRedisExpireTime);
                essayPaperAnswer.setName(mock.getName());
            }
            essayPaperAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayPaperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayPaperAnswer.setCreator(userId + "");
            essayPaperAnswer = essayPaperAnswerRepository.save(essayPaperAnswer);
            answerId = essayPaperAnswer.getId();
        }
        if (0 != answerId) {
            vo.setAnswerCardId(answerId);
        }
        return vo;
    }

    @Override
    public List<EssayAnswerVO> correctList(int userId, Integer type, Pageable pageRequest, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        List<EssayAnswerVO> list = new LinkedList<EssayAnswerVO>();
        //判断是是单题还是试卷( 0 单题  1 试卷  )
        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());

        if (SINGLE_QUESTION == type) {
            //查询EssayQuestionAnswer表
            List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndStatusAndBizStatusInAndAnswerCardType
                    (userId, 0L, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList,
                            modeTypeEnum.getType(), pageRequest);
            for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
                EssayAnswerVO answerVO = EssayAnswerVO.builder()
                        .questionDetailId(questionAnswer.getQuestionDetailId())//detail试题id
                        .questionBaseId(questionAnswer.getQuestionBaseId())//base试题id
                        .correctDate(DateUtil.convertDateFormat(questionAnswer.getCorrectDate()))//批改时间
                        .examScore(questionAnswer.getExamScore())//学员得分
                        .score(questionAnswer.getScore())//题目总分
                        .answerId(questionAnswer.getId())//答题卡id
                        .bizStatus(questionAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                        .build();

                //题干信息
//                EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());

                List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionAnswer.getQuestionBaseId(), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
                if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                    long similarId = similarQuestionList.get(0).getSimilarId();
                    EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.getOne(similarId);
                    if (similarQuestionGroupInfo != null) {
                        answerVO.setSimilarId(similarQuestionGroupInfo.getId());
                        answerVO.setStem(similarQuestionGroupInfo.getShowMsg());
                        answerVO.setQuestionType(similarQuestionGroupInfo.getType());
                    }
                }

                //所属地区
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
                if (essayQuestionBase != null) {
                    if (StringUtils.isEmpty(essayQuestionBase.getSubAreaName())) {
                        answerVO.setAreaId(essayQuestionBase.getAreaId());//地区id
                        answerVO.setAreaName(essayQuestionBase.getAreaName());//地区名称
                    } else {
                        answerVO.setAreaId(essayQuestionBase.getSubAreaId());//子地区id
                        answerVO.setAreaName(essayQuestionBase.getSubAreaName());//子地区名称
                    }
                }
                list.add(answerVO);
            }
        } else {

            List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndStatusAndAnswerCardTypeAndTypeAndBizStatusIn
                    (userId,
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            modeTypeEnum.getType(),
                            AdminPaperConstant.TRUE_PAPER,
                            bizStatusList,
                            pageRequest);

            for (EssayPaperAnswer paperAnswer : paperAnswerList) {
                EssayAnswerVO answerVO = EssayAnswerVO.builder()
                        .paperName(paperAnswer.getName())//试卷名称
                        .paperId(paperAnswer.getPaperBaseId())//base试卷id
                        .correctDate(DateUtil.convertDateFormat(paperAnswer.getCorrectDate()))//批改时间
                        .examScore(paperAnswer.getExamScore())//学员得分
                        .score(paperAnswer.getScore())//试卷总分
                        .answerId(paperAnswer.getId())
                        .bizStatus(paperAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                        .paperReportFlag((null != paperAnswer.getCorrectDate() && paperAnswer.getCorrectDate().getTime() > paperReportStartTime) ? true : false)
                        .build();

                //所属地区
                EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
                if (essayPaperBase != null) {
                    answerVO.setAreaId(essayPaperBase.getAreaId());//地区id
                    answerVO.setAreaName(essayPaperBase.getAreaName());//地区名称
                }
                answerVO.setVideoAnalyzeFlag(essayPaperBase.getVideoAnalyzeFlag());
                list.add(answerVO);
            }
        }
        return list;
    }

    @Override
    public long countCorrectList(int userId, int type, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        if (SINGLE_QUESTION == type) {
            return essayQuestionAnswerRepository.countByUserIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(userId,
                    0,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                    modeTypeEnum.getType());
        } else {
            return essayPaperAnswerRepository.countByUserIdAndStatusAndAnswerCardTypeAndBizStatus(userId,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    modeTypeEnum.getType(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        }

    }

    /**
     * 接口升级，议论文的批改次数相关单独处理
     */
    private boolean singleQuestionCommit(List<PaperCommitAnswerVO> answerList, Long answerCardId, PaperCommitVO paperCommitVO, UserSession userSession, String essayGoodsFree, int type, int terminal) {
        //操作类型(0 保存  1交卷)
        int saveType = paperCommitVO.getSaveType();
        int spendTime = paperCommitVO.getSpendTime();
        //根据答题卡id查询答题卡信息
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerCardId);
        if (null == questionAnswer) {
            log.warn("答题卡ID 有误 answerCardId：{}", answerCardId);
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (null != questionAnswer.getAnswerCardType() && questionAnswer.getAnswerCardType().intValue() == 2) {
            essayGoodsFree = ESSAY_GOODS_FREE + "";
        }
        int questionType = questionAnswer.getQuestionType();
        if (questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
//            log.warn("请求参数错误,试题已提交或已批改。answerCardId：{}",answerCardId );
//            throw new BizException(EssayErrors.ANSWER_CARD_COMMIT);
            //返回批改详情，不抛异常
            return true;
        }

        long questionBaseId = questionAnswer.getQuestionBaseId();
        long questionDetailId = questionAnswer.getQuestionDetailId();
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findById(questionDetailId);
        if (null == questionBase || null == essayQuestionDetail) {
            log.warn("试题id有误 questionBaseId:{},questionDetailId:{}", questionBaseId, questionDetailId);
            throw new BizException(EssayErrors.ESSAY_QUESTION_BASE_ID_NULL);
        } else {
//            if (EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus() != questionBase.getBizStatus()
//                    || EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus() != questionBase.getStatus()) {
//                log.warn("试题已下线或已删除，交卷失败  questionBaseId: {}", questionBaseId);
//                throw new BizException(EssayErrors.ESSAY_QUESTION_OFFLINE);
//            }
        }

        if (CollectionUtils.isNotEmpty(answerList) && null != answerList.get(0)) {
            questionAnswer.setContent(answerList.get(0).getContent());//学员答案
            questionAnswer.setInputWordNum(answerList.get(0).getInputWordNum());
            if (StringUtils.isNotEmpty(answerList.get(0).getFileName())) {
                questionAnswer.setFileName(answerList.get(0).getFileName());
            }

        }
        questionAnswer.setCorrectType(essayQuestionDetail.getCorrectType());
        questionAnswer.setSpendTime(spendTime);//答题时间
        questionAnswer.setSpeed(spendTime);//答题速度

        if (AnswerSaveTypeConstant.SAVE == saveType) {
            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
        } else if (AnswerSaveTypeConstant.COMMIT == saveType) {
            //校验用户该题批改了几次
            ResponseVO responseVO = correctCount(userSession.getId(), type, questionAnswer.getQuestionBaseId(), EssayAnswerCardEnum.ModeTypeEnum.create(questionAnswer.getAnswerCardType()));
            if (CANNOT_CORRECT == responseVO.getCanCorrect()) {
                log.info("已批改" + maxCorrectTime + "次，无法交卷。 questionBaseId {}" + questionAnswer.getQuestionBaseId());
                throw new BizException(ErrorResult.create(1000509, "已批改" + maxCorrectTime + "次，无法交卷"));
            }
            //bizStatus改为 ：已经提交
            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
            boolean NOT_CORRECT_FREE_USER = true;
            //查询白名单用户
            List<Integer> freeUserIdList = essayCorrectFreeUserRepository.findUserIdByStatusAndBizStatus
                    (EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
            if (freeUserIdList.contains(userSession.getId())) {
                NOT_CORRECT_FREE_USER = false;
            }
            //当前批改不是免费，并且用户不在白名单里才扣除批改次数 0206
            if (ESSAY_GOODS_NOT_FREE == Integer.parseInt(essayGoodsFree) && NOT_CORRECT_FREE_USER) {
                // 修改批改次数
                int i = 0;
                //题目不是议论文，扣除单题批改次数
                if (questionType != 5) {
                    i = essayUserCorrectGoodsRepository.modifyUsefulNumByUserIdAndType(1, userSession.getId(), SINGLE_QUESTION);
                } else {
                    //议论文扣除议论文的批改次数
                    i = essayUserCorrectGoodsRepository.modifyUsefulNumByUserIdAndType(1, userSession.getId(), ARGUMENTATION);
                }
                if (1 != i) {
                    log.info("批改次数不足，更改用户批改次数错误。userId：{}，type：{}", userSession.getId(), SINGLE_QUESTION);
                    throw new BizException(EssayErrors.LOW_CORRECT_TIMES);
                }
            }
        } else if (AnswerSaveTypeConstant.UPDATE_TIME == saveType) {
            questionAnswer.setSpendTime(spendTime);
        } else {
            log.info("答题卡保存类型有误 saveType ：{}:" + saveType);
            throw new BizException(EssayErrors.ANSWER_CARD_SAVE_TYPE_ERROR);
        }
        essayQuestionAnswerRepository.save(questionAnswer);
        long questionAnswerId = questionAnswer.getId();

        questionAnswer = new EssayQuestionAnswer();
        questionAnswer.setId(questionAnswerId);
        if (AnswerSaveTypeConstant.COMMIT == saveType) {
            //调用批改试题接口
            try {
                Map params = new HashMap<>();
                params.put("answerCardId", answerCardId);
                params.put("type", SINGLE_QUESTION);
                HttpEntity<Map> request = new HttpEntity<>(params, null); //组装
                log.info("批改单题:post 请求发送");
                String url = answerCorrectUrl + "?answerCardId=" + answerCardId + "&type=" + SINGLE_QUESTION;
                long start = System.currentTimeMillis();
                ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);


                if (null != body) {
                    long end = System.currentTimeMillis();
                    log.info("批改单题:post 请求发送成功");
                    log.info("请求用时：" + (end - start) + "毫秒");
                    if (1000000 == body.getCode()) {
                        if (null != body.getData() && (Boolean) body.getData()) {
                            /**
                             * 神策埋点上报交卷成功
                             */
                            essayCommitSucceedAnalytics(userSession.getUcId(), terminal, questionBaseId, "", questionType == 5 ? ARGUMENTATION : SINGLE_QUESTION);
                            log.info("批改单题增加图币,questionType:{}", questionType);
                            //单题增加金币
                            eventPublisher.publishEvent(RewardActionEvent.class,
                                    this,
                                    (event) -> event.setAction(
                                            (questionType == 5) ? RewardAction.ActionType.SL_CORR_ARGUMENT : RewardAction.ActionType.SL_CORR_STANDARD)
                                            .setUid(userSession.getId())
                                            .setUname(userSession.getUname())
                            );


                            return true;
                        } else {
                            return false;
                        }
                    }
                }

                /*paperCorrect( answerCardId, 0);*/
            } catch (Exception e) {
                log.info("批改试卷服务异常");
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 单题提交（新增人工批改）
     *
     * @param answerList
     * @param answerCardId
     * @param paperCommitVO
     * @param userSession
     * @param essayGoodsFree
     * @param type
     * @param terminal
     * @return
     */
    private Integer singleQuestionCommitV2(List<PaperCommitAnswerVO> answerList, Long answerCardId, PaperCommitVO paperCommitVO, UserSession userSession, String essayGoodsFree, int type, int terminal) {
        Integer returnQuestionType = null;
        //操作类型(0 保存  1交卷)
        int saveType = paperCommitVO.getSaveType();
        int spendTime = paperCommitVO.getSpendTime();
        long goodsOrderDetailId = 0L;
        //根据答题卡id查询答题卡信息
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerCardId);
        if (null == questionAnswer) {
            log.warn("答题卡ID 有误 answerCardId：{}", answerCardId);
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (null != questionAnswer.getAnswerCardType() && questionAnswer.getAnswerCardType().intValue() == 2) {
            essayGoodsFree = ESSAY_GOODS_FREE + "";
        }
        //不使用客户端传递的correctMode,因为智能转人工时,他们correctMode传递的是2
        paperCommitVO.setCorrectMode(questionAnswer.getCorrectMode());
        returnQuestionType = questionAnswer.getQuestionType();
        if (questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            return returnQuestionType;
        }
        long questionBaseId = questionAnswer.getQuestionBaseId();
        long questionDetailId = questionAnswer.getQuestionDetailId();
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findById(questionDetailId);
        if (null == questionBase || null == essayQuestionDetail) {
            log.warn("试题id有误 questionBaseId:{},questionDetailId:{}", questionBaseId, questionDetailId);
            throw new BizException(EssayErrors.ESSAY_QUESTION_BASE_ID_NULL);
        }
        int questionType = essayQuestionDetail.getType();
        if (CollectionUtils.isNotEmpty(answerList) && null != answerList.get(0)) {
            questionAnswer.setContent(answerList.get(0).getContent());//学员答案
            questionAnswer.setInputWordNum(answerList.get(0).getInputWordNum());
            if (StringUtils.isNotEmpty(answerList.get(0).getFileName())) {
                questionAnswer.setFileName(answerList.get(0).getFileName());
            }

        }
        questionAnswer.setCorrectType(essayQuestionDetail.getCorrectType());
        questionAnswer.setSpendTime(spendTime);//答题时间
        questionAnswer.setSpeed(spendTime);//答题速度
        questionAnswer.setCorrectMode(paperCommitVO.getCorrectMode());//批改类型
        EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(questionType, questionAnswer.getCorrectMode());
        if (AnswerSaveTypeConstant.SAVE == saveType) {
            if (questionAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus()) {
                questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            }
        } else if (AnswerSaveTypeConstant.COMMIT == saveType) {
            //去掉 校验用户该题批改了几次
            //bizStatus改为 ：已经提交
            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
            //当前批改不是免费
            if (ESSAY_GOODS_NOT_FREE == Integer.parseInt(essayGoodsFree)) {
                goodsOrderDetailId = UserOrderUtil.reduceUserCorrectTimes(goodsTypeEnum, userSession.getId(), essayUserCorrectGoodsRepository, userCorrectGoodsServiceV4, questionAnswer.getQuestionBaseId());
            }
        } else if (AnswerSaveTypeConstant.UPDATE_TIME == saveType) {
            questionAnswer.setSpendTime(spendTime);
        } else {
            log.info("答题卡保存类型有误 saveType ：{}:" + saveType);
            throw new BizException(EssayErrors.ANSWER_CARD_SAVE_TYPE_ERROR);
        }
        questionAnswer.setGmtModify(new Date());
        questionAnswer.setSubmitTime(new Date());
        log.info("单题答题卡:{},保存批改类型:{}", questionAnswer.getId(), questionAnswer.getCorrectMode());
        essayQuestionAnswerRepository.save(questionAnswer);
        long questionAnswerId = questionAnswer.getId();

        questionAnswer = new EssayQuestionAnswer();
        questionAnswer.setId(questionAnswerId);
        //如果是提交并且是智能批改
        if (AnswerSaveTypeConstant.COMMIT == saveType) {
            if (CorrectModeEnum.INTELLIGENCE.getMode() == paperCommitVO.getCorrectMode()) {
                //调用批改试题接口
                try {
                    Map params = new HashMap<>();
                    params.put("answerCardId", answerCardId);
                    params.put("type", SINGLE_QUESTION);
                    HttpEntity<Map> request = new HttpEntity<>(params, null); //组装
                    log.info("批改单题:post 请求发送");
                    String url = answerCorrectUrlV2 + "?answerCardId=" + answerCardId + "&type=" + SINGLE_QUESTION;
                    long start = System.currentTimeMillis();
                    ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);


                    if (null != body) {
                        long end = System.currentTimeMillis();
                        log.info("批改单题:post 请求发送成功");
                        log.info("请求用时：" + (end - start) + "毫秒");
                        if (1000000 == body.getCode()) {
                            if (null != body.getData() && (Boolean) body.getData()) {
                                /**
                                 * 神策埋点上报交卷成功
                                 */
                                essayCommitSucceedAnalytics(userSession.getUcId(), terminal, questionBaseId, "", questionType == 5 ? ARGUMENTATION : SINGLE_QUESTION);
                                log.info("批改单题增加图币,questionType:{}", questionType);
                                //单题增加金币
                                eventPublisher.publishEvent(RewardActionEvent.class,
                                        this,
                                        (event) -> event.setAction(
                                                (questionType == 5) ? RewardAction.ActionType.SL_CORR_ARGUMENT : RewardAction.ActionType.SL_CORR_STANDARD)
                                                .setUid(userSession.getId())
                                                .setUname(userSession.getUname())
                                );
                            }
                        }
                    }

                } catch (Exception e) {
                    log.info("批改试卷服务异常");
                    e.printStackTrace();
                }
            } else {
                // 人工批改
                CorrectOrder oldCorrectOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(answerCardId, type, EssayStatusEnum.NORMAL.getCode());
                int correctOrderType = questionTypeService.convertQuestionTypeToQuestionLabelType(questionType);
                int delayStatus = paperCommitVO.getDelayStatus() == DelayStatusEnum.NO.getCode() ? DelayStatusEnum.NO.getCode()
                        : paperCommitVO.getDelayStatus();
                CorrectOrder correctOrder = CorrectOrder.builder().answerCardId(answerCardId)
                        .answerCardType(QuestionTypeConstant.SINGLE_QUESTION).type(correctOrderType)
                        .correctMode(CorrectModeEnum.MANUAL.getMode()).delayStatus(delayStatus)
                        .userId(userSession.getId())
                        .userName(userSession.getUname())
                        .userPhoneNum(userSession.getMobile())
                        .exercisesType(paperCommitVO.getExercisesType())
                        .gmtDeadLine(correctOrderService.calculateDeadLine(correctOrderType, delayStatus)).build();
                if (null != oldCorrectOrder) {
                    correctOrder.setId(oldCorrectOrder.getId());
                    correctOrder.setGmtCreate(new Date());
                    correctOrder.setGmtModify(new Date()); 
                    correctOrder.setDelayStatus(delayStatus);
                    correctOrder.setGmtDeadLine(correctOrderService.calculateDeadLine(correctOrderType, delayStatus));
                }
                correctOrder.setGoodsOrderDetailId(goodsOrderDetailId);
                correctOrder.setStatus(YesNoEnum.YES.getValue());
                if (correctOrder.getUserId() == 0) {
                    throw new BizException(EssayErrors.ANSWER_USERINFO_ILLEGAL);
                }
                correctOrderService.createOrder(correctOrder);
            }
        }
        return returnQuestionType;
    }


    @Override
    public Object paperCommit(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, String cv, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int userId = userSession.getId();
        //替换答案中的加号
        paperCommitVO.getAnswerList().forEach(answer -> {
            String content = answer.getContent();
            if (content != null) {
                answer.setContent(content.replaceAll("\\+", "＋"));
            }
        });
        //批改是否免费
        String essayGoodsFree = String.valueOf(redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY));
        int type = paperCommitVO.getType();//答题类型 0单题   1套题
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();//答案列表
        Long answerCardId = paperCommitVO.getAnswerCardId();//答题卡id

        if (null == answerCardId) {
            log.warn("缺少答题卡ID 参数 {}", answerCardId);
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        List<EssayQuestionVO> essayQuestionVOS = new ArrayList<EssayQuestionVO>();
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            singleQuestionCommit(answerList, answerCardId, paperCommitVO, userSession, essayGoodsFree, type, terminal);
            essayQuestionVOS = answerDetail(userId, type, answerCardId, terminal, cv);
        } else {
            correctPaper(userSession, paperCommitVO, terminal,modeTypeEnum);
            essayQuestionVOS = answerDetail(userId, type, answerCardId, terminal, cv);
        }

        return essayQuestionVOS;
    }

//    private List<EssayQuestionVO> queryAnswerDetail(long answerCardId, int type, int userId) {
//        int bizStatus = 0;
//        if (QuestionTypeConstant.SINGLE_QUESTION == type) {
//            EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findCorrect(answerCardId);
//            bizStatus = questionAnswer.getBizStatus();
//        } else {
//            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerCardId);
//            bizStatus = paperAnswer.getBizStatus();
//        }
//        List<EssayQuestionVO> essayQuestionVOS = new ArrayList<EssayQuestionVO>();
//
//        essayQuestionVOS = answerDetail(userId, type, answerCardId);
//
//        return essayQuestionVOS;
//    }

    /**
     * 套题的保存&&交卷
     *  @param userSession
     * @param paperCommitVO
     * @param terminal
     * @param modeTypeEnum
     */
    @Override
    public Boolean correctPaper(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int userId = userSession.getId();
        Integer examType = paperCommitVO.getExamType();
        if (null == examType) {
            examType = AdminPaperConstant.TRUE_PAPER;
        }
        //批改是否免费
        String essayGoodsFree = String.valueOf(redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY));
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(paperCommitVO.getAnswerCardId());
        if (null == paperAnswer) {
            log.warn("答题卡ID 有误 {}", paperCommitVO.getAnswerCardId());
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (null != paperAnswer.getAnswerCardType() && paperAnswer.getAnswerCardType().intValue() == 2) {
            essayGoodsFree = ESSAY_GOODS_FREE + "";
        }
        if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
//            log.warn("请求参数错误,试卷已提交或已批改");
//            throw new BizException(EssayErrors.ANSWER_CARD_COMMIT);
            return true;
        }
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();//答案列表
        Long answerCardId = paperCommitVO.getAnswerCardId();//答题卡id
        int saveType = paperCommitVO.getSaveType();//操作类型(0 保存  1交卷)
        Integer spendTime = paperCommitVO.getSpendTime();//花费时间


        //校验试卷状态
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
        if (null == paperBase) {
            log.warn("试卷id有误 {}", paperBase);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        } else {
//            if (EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()
//                    || EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus() != paperBase.getStatus()) {
//                log.warn("试卷已下线或已删除，交卷失败", paperAnswer.getPaperBaseId());
//                throw new BizException(EssayErrors.ESSAY_PAPER_OFFLINE);
//            }
        }

        if (null != paperCommitVO.getUnfinishedCount()) {
            paperAnswer.setUnfinishedCount(paperCommitVO.getUnfinishedCount());
        }
        if (null != paperCommitVO.getLastIndex()) {
            paperAnswer.setLastIndex(paperCommitVO.getLastIndex());
        }

        paperAnswer.setSpendTime(spendTime);
        LinkedList<EssayQuestionAnswer> essayQuestionAnswers = new LinkedList<>();

        int answeredQuestionCount = 0;//已作答题目数
        if (CollectionUtils.isNotEmpty(answerList)) {

            for (PaperCommitAnswerVO commitAnswerVO : answerList) {
                EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(commitAnswerVO.getQuestionBaseId());
                EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(commitAnswerVO.getQuestionDetailId());

                EssayQuestionAnswer questionAnswer = EssayQuestionAnswer.builder()
                        .areaName(paperAnswer.getAreaName())//地区名称
                        .areaId(paperAnswer.getAreaId())//地区id
                        .questionYear(questionBase.getQuestionYear())//试题年份
                        .score(questionDetail.getScore())//试题分数
                        .content(commitAnswerVO.getContent())//学员作答答案
                        .userId(userId)//用户id
                        .questionBaseId(commitAnswerVO.getQuestionBaseId())//题目baseId
                        .questionDetailId(commitAnswerVO.getQuestionDetailId())//题目detailId
                        .terminal(terminal)//答题终端
                        .paperId(paperAnswer.getPaperBaseId())//试卷baseId
                        .paperAnswerId(answerCardId)//试卷答题卡id
                        .spendTime(commitAnswerVO.getSpendTime())
                        .questionType(questionDetail.getType())
                        .inputWordNum(commitAnswerVO.getInputWordNum())
                        .labelStatus(0)
                        .subScore(-1D)
                        .correctType(questionDetail.getCorrectType())
                        .subScoreRatio(0D)
                        .build();

                if (StringUtils.isNotEmpty(commitAnswerVO.getFileName())) {
                    questionAnswer.setFileName(commitAnswerVO.getFileName());
                }
                questionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
                if (0 != commitAnswerVO.getSpendTime()) {
                    answeredQuestionCount += 1;
                }
                if (null != commitAnswerVO.getAnswerId()) {
                    questionAnswer.setId(commitAnswerVO.getAnswerId());//试题答题卡id
                }
                if (AnswerSaveTypeConstant.SAVE == saveType) {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                } else {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                }
                essayQuestionAnswers.add(questionAnswer);
            }
        }
        if (AnswerSaveTypeConstant.SAVE == saveType) {
            paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
        } else if (AnswerSaveTypeConstant.COMMIT == saveType) {
            boolean NOT_CORRECT_FREE_USER = true;
            //校验用户该试卷批改次数
            ResponseVO responseVO = correctCount(userId, QuestionTypeConstant.PAPER, paperAnswer.getPaperBaseId(), modeTypeEnum);
            if (CANNOT_CORRECT == responseVO.getCanCorrect()) {
                log.warn("已批改" + maxCorrectTime + "次，无法交卷。 paperBaseId {}" + paperAnswer.getPaperBaseId());
                throw new BizException(ErrorResult.create(1000509, "已批改" + maxCorrectTime + "次，无法交卷"));
            }
            paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
            // 修改批改次数 (模考交卷 不扣批改次数)
            // 当前批改不是免费，并且用户不在白名单里才扣除批改次数(模考交卷 不扣批改次数) 0206
            //查询白名单用户
            List<Integer> freeUserIdList = essayCorrectFreeUserRepository.findUserIdByStatusAndBizStatus
                    (EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
            if (freeUserIdList.contains(userId)) {
                NOT_CORRECT_FREE_USER = false;
            }

            if (ESSAY_GOODS_NOT_FREE == Integer.parseInt(essayGoodsFree) && AdminPaperConstant.MOCK_PAPER != examType && NOT_CORRECT_FREE_USER) {
                int i = essayUserCorrectGoodsRepository.modifyUsefulNumByUserIdAndType(1, userId, 1);
                if (1 != i) {
                    log.info("批改次数不足");
                    throw new BizException(EssayErrors.LOW_CORRECT_TIMES);
                }
            }
        } else if (AnswerSaveTypeConstant.UPDATE_TIME == saveType) {
            paperAnswer.setSpendTime(spendTime);
        } else {
            log.info("答题卡保存类型有误 saveType {}:" + saveType);
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        int speed = 0;
        if (0 != answeredQuestionCount) {
            speed = spendTime / answeredQuestionCount;
        }

        //保存试卷&&试题 答题卡信息
        for (EssayQuestionAnswer questionAnswer : essayQuestionAnswers) {
            questionAnswer.setSpeed(speed);
            essayQuestionAnswerRepository.save(questionAnswer);
            long questionAnswerId = questionAnswer.getId();
            questionAnswer = new EssayQuestionAnswer();
            questionAnswer.setId(questionAnswerId);
        }
        paperAnswer.setSpeed(speed);
        essayPaperAnswerRepository.saveAndFlush(paperAnswer);
        long paperAnswerId = paperAnswer.getId();
        paperAnswer = new EssayPaperAnswer();
        paperAnswer.setId(paperAnswerId);
        if (AnswerSaveTypeConstant.COMMIT == saveType && AdminPaperConstant.MOCK_PAPER != examType) {
            try {
//               /* MQ方式请求
                //调用批卷接口(模考交卷 在 持久化之前)
                /*  paperCorrect( answerCardId, 1);*/
                String url = answerCorrectUrl + "?answerCardId=" + answerCardId + "&type=" + QuestionTypeConstant.PAPER;
                long start = System.currentTimeMillis();
                ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);
                if (null != body) {
                    long end = System.currentTimeMillis();
                    log.info("请求用时：" + (end - start) + "毫秒");
                    if (1000000 == body.getCode()) {
                        if (null != body.getData()) {
                            /**
                             * 神策埋点上报交卷成功
                             */
                            essayCommitSucceedAnalytics(userSession.getUcId(), terminal, paperBase.getId(), paperBase.getName(), PAPER);
                            //  log.info("批改套题增加金币");
                            //套题增加金币
                            eventPublisher.publishEvent(RewardActionEvent.class,
                                    this,
                                    (event) -> event.setAction(RewardAction.ActionType.SL_CORR_SET)
                                            .setUid(userSession.getId())
                                            .setUname(userSession.getUname())
                            );
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批改试卷服务异常 {}", e.getMessage());
                e.printStackTrace();
            }

        }
        return true;
    }

    @Override
    public Boolean correctPaperV2(UserSession userSession, PaperCommitVO paperCommitVO, int terminal) {
        int userId = userSession.getId();
        Integer examType = paperCommitVO.getExamType();
        long goodsOrderDetailId = 0L;
        if (null == examType) {
            examType = AdminPaperConstant.TRUE_PAPER;
        }
        //批改是否免费
        String essayGoodsFree = String.valueOf(redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY));
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(paperCommitVO.getAnswerCardId());
        if (null == paperAnswer) {
            log.warn("答题卡ID 有误 {}", paperCommitVO.getAnswerCardId());
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (null != paperAnswer.getAnswerCardType() && paperAnswer.getAnswerCardType().intValue() == 2) {
            essayGoodsFree = ESSAY_GOODS_FREE + "";
        }
        //不使用客户端传递的correctMode,因为智能转人工时,他们correctMode传递的是2
        paperCommitVO.setCorrectMode(paperAnswer.getCorrectMode());
        if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            return true;
        }
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();//答案列表
        Long answerCardId = paperCommitVO.getAnswerCardId();//答题卡id
        int type = paperCommitVO.getType(); //单题 套题 类型
        int saveType = paperCommitVO.getSaveType();//操作类型(0 保存  1交卷)
        Integer spendTime = paperCommitVO.getSpendTime();//花费时间


        //校验试卷状态
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
        if (null == paperBase) {
            log.warn("试卷id有误 {}", paperBase);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }

        if (null != paperCommitVO.getUnfinishedCount()) {
            paperAnswer.setUnfinishedCount(paperCommitVO.getUnfinishedCount());
        }
        if (null != paperCommitVO.getLastIndex()) {
            paperAnswer.setLastIndex(paperCommitVO.getLastIndex());
        }

        paperAnswer.setSpendTime(spendTime);
        LinkedList<EssayQuestionAnswer> essayQuestionAnswers = new LinkedList<>();

        int answeredQuestionCount = 0;//已作答题目数
        if (CollectionUtils.isNotEmpty(answerList)) {

            for (PaperCommitAnswerVO commitAnswerVO : answerList) {
                EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(commitAnswerVO.getQuestionBaseId());
                EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(commitAnswerVO.getQuestionDetailId());

                EssayQuestionAnswer questionAnswer = EssayQuestionAnswer.builder()
                        .areaName(paperAnswer.getAreaName())//地区名称
                        .areaId(paperAnswer.getAreaId())//地区id
                        .questionYear(questionBase.getQuestionYear())//试题年份
                        .score(questionDetail.getScore())//试题分数
                        .content(commitAnswerVO.getContent())//学员作答答案
                        .userId(userId)//用户id
                        .questionBaseId(commitAnswerVO.getQuestionBaseId())//题目baseId
                        .questionDetailId(commitAnswerVO.getQuestionDetailId())//题目detailId
                        .terminal(terminal)//答题终端
                        .paperId(paperAnswer.getPaperBaseId())//试卷baseId
                        .paperAnswerId(answerCardId)//试卷答题卡id
                        .spendTime(commitAnswerVO.getSpendTime())
                        .questionType(questionDetail.getType())
                        .inputWordNum(commitAnswerVO.getInputWordNum())
                        .labelStatus(0)
                        .subScore(-1D)
                        .correctType(questionDetail.getCorrectType())
                        .subScoreRatio(0D)
                        .correctMode(paperCommitVO.getCorrectMode())
                        .answerCardType(paperAnswer.getAnswerCardType())
                        .build();

                if (StringUtils.isNotEmpty(commitAnswerVO.getFileName())) {
                    questionAnswer.setFileName(commitAnswerVO.getFileName());
                }
                questionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
                if (0 != commitAnswerVO.getSpendTime()) {
                    answeredQuestionCount += 1;
                }
                if (null != commitAnswerVO.getAnswerId()) {
                    questionAnswer.setId(commitAnswerVO.getAnswerId());//试题答题卡id
                }
                if (AnswerSaveTypeConstant.SAVE == saveType) {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                } else {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                    questionAnswer.setSubmitTime(new Date());
                }
                essayQuestionAnswers.add(questionAnswer);
            }
        }
        //如果答题卡状态不是被退回并且是保存答题卡
        if (AnswerSaveTypeConstant.SAVE == saveType) {
            if (paperAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus()) {
                paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            }
            //最后编辑的答题卡时间倒排
            paperAnswer.setGmtModify(new Date());
        } else if (AnswerSaveTypeConstant.COMMIT == saveType) {
            paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
            paperAnswer.setSubmitTime(new Date());
            // 修改批改次数 (模考交卷 不扣批改次数)
            // 当前批改不是免费(模考交卷 不扣批改次数) 0206

            if (ESSAY_GOODS_NOT_FREE == Integer.parseInt(essayGoodsFree) && AdminPaperConstant.MOCK_PAPER != examType) {
                //获取汇总表数据
                EssayCorrectGoodsConstant.GoodsTypeEnum goodsType = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(0, paperAnswer.getCorrectMode());
                goodsOrderDetailId = UserOrderUtil.reduceUserCorrectTimes(goodsType, userId, essayUserCorrectGoodsRepository, userCorrectGoodsServiceV4, paperAnswer.getPaperBaseId());
            }
        } else if (AnswerSaveTypeConstant.UPDATE_TIME == saveType) {
            paperAnswer.setSpendTime(spendTime);
        } else {
            log.info("答题卡保存类型有误 saveType {}:" + saveType);
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        int speed = 0;
        if (0 != answeredQuestionCount) {
            speed = spendTime / answeredQuestionCount;
        }

        //保存试卷&&试题 答题卡信息
        for (EssayQuestionAnswer questionAnswer : essayQuestionAnswers) {
            questionAnswer.setSpeed(speed);
            essayQuestionAnswerRepository.save(questionAnswer);
            long questionAnswerId = questionAnswer.getId();
            questionAnswer = new EssayQuestionAnswer();
            questionAnswer.setId(questionAnswerId);
        }
        paperAnswer.setSpeed(speed);
        log.info("试卷答题卡id :{},批改类型是:{}", paperAnswer.getId(), paperAnswer.getCorrectMode());
        essayPaperAnswerRepository.saveAndFlush(paperAnswer);
        long paperAnswerId = paperAnswer.getId();
        paperAnswer = new EssayPaperAnswer();
        paperAnswer.setId(paperAnswerId);
        if (AnswerSaveTypeConstant.COMMIT == saveType && AdminPaperConstant.MOCK_PAPER != examType) {
            if (CorrectModeEnum.INTELLIGENCE.getMode() == paperCommitVO.getCorrectMode()) {
                //智能批改
                try {
                    String url = answerCorrectUrlV2 + "?answerCardId=" + answerCardId + "&type=" + QuestionTypeConstant.PAPER;
                    long start = System.currentTimeMillis();
                    ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);
                    if (null != body) {
                        long end = System.currentTimeMillis();
                        log.info("请求用时：" + (end - start) + "毫秒");
                        if (1000000 == body.getCode()) {
                            if (null != body.getData()) {
                                /**
                                 * 神策埋点上报交卷成功
                                 */
                                essayCommitSucceedAnalytics(userSession.getUcId(), terminal, paperBase.getId(), paperBase.getName(), PAPER);
                                //套题增加金币
                                eventPublisher.publishEvent(RewardActionEvent.class,
                                        this,
                                        (event) -> event.setAction(RewardAction.ActionType.SL_CORR_SET)
                                                .setUid(userSession.getId())
                                                .setUname(userSession.getUname())
                                );
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("批改试卷服务异常 {}", e.getMessage());
                    e.printStackTrace();
                }
            } else {
                int delayStatus = paperCommitVO.getDelayStatus() == DelayStatusEnum.NO.getCode() ? DelayStatusEnum.NO.getCode()
                        : paperCommitVO.getDelayStatus();
                CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(answerCardId, paperCommitVO.getType(), EssayStatusEnum.NORMAL.getCode());
                if (null == correctOrder) {
                    correctOrder = CorrectOrder.builder().answerCardId(answerCardId)
                            .answerCardType(QuestionTypeConstant.PAPER).type(TeacherOrderTypeEnum.SET_QUESTION.getValue())
                            .correctMode(CorrectModeEnum.MANUAL.getMode()).delayStatus(delayStatus)
                            .userId(userSession.getId())
                            .userName(userSession.getUname())
                            .userPhoneNum(userSession.getMobile())
                            .gmtDeadLine(correctOrderService.calculateDeadLine(TeacherOrderTypeEnum.SET_QUESTION.getValue(),
                                    delayStatus))
                            .exercisesType(paperCommitVO.getExercisesType())
                            .build();
                } else {
                    correctOrder.setCorrectMemo(null);
                    correctOrder.setDelayStatus(delayStatus);
                    correctOrder.setGmtCreate(new Date());
                    correctOrder.setGmtModify(new Date());
                    correctOrder.setGmtDeadLine(correctOrderService.calculateDeadLine(TeacherOrderTypeEnum.SET_QUESTION.getValue(),
                            delayStatus));
                }
                correctOrder.setGoodsOrderDetailId(goodsOrderDetailId);
                correctOrder.setStatus(YesNoEnum.YES.getValue());
                correctOrderService.createOrder(correctOrder);

            }

        }
        return true;
    }


//    public void paperCorrect(long answerCardId, int qType) {
//
//        log.info("=====进入批改试卷接口，发送消息到消息队列:" + answerCardId + "&" + qType + "=========");
//        //发送消息到消息队列
//        rabbitTemplate.convertAndSend(SystemConstant.ANSWER_CORRECT_ROUTING_KEY, answerCardId + "and" + qType);
//         /*   */
////        String url = SystemConstant.ANSWER_CORRECT_IP+"/cr/intelligent/correct?";
////        String param = "answerCardId="+answerCardId+"&qtype="+qType;
////        log.info("发送put请求，url = {}",url+param);
////        restTemplate.put(url+param,null);
////        log.info("put 请求发送成功");
//
//
//    }


    /**
     * 申论交卷成功神策埋点
     *
     * @param ucId
     * @param terminal
     * @param id
     * @param name
     * @param type     (答题卡类型 0单题 1套题 2议论文)
     */
    @Async
    public void essayCommitSucceedAnalytics(String ucId, int terminal, long id, String name, int type) {

        try {
            log.info("essayCommitSucceedAnalytics -----------");
            Map<String, Object> properties = Maps.newHashMap();
            switch (type) {
                case 0: {
                    properties.put("on_module", "单题组");
                    break;
                }
                case 1: {
                    properties.put("on_module", "套题");
                    break;
                }
                case 2: {
                    properties.put("on_module", "文章写作");
//                    properties.put("on_module", "议论文");
                    break;
                }
            }

            properties.put("exam_title", name);
            properties.put("exam_id", id + "");
            properties.put("platform", SensorsUtils.getPlatform(terminal));
            log.info("essayCommitSucceedAnalytics ucId :{},properties：{}", ucId, properties.toString());
            sensorsAnalytics.track(ucId, true, SensorsEventEnum.ESSAY_COMMIT_SUCCEED.getCode(), properties);
            sensorsAnalytics.flush();
        } catch (InvalidArgumentException e) {
            log.error("sa track error:" + e);
        }
    }


    @Override
    public List<EssayQuestionVO> answerDetail(int userId, int type, long answerId, int terminal, String cv) {
        LinkedList<EssayQuestionVO> essayQuestionVOList = new LinkedList<>();

        //判断是单题还是套题
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
//            EssayQuestionVO vo = answerDetail(answerId);
            EssayQuestionVO vo = answerDetailV1(answerId);
            essayQuestionVOList.add(vo);
        } else {
            //根据试卷答题卡查询试题答题卡
            String userPaperAnswerKey = RedisKeyConstant.getUserPaperAnswerKey(answerId);
            essayQuestionVOList = (LinkedList<EssayQuestionVO>) redisTemplate.opsForValue().get(userPaperAnswerKey);
            if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                return essayQuestionVOList;
            }
            essayQuestionVOList = new LinkedList<>();
            entityManager.clear();
            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerId);

            if (null == paperAnswer) {
                log.warn("答题卡id错误");
                throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
            }
            List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                    (answerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));

            if (CollectionUtils.isNotEmpty(answers)) {
                for (EssayQuestionAnswer answer : answers) {
                    EssayQuestionVO vo = answerDetailV1(answer.getId());
                    vo.setTotalExamScore(paperAnswer.getExamScore());
                    vo.setTotalSpendTime(paperAnswer.getSpendTime());
                    essayQuestionVOList.add(vo);
                }
            }
            if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()
                    && CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                redisTemplate.opsForValue().set(userPaperAnswerKey, essayQuestionVOList, 5, TimeUnit.MINUTES);
            }
        }

        return essayQuestionVOList;
    }


    //字段替换标签后是否为空
    private static boolean replaceLabel(String sourceStr) {
        String regEx1 = "<br>";//识别所有<br><br/>的标签
        String regEx2 = "<br/>";
        String regEx3 = "<p>";
        String regEx4 = "&nbsp;";
        String regEx5 = "</p>";
        if (StringUtils.isNotEmpty(sourceStr)) {
            sourceStr = sourceStr.replaceAll(regEx1, "")
                    .replaceAll(regEx2, "")
                    .replaceAll(regEx3, "")
                    .replaceAll(regEx4, "")
                    .replaceAll(regEx5, "");
            return StringUtils.isEmpty(sourceStr);
        } else {
            return true;
        }

    }

    private EssayQuestionVO answerDetail(long answerId) {
        //根据答题卡id查询答题卡信息
        entityManager.clear();
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
        EssayQuestionVO vo = null;
        if (null != questionAnswer) {
            //查询对应的base信息（取时限）
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
            //根据detailId查询题目详情
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());
            double difficultGrade = questionDetail.getDifficultGrade();
            String difficultGradeStr = "";
            switch ((int) difficultGrade) {
                case 0:
                    break;
                case 1:
                    difficultGradeStr = "难度：较小";
                    break;
                case 2:
                    difficultGradeStr = "难度：适中";
                    break;
                case 3:
                    difficultGradeStr = "难度：较大";
                    break;
            }


            vo = EssayQuestionVO.builder()
                    .type(questionDetail.getType())
                    .questionDetailId(questionBase.getDetailId())//试题的detailId
                    .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明11111
                    .stem(questionDetail.getStem())//题干信息
                    .score(questionDetail.getScore())//题目分数
                    .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                    .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                    .limitTime(questionBase.getLimitTime())//答题限时
                    .questionBaseId(questionAnswer.getQuestionBaseId())//题目的baseId
                    .examScore(questionAnswer.getExamScore())//学员得分
                    .spendTime(questionAnswer.getSpendTime())//答题时间
                    .inputWordNum(questionAnswer.getInputWordNum())//答案字数
                    .correctedContent(StringUtils.isNotEmpty(questionAnswer.getCorrectedContent()) ? questionAnswer.getCorrectedContent() : questionAnswer.getContent())//批改后的答案
                    .answerDetails(questionDetail.getAnswerDetails())
                    .answerRange(questionDetail.getAnswerRange())
                    .answerTask(questionDetail.getAnswerTask())
                    .correctRule(questionDetail.getCorrectRule())//规则描述
                    .authorityReviews(questionDetail.getAuthorityReviews())//权威点评
                    .analyzeQuestion(questionDetail.getAnalyzeQuestion())//试题分析
                    .sort(questionBase.getSort())//题目序号
                    .bizStatus(questionAnswer.getBizStatus())//答题状态（0 空白，未开始 1未完成 2 已交卷 3已批改）
                    .answerComment(questionDetail.getAnswerComment())//参考答案
                    .topic(questionDetail.getTopic())
                    .callName(questionDetail.getCallName())
                    .subTopic(questionDetail.getSubTopic())
                    /**
                     * 临时替换落款日期和落款人字段（解决客户端展示问题）
                     */
                    .inscribedDate(questionDetail.getInscribedName())
                    .inscribedName(questionDetail.getInscribedDate())
                    .difficultGrade(difficultGradeStr)
                    .build();
            if (replaceLabel(questionDetail.getAnswerComment())) {
                vo.setAnswerComment(null);
            }

            if (replaceLabel(questionDetail.getAnswerRequire())) {
                vo.setAnswerRequire(null);
            }

            if (replaceLabel(questionDetail.getCorrectRule())) {
                vo.setCorrectRule(null);
            }

            if (replaceLabel(questionDetail.getAuthorityReviews())) {
                vo.setAuthorityReviews(null);
            }

            if (replaceLabel(questionDetail.getAnalyzeQuestion())) {
                vo.setAnalyzeQuestion(null);
            }

            if (replaceLabel(questionDetail.getAnswerDetails())) {
                vo.setAnswerDetails(null);
            }
            if (replaceLabel(questionDetail.getAnswerRange())) {
                vo.setAnswerRange(null);
            }
            if (replaceLabel(questionDetail.getAnswerTask())) {
                vo.setAnswerTask(null);
            }

            //查询得分点
            LinkedList<ScoreVO> addScoreList = new LinkedList<>();
            LinkedList<ScoreVO> subScoreList = new LinkedList<>();
            LinkedList<ScoreVO> elseScoreList = new LinkedList<>();
            List<EssayUserAnswerQuestionDetailedScore> detailedScores = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatusOrderBySequenceNumberAsc(answerId, 1);
            if (CollectionUtils.isNotEmpty(detailedScores)) {
                //1为内容得分，2为格式得分，3为减分
                for (EssayUserAnswerQuestionDetailedScore detailedScore : detailedScores) {
                    ScoreVO scoreVO = new ScoreVO();
                    BeanUtils.copyProperties(detailedScore, scoreVO);
                    if (1 == detailedScore.getType()) {
                        addScoreList.add(scoreVO);
                    } else if (3 == detailedScore.getType()) {
                        subScoreList.add(scoreVO);
                    } else {
                        elseScoreList.add(scoreVO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(elseScoreList)) {
                addScoreList.addAll(elseScoreList);
            }
            vo.setAddScoreList(addScoreList);
            vo.setSubScoreList(subScoreList);


        }
        return vo;

    }

    /**
     * 校验用户单题/试卷批改次数
     *
     * @param userId
     * @param type
     * @param baseId
     * @param modeTypeEnum
     */
    @Override
    public ResponseVO correctCount(int userId, int type, long baseId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        ResponseVO responseVO = new ResponseVO();

        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        //判断是单题还是套题，是否超过最大批改次数

        if (maxCorrectTime == 9999) {
            responseVO.setCanCorrect(CAN_CORRECT);
        } else {
            if (type == SINGLE_QUESTION) {

                int correctCount = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType
                        (userId, baseId, 0, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                modeTypeEnum.getType());

                if (correctCount >= maxCorrectTime) {
                    responseVO.setCanCorrect(CANNOT_CORRECT);
                } else {
                    responseVO.setCanCorrect(CAN_CORRECT);
                }
            } //议论文单独
            else if (type == ARGUMENTATION) {

                long correctCount = essayQuestionAnswerRepository.countByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusInAndAnswerCardType
                        (userId, 0L, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList,
                                modeTypeEnum.getType());

                if (correctCount >= maxCorrectTime) {
                    responseVO.setCanCorrect(CANNOT_CORRECT);
                } else {
                    responseVO.setCanCorrect(CAN_CORRECT);
                }
            } else {
                int correctCount = essayPaperAnswerRepository.countByUserIdAndPaperBaseIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc
                        (userId,
                                baseId,
                                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                modeTypeEnum.getType());

                if (correctCount >= maxCorrectTime) {
                    responseVO.setCanCorrect(CANNOT_CORRECT);
                } else {
                    responseVO.setCanCorrect(CAN_CORRECT);
                }
            }
        }

        //最大批改次数
        responseVO.setMaxCorrectTimes(maxCorrectTime);
        //是否存在剩余批改次数
        int exist = 1;
        List<EssayUserCorrectGoods> list = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId, UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(), UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(), type);
        if (CollectionUtils.isNotEmpty(list) && list.get(0) != null) {
            exist = 0 < list.get(0).getUsefulNum() ? 0 : 1;
        } else {
            exist = 1;
        }
        List<EssayCorrectFreeUser> freeUsers = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        if (CollectionUtils.isNotEmpty(freeUsers)) {
            exist = 0;
        }
        responseVO.setExist(exist);
        return responseVO;

    }

    @Override
    public ResponseVO free() {
        String essayGoodsFree = String.valueOf(redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY));
        log.info(essayGoodsFree);
        ResponseVO responseVO = ResponseVO.builder().essayGoodsFree(essayGoodsFree).build();
        return responseVO;
    }


    @Cacheable(value = "singleCorrectListCopies", sync = true)
    public List<EssayAnswerVO> singleCorrectListCopies(int userId) {
        /*  guava 机器内存 缓存 数据 */
        log.info(">>>>>>>>从缓存中获取试题对应的correct列表<<<<<<<<");
        List<EssayAnswerVO> correctList = singleCorrectListCache.getIfPresent(userId);
        if (CollectionUtils.isEmpty(correctList)) {
            correctList = new LinkedList<EssayAnswerVO>();
            log.info(">>>>>>>>缓存获取数据失败<<<<<<<<");
            //查询批改记录
            singleCorrectListCache.put(userId, correctList);
        }
        return correctList;
    }


    @Override
    public YoutuVO photo(MultipartFile file, int type, int userId, int terminal) throws Exception {

        YoutuVO vo = photoDistinguish(file, type);
        try {
            //异步方法，持久化拍照答题相关信息
            asyncFileSaveService.savePhotoAndAnswerToMysql(file, vo.getContent(), userId, terminal);

        } catch (Exception e) {
            log.info("保存拍照答题图片出错");
            e.printStackTrace();
        }

        return vo;
    }


    private YoutuVO photoDistinguish(MultipartFile file, int type) {
        try {
            //处理请求参数

            YoutuVO vo = YoutuVO.builder()
                    .content("")
                    .type(type)
                    .build();

            byte[] data = null;
            if (null == file) {
                log.warn("图片读取异常");
                throw new BizException(EssayErrors.FILE_READ_ERROR);
            }
            InputStream inputStream = file.getInputStream();
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            BASE64Encoder encoder = new BASE64Encoder();

            //拼接youtu请求参数
            String fileStr = encoder.encode(data);
            long length = 0L;

            Map<String, String> hashMap = new LinkedHashMap<String, String>();
            hashMap.put("appid", OCRAppID + "");
            //方式一：base64上传
            hashMap.put("image", fileStr);
            //方式二：图片地址上传
//        hashMap.put("url","http://tiku.huatu.com/cdn/images/vhuatu/tiku/p/pN40pdlTazZ8zM30AvjKuF1tdPs.jpeg");
            hashMap.put("bucket", "tencentyun");


            String appSign = OCRSign.appSign(OCRAppID,
                    OCRSecretID,
                    OCRSecretKey,
                    "tencentyun",//（可不填）
                    TimeUnit.DAYS.toMillis(30) / 1000
            );
            //拼接头部信息
            HttpHeaders headers = new HttpHeaders();
            MediaType contentType = MediaType.parseMediaType("application/json");
            headers.setContentType(contentType);
            headers.add("Host", "recognition.image.myqcloud.com");
            headers.add("Content-Type", "application/json");
            headers.add("Content-Length", hashMap.size() + "");
            headers.add("Authorization", appSign);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(hashMap, headers);

            String url =
                    //  "http://recognition.image.myqcloud.com/ocr/generalfast";
                    "https://recognition.image.myqcloud.com/ocr/handwriting";
            //发送post请求
            long start = System.currentTimeMillis();

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            long end = System.currentTimeMillis();
            log.info("OCR拍照识别，总用时" + (end - start) + "毫秒");
            //解析youtu返回数据

            if (null != resp) {
                String body = resp.getBody();
                if (StringUtils.isNotEmpty(body)) {
//                body = new String(body.getBytes("ISO-8859-1"), "UTF-8");

                    log.info("OCR拍照识别结果，响应数据：{}", body);
                    Map map = JSON.parseObject(body);
                    String msg = map.get("message").toString();
                    String code = map.get("code").toString();
                    //响应成功，取出其中文字内容
                    if ("OK".equals(msg) && "0".equals(code)) {
                        Map respData = (Map) map.get("data");
                        if (respData.isEmpty()) {
                            return vo;
                        }
                        List<Map> items = (List<Map>) respData.get("items");
                        if (CollectionUtils.isNotEmpty(items)) {
                            StringBuilder stringBuilder = new StringBuilder();

                            //当前段落
                            Integer paraNum = 0;
                            for (Map item : items) {
                                stringBuilder.append(item.get("itemstring"));
                                Map parag = (Map) item.get("parag");
                                String paragNo = parag.get("parag_no").toString();
                                Integer paraNumTemp = Integer.parseInt(paragNo);
                                if (paraNumTemp > paraNum) {
                                    stringBuilder.append("$$$");
                                    paraNum = paraNumTemp;
                                }
//                            stringBuilder.append(item.get("itemstring"));
//                            stringBuilder.append("$$$");
                            }
                            vo.setContent(stringBuilder.toString());
                        }
                    }
                }
            }
            return vo;
        } catch (Exception e) {
            log.error("图片识别失败:{}", e.getMessage());
            return null;
        }
    }


    private EssayQuestionVO answerDetailV1(long answerId) {
        //根据答题卡id查询答题卡信息
        entityManager.clear();
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
        EssayQuestionVO vo = null;
        if (null != questionAnswer) {
            //查询对应的base信息（取时限）
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
            //根据detailId查询题目详情
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());
            // 获取标准答案
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                    (questionAnswer.getQuestionDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionAnswer.getQuestionDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            EssayStandardAnswer essayStandardAnswer = standardAnswerList.get(0);
            double difficultGrade = questionDetail.getDifficultGrade();
            String difficultGradeStr = "";
            switch ((int) difficultGrade) {
                case 0:
                    break;
                case 1:
                    difficultGradeStr = "难度：较小";
                    break;
                case 2:
                    difficultGradeStr = "难度：适中";
                    break;
                case 3:
                    difficultGradeStr = "难度：较大";
                    break;
            }

            vo = EssayQuestionVO.builder()
                    .type(questionDetail.getType())
                    .questionDetailId(questionBase.getDetailId())//试题的detailId
                    .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明11111
                    .stem(questionDetail.getStem())//题干信息
                    .score(questionDetail.getScore())//题目分数
                    .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                    .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                    .limitTime(questionBase.getLimitTime())//答题限时
                    .questionBaseId(questionAnswer.getQuestionBaseId())//题目的baseId
                    .examScore(questionAnswer.getExamScore())//学员得分
                    .spendTime(questionAnswer.getSpendTime())//答题时间
                    .inputWordNum(questionAnswer.getInputWordNum())//答案字数
                    .correctedContent(StringUtils.isNotEmpty(questionAnswer.getCorrectedContent()) ? questionAnswer.getCorrectedContent() : questionAnswer.getContent())//批改后的答案
                    .answerDetails(questionDetail.getAnswerDetails())
                    .answerRange(questionDetail.getAnswerRange())
                    .answerTask(questionDetail.getAnswerTask())
                    // .correctRule(questionDetail.getCorrectRule())//规则描述
                    .correctRule(null)//不给学员返回阅卷规则
                    .authorityReviews(questionDetail.getAuthorityReviews())//权威点评
                    .analyzeQuestion(questionDetail.getAnalyzeQuestion())//试题分析
                    .sort(questionBase.getSort())//题目序号
                    .bizStatus(questionAnswer.getBizStatus())//答题状态（0 空白，未开始 1未完成 2 已交卷 3已批改）
                    .answerComment(essayStandardAnswer.getAnswerComment())//参考答案
                    .topic(essayStandardAnswer.getTopic())
                    .callName(essayStandardAnswer.getCallName())
                    .subTopic(essayStandardAnswer.getSubTopic())
                    .answerList(standardAnswerList)
                    //答案类型(0 参考答案  1标准答案)(V1单个答案根据阅卷规则判断)
                    .answerFlag(StringUtils.isNotEmpty(questionDetail.getCorrectRule()) ? 1 : 0)
                    /**
                     * 临时替换落款日期和落款人字段（解决客户端展示问题）
                     */
                    .inscribedDate(essayStandardAnswer.getInscribedName())
                    .inscribedName(essayStandardAnswer.getInscribedDate())
                    .difficultGrade(difficultGradeStr)
                    .videoId(questionBase.getVideoId() == null ? 0 : questionBase.getVideoId())
                    .videoAnalyzeFlag((questionBase.getVideoId() != null && questionBase.getVideoId() > 0) ? true : false)
                    .correctType(questionAnswer.getCorrectType())//添加批改类型返回
                    .build();
//            if (replaceLabel(questionDetail.getAnswerComment())) {
//                vo.setAnswerComment(null);
//            }

            if (replaceLabel(questionDetail.getAnswerRequire())) {
                vo.setAnswerRequire(null);
            }

            if (replaceLabel(questionDetail.getCorrectRule())) {
                vo.setCorrectRule(null);
            }

            if (replaceLabel(questionDetail.getAuthorityReviews())) {
                vo.setAuthorityReviews(null);
            }

            if (replaceLabel(questionDetail.getAnalyzeQuestion())) {
                vo.setAnalyzeQuestion(null);
            }

            if (replaceLabel(questionDetail.getAnswerDetails())) {
                vo.setAnswerDetails(null);
            }
            if (replaceLabel(questionDetail.getAnswerRange())) {
                vo.setAnswerRange(null);
            }
            if (replaceLabel(questionDetail.getAnswerTask())) {
                vo.setAnswerTask(null);
            }

            //查询得分点
            LinkedList<ScoreVO> addScoreList = new LinkedList<>();
            LinkedList<ScoreVO> subScoreList = new LinkedList<>();
            LinkedList<ScoreVO> elseScoreList = new LinkedList<>();
            List<EssayUserAnswerQuestionDetailedScore> detailedScores = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatusOrderBySequenceNumberAsc(answerId, 1);
            if (CollectionUtils.isNotEmpty(detailedScores)) {
                //1为内容得分，2为格式得分，3为减分
                for (EssayUserAnswerQuestionDetailedScore detailedScore : detailedScores) {
                    ScoreVO scoreVO = new ScoreVO();
                    BeanUtils.copyProperties(detailedScore, scoreVO);
                    if (1 == detailedScore.getType()) {
                        addScoreList.add(scoreVO);
                    } else if (3 == detailedScore.getType()) {
                        subScoreList.add(scoreVO);
                    } else {
                        elseScoreList.add(scoreVO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(elseScoreList)) {
                addScoreList.addAll(elseScoreList);
            }
            vo.setAddScoreList(addScoreList);
            vo.setSubScoreList(subScoreList);


        }
        vo.setToken(bjyHandler.getToken(vo.getVideoId()));
        return vo;

    }


    @Override
    public List<EssayQuestionVO> answerDetailV2(int userId, int type, long answerId, int terminal, String cv) {
        LinkedList<EssayQuestionVO> essayQuestionVOList = new LinkedList<>();

        //判断是单题还是套题
        if (SINGLE_QUESTION == type) {
            EssayQuestionVO vo = answerDetailV2(answerId);
            essayQuestionVOList.add(vo);
        } else {

            //根据试卷答题卡查询试题答题卡
            String userPaperAnswerKey = RedisKeyConstant.getUserPaperAnswerKey(answerId);
            essayQuestionVOList = (LinkedList<EssayQuestionVO>) redisTemplate.opsForValue().get(userPaperAnswerKey);
            if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                return essayQuestionVOList;
            }
            essayQuestionVOList = new LinkedList<>();
            //根据试卷答题卡查询试题答题卡
            entityManager.clear();
            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerId);

            if (null == paperAnswer) {
                log.warn("答题卡id错误");
                throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
            }
            List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                    (answerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));

            //处理0323模考临时代码
            if (paperAnswer.getPaperBaseId() == 563L && paperAnswer.getType() == AdminPaperConstant.MOCK_PAPER && CollectionUtils.isEmpty(answers)) {
                //创建试题答题卡
                String paperQuestionKey = RedisKeyConstant.getPaperQuestionKey(563);
                List<EssayQuestionRedisVO> questionList = Lists.newArrayList();
                questionList = (List<EssayQuestionRedisVO>) redisTemplate.opsForValue().get(paperQuestionKey);

                List<EssayQuestionAnswer> essayQuestionAnswerList = new LinkedList<>();
                for (EssayQuestionRedisVO questionVO : questionList) {
                    EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                            .userId(userId)
                            .terminal(terminal)
                            .questionType(questionVO.getType())
                            .areaId(paperAnswer.getAreaId())
                            .areaName("")
                            .questionBaseId(questionVO.getBaseId())
                            .questionYear(questionVO.getQuestionYear())
                            .questionDetailId(questionVO.getDetailId())
                            .score(questionVO.getScore())
                            .paperAnswerId(paperAnswer.getId())//对应的是试卷答题卡的id
                            .paperId(paperAnswer.getPaperBaseId())
                            .correctType(questionVO.getCorrectType())
                            .build();
                    essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
                    essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
                    essayQuestionAnswer.setCreator(userId + "");
                    essayQuestionAnswerList.add(essayQuestionAnswer);
                }
                // 批量插入
                essayQuestionAnswerRepository.save(essayQuestionAnswerList);
            }

            if (CollectionUtils.isNotEmpty(answers)) {
                for (EssayQuestionAnswer answer : answers) {
                    EssayQuestionVO vo = answerDetailV2(answer.getId());
                    vo.setTotalExamScore(paperAnswer.getExamScore());
                    vo.setTotalSpendTime(paperAnswer.getSpendTime());
                    essayQuestionVOList.add(vo);
                }
            }
            if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()
                    && CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                redisTemplate.opsForValue().set(userPaperAnswerKey, essayQuestionVOList, 5, TimeUnit.MINUTES);
            }
        }

        return essayQuestionVOList;
    }


    public EssayQuestionVO answerDetailV2(long answerId) {
        //根据答题卡id查询答题卡信息
        entityManager.clear();
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
        EssayQuestionVO vo = null;
        if (null != questionAnswer) {
            //查询对应的base信息（取时限）
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
            //根据detailId查询题目详情
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());
            // 获取标准答案
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                    (questionAnswer.getQuestionDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionAnswer.getQuestionDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            double difficultGrade = questionDetail.getDifficultGrade();
            String difficultGradeStr = "";
            switch ((int) difficultGrade) {
                case 0:
                    break;
                case 1:
                    difficultGradeStr = "难度：较小";
                    break;
                case 2:
                    difficultGradeStr = "难度：适中";
                    break;
                case 3:
                    difficultGradeStr = "难度：较大";
                    break;
            }

            vo = EssayQuestionVO.builder()
                    .type(questionDetail.getType())
                    .questionDetailId(questionBase.getDetailId())//试题的detailId
                    .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明11111
                    .stem(questionDetail.getStem())//题干信息
                    .score(questionDetail.getScore())//题目分数
                    .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                    .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                    .limitTime(questionBase.getLimitTime())//答题限时
                    .questionBaseId(questionAnswer.getQuestionBaseId())//题目的baseId
                    .examScore(questionAnswer.getExamScore())//学员得分
                    .spendTime(questionAnswer.getSpendTime())//答题时间
                    .inputWordNum(questionAnswer.getInputWordNum())//答案字数
                    .correctedContent(StringUtils.isNotEmpty(questionAnswer.getCorrectedContent()) ? questionAnswer.getCorrectedContent() : questionAnswer.getContent())//批改后的答案
                    .answerDetails(questionDetail.getAnswerDetails())
                    .answerRange(questionDetail.getAnswerRange())
                    .answerTask(questionDetail.getAnswerTask())
                    .correctRule(questionDetail.getCorrectRule())//规则描述
                    .authorityReviews(questionDetail.getAuthorityReviews())//权威点评
                    .analyzeQuestion(questionDetail.getAnalyzeQuestion())//试题分析
                    .sort(questionBase.getSort())//题目序号
                    .bizStatus(questionAnswer.getBizStatus())//答题状态（0 空白，未开始 1未完成 2 已交卷 3已批改）
                    .answerList(standardAnswerList)
                    .difficultGrade(difficultGradeStr)
                    .videoId(questionBase.getVideoId() == null ? 0 : questionBase.getVideoId())
                    .videoAnalyzeFlag((questionBase.getVideoId() != null && questionBase.getVideoId() > 0) ? true : false)
                    .correctType(questionAnswer.getCorrectType())//批改类型
                    .correctMode(questionAnswer.getCorrectMode())
                    .build();
            if (replaceLabel(questionDetail.getAnswerComment())) {
                vo.setAnswerComment(null);
            }

            if (replaceLabel(questionDetail.getAnswerRequire())) {
                vo.setAnswerRequire(null);
            }

            if (replaceLabel(questionDetail.getCorrectRule())) {
                vo.setCorrectRule(null);
            }

            if (replaceLabel(questionDetail.getAuthorityReviews())) {
                vo.setAuthorityReviews(null);
            }

            if (replaceLabel(questionDetail.getAnalyzeQuestion())) {
                vo.setAnalyzeQuestion(null);
            }

            if (replaceLabel(questionDetail.getAnswerDetails())) {
                vo.setAnswerDetails(null);
            }
            if (replaceLabel(questionDetail.getAnswerRange())) {
                vo.setAnswerRange(null);
            }
            if (replaceLabel(questionDetail.getAnswerTask())) {
                vo.setAnswerTask(null);
            }

            //查询得分点
            LinkedList<ScoreVO> addScoreList = new LinkedList<>();
            LinkedList<ScoreVO> subScoreList = new LinkedList<>();
            LinkedList<ScoreVO> elseScoreList = new LinkedList<>();
            List<EssayUserAnswerQuestionDetailedScore> detailedScores = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatusOrderBySequenceNumberAsc(answerId, 1);
            if (CollectionUtils.isNotEmpty(detailedScores)) {
                //1为内容得分，2为格式得分，3为减分
                for (EssayUserAnswerQuestionDetailedScore detailedScore : detailedScores) {
                    ScoreVO scoreVO = new ScoreVO();
                    BeanUtils.copyProperties(detailedScore, scoreVO);
                    if (1 == detailedScore.getType()) {
                        addScoreList.add(scoreVO);
                    } else if (3 == detailedScore.getType()) {
                        subScoreList.add(scoreVO);
                    } else {
                        elseScoreList.add(scoreVO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(elseScoreList)) {
                addScoreList.addAll(elseScoreList);
            }
            vo.setAddScoreList(addScoreList);
            vo.setSubScoreList(subScoreList);
            vo.setToken(bjyHandler.getToken(vo.getVideoId()));


        }
        return vo;

    }


    /**
     * 将答题卡加入回收站
     * 备注:人工批改的订单不删除
     *
     * @param type
     * @param answerId
     * @return
     */
    @Override
    public Map delAnswer(int type, long answerId) {
        int i = 0;
        if (type == PAPER) {
            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findByIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
            if (null != paperAnswer) {
                if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus()) {
                    throw new BizException(EssayErrors.PAPER_CORRECT_CAN_NOT_DELETE);
                }
                i = essayPaperAnswerRepository.updateToRecycle(answerId);
            }
        } else {
            EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findByIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
            if (null != essayQuestionAnswer) {
                if (essayQuestionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus()) {
                    throw new BizException(EssayErrors.PAPER_CORRECT_CAN_NOT_DELETE);
                }
                i = essayQuestionAnswerRepository.updateToRecycle(answerId);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("flag", 1 == i);
        return map;
    }

    @Override
    public List<EssayAnswerVO> recycleList(int userId, Integer type, Pageable pageRequest) {
        List<EssayAnswerVO> list = new LinkedList<EssayAnswerVO>();
        //判断是是单题还是试卷( 0 单题  1 试卷 2议论文 )
        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());

        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            List<EssayQuestionAnswer> questionAnswerList = ListUtils.EMPTY_LIST;
            //查询EssayQuestionAnswer表
            if (SINGLE_QUESTION == type) {
                questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusIn
                        (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus(), bizStatusList, pageRequest);
            } else if (ARGUMENTATION == type) {
                questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusIn
                        (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus(), bizStatusList, pageRequest);
            }
            for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
                EssayAnswerVO answerVO = EssayAnswerVO.builder()
                        .questionDetailId(questionAnswer.getQuestionDetailId())//detail试题id
                        .questionBaseId(questionAnswer.getQuestionBaseId())//base试题id
                        .correctDate(DateUtil.convertDateFormat(questionAnswer.getCorrectDate()))//批改时间
                        .examScore(questionAnswer.getExamScore())//学员得分
                        .score(questionAnswer.getScore())//题目总分
                        .answerId(questionAnswer.getId())//答题卡id
                        .bizStatus(questionAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                        .build();

                //题干信息
                EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());

                List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionAnswer.getQuestionBaseId(), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
                if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                    long similarId = similarQuestionList.get(0).getSimilarId();
                    EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.getOne(similarId);
                    if (similarQuestionGroupInfo != null) {
                        answerVO.setSimilarId(similarQuestionGroupInfo.getId());
                        answerVO.setStem(similarQuestionGroupInfo.getShowMsg());
                        answerVO.setQuestionType(similarQuestionGroupInfo.getType());
                    }
                }

                //所属地区
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
                if (essayQuestionBase != null) {
                    if (StringUtils.isEmpty(essayQuestionBase.getSubAreaName())) {
                        answerVO.setAreaId(essayQuestionBase.getAreaId());//地区id
                        answerVO.setAreaName(essayQuestionBase.getAreaName());//地区名称
                    } else {
                        answerVO.setAreaId(essayQuestionBase.getSubAreaId());//子地区id
                        answerVO.setAreaName(essayQuestionBase.getSubAreaName());//子地区名称
                    }
                }
                list.add(answerVO);
            }
        } else {

            List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndStatusAndBizStatusIn
                    (userId,
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus(),
                            bizStatusList,
                            pageRequest);

            for (EssayPaperAnswer paperAnswer : paperAnswerList) {
                EssayAnswerVO answerVO = EssayAnswerVO.builder()
                        .paperName(paperAnswer.getName())//试卷名称
                        .paperId(paperAnswer.getPaperBaseId())//base试卷id
                        .correctDate(DateUtil.convertDateFormat(paperAnswer.getCorrectDate()))//批改时间
                        .examScore(paperAnswer.getExamScore())//学员得分
                        .score(paperAnswer.getScore())//试卷总分
                        .answerId(paperAnswer.getId())
                        .bizStatus(paperAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                        .build();


                //所属地区
                EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
                if (essayPaperBase != null) {
                    answerVO.setAreaId(essayPaperBase.getAreaId());//地区id
                    answerVO.setAreaName(essayPaperBase.getAreaName());//地区名称
                }
                list.add(answerVO);
            }
        }
        return list;
    }

    @Override
    public long countRecycleList(int userId, Integer type) {

        if (SINGLE_QUESTION == type) {
            return essayQuestionAnswerRepository.countByUserIdAndPaperIdAndStatusAndBizStatus(userId, 0, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        } else {
            return essayPaperAnswerRepository.countByUserIdAndStatusAndBizStatus(userId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        }

    }

    /**
     * 单题批改列表
     *
     * @param userId
     * @param type         （type   1标准答案 2议论文）
     * @param modeTypeEnum
     * @param pageRequest
     * @return
     */

    @Override
    public List<EssayAnswerVO> questionCorrectList(int userId, Integer type, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum, Pageable pageRequest) {
        List<EssayAnswerVO> list = new LinkedList<EssayAnswerVO>();

        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        //查询EssayQuestionAnswer表
        List<EssayQuestionAnswer> questionAnswerList = ListUtils.EMPTY_LIST;
        if (type == QuestionTypeConstant.SINGLE_QUESTION) {
            //type不是5
            questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusInAndAnswerCardType
                    (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList,
                            modeTypeEnum.getType(), pageRequest);
        } else if (type == QuestionTypeConstant.ARGUMENTATION) {
            //type是5
            questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusInAndAnswerCardType
                    (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),bizStatusList,
                            modeTypeEnum.getType(),pageRequest);
        }
        //遍历题目
        for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
            EssayAnswerVO answerVO = EssayAnswerVO.builder()
                    .questionDetailId(questionAnswer.getQuestionDetailId())//detail试题id
                    .questionBaseId(questionAnswer.getQuestionBaseId())//base试题id
                    .correctDate(DateUtil.convertDateFormat(questionAnswer.getCorrectDate()))//批改时间
                    .examScore(questionAnswer.getExamScore())//学员得分
                    .score(questionAnswer.getScore())//题目总分
                    .answerId(questionAnswer.getId())//答题卡id
                    .bizStatus(questionAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                    .build();

            //题干信息
            EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());

            List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionAnswer.getQuestionBaseId(), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                long similarId = similarQuestionList.get(0).getSimilarId();
                EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.getOne(similarId);
                if (similarQuestionGroupInfo != null) {
                    answerVO.setSimilarId(similarQuestionGroupInfo.getId());
                    answerVO.setStem(similarQuestionGroupInfo.getShowMsg());
                    answerVO.setQuestionType(similarQuestionGroupInfo.getType());
                }
            }

            //所属地区
            EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
            if (essayQuestionBase != null) {
                if (StringUtils.isEmpty(essayQuestionBase.getSubAreaName())) {
                    answerVO.setAreaId(essayQuestionBase.getAreaId());//地区id
                    answerVO.setAreaName(essayQuestionBase.getAreaName());//地区名称
                } else {
                    answerVO.setAreaId(essayQuestionBase.getSubAreaId());//子地区id
                    answerVO.setAreaName(essayQuestionBase.getSubAreaName());//子地区名称
                }
                answerVO.setVideoId(essayQuestionBase.getVideoId() == null ? 0 : essayQuestionBase.getVideoId());
                answerVO.setVideoAnalyzeFlag((essayQuestionBase.getVideoId() != null && essayQuestionBase.getVideoId() > 0) ? true : false);
            }
            list.add(answerVO);
        }

        return list;
    }

    @Override
    public long countQuestionCorrectList(int userId, Integer type) {

        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());
        //查询EssayQuestionAnswer表
        long count = 0L;
        if (type == QuestionTypeConstant.SINGLE_QUESTION) {
            //type不是5
            count = essayQuestionAnswerRepository.countByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusIn
                    (userId, 0L, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList);

        } else if (type == QuestionTypeConstant.ARGUMENTATION) {
            //type是5
            count = essayQuestionAnswerRepository.countByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusIn
                    (userId, 0L, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList);
        }
        return count;
    }


    /**
     * 提交未批改完成的答题卡
     *
     * @return
     */
    @Override
    public Object unfinishedCardCommit() {
        Date fiveMinutesBefore = DateUtil.getMinutesBefore(5);
        Date tenMinutesBefore = DateUtil.getMinutesBefore(10);
//        Date tenMinutesBefore = DateUtil.getDaysBefore(500);
        log.info("定时未完成的答题卡ID");
        List<EssayQuestionAnswer> unfinishedCardList = essayQuestionAnswerRepository.findUnfinishedCard(tenMinutesBefore, fiveMinutesBefore);
        List<EssayPaperAnswer> unfinishedPaperCardList = essayPaperAnswerRepository.findUnfinishedCard(tenMinutesBefore, fiveMinutesBefore);
        if (CollectionUtils.isNotEmpty(unfinishedCardList)) {
            for (EssayQuestionAnswer card : unfinishedCardList) {
                log.info("定时任务处理未批改完成的答题卡,questionAnswerId：{}", card.getId());
                String url = answerCorrectUrl + "?answerCardId=" + card.getId() + "&type=" + SINGLE_QUESTION;
                long start = System.currentTimeMillis();
                ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);

            }
        }

        if (CollectionUtils.isNotEmpty(unfinishedPaperCardList)) {
            for (EssayPaperAnswer card : unfinishedPaperCardList) {
                log.info("定时任务处理未批改完成的答题卡,paperAnswerId：{}", card.getId());

                String url = answerCorrectUrl + "?answerCardId=" + card.getId() + "&type=" + PAPER;
                long start = System.currentTimeMillis();
                ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);

            }
        }

        return null;
    }


    //持久化拍照答题信息
    @Override
    public void savePhotoAnswer(YoutuMQVO vo) {

        EssayPhotoAnswer photoAnswer = EssayPhotoAnswer.builder()
                .url(vo.getUrl())
                .userId(vo.getUserId())
                .terminal(vo.getTerminal())
                .content(vo.getContent())
                .build();
        essayPhotoAnswerRepository.save(photoAnswer);

    }


    /**
     * @param file
     * @param userId
     * @param answerId
     * @param sort
     * @return
     */
    @Override
    public Object photoDistinguish(MultipartFile file, int userId, long answerId, int sort) {
        if (answerId == 0) {
            throw new BizException(ErrorResult.create(1000010, "答题卡不能为空"));
        }
        if (null == file) {
            throw new BizException(ErrorResult.create(100010, "图片不能为空"));
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String originalFilename = file.getOriginalFilename();
        //读取文件后缀
        int indexOf = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(indexOf, originalFilename.length());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
        String url = MANUAL_CORRECT_SAVE_URL + fileName;
        try {
            InputStream inputStream = file.getInputStream();
            //线程异步写入cdn
            executorService.execute(() -> uploadFileUtil.ftpUploadFileInputStream(inputStream, fileName, MANUAL_CORRECT_SAVE_PATH));
            YoutuVO youtuVO = photoDistinguish(file, 0);
            if (null == youtuVO) {
                throw new BizException(ErrorResult.create(100010, "图片识别异常！"));
            }
            String content = youtuVO.getContent();
            CorrectImage correctImage = CorrectImage.builder()
                    .imageUrl(url)
                    .sort(sort)
                    .questionAnswerId(answerId)
                    .content(content)
                    .build();
            correctImage.setStatus(EssayStatusEnum.NORMAL.getCode());
            correctImage.setGmtCreate(new Date());
            correctImage.setGmtModify(new Date());

            correctImage = essayCorrectImageRepository.save(correctImage);
            PhotoDistinguishVo photoDistinguishVo = PhotoDistinguishVo.builder()
                    .content(content)
                    .id(correctImage.getId())
                    .url(url)
                    .sort(sort)
                    .build();
            return photoDistinguishVo;
        } catch (IOException e) {
            e.printStackTrace();
            return PhotoDistinguishVo.builder()
                    .url(url)
                    .sort(sort)
                    .build();
        } finally {
            executorService.shutdown();
        }
    }

    /**
     * 修改答题图片排序
     *
     * @param userId
     * @param dtoList
     * @return
     */
    @Override
    public Object updatePhotoSort(int userId, List<ImageSortDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return SuccessMessage.create("操作成功");
        }
        for (ImageSortDto dto : dtoList) {
            Long answerCardId = dto.getAnswerId();
            List<CorrectImage> dataImages = essayCorrectImageRepository.findByQuestionAnswerId(answerCardId);
            if (CollectionUtils.isNotEmpty(dataImages)) {
                List<Long> dataImageIdList = dataImages.stream().map(CorrectImage::getId).collect(Collectors.toList());
                List<Long> currentImageIdList = dto.getImageList().stream().map(ImageSortDto.ImageSortDetailDto::getImageId).collect(Collectors.toList());
                Map<Long, ImageSortDto.ImageSortDetailDto> currentImageMap = dto.getImageList().stream().collect(Collectors.toMap(i -> i.getImageId(), i -> i));

                for (Long imageId : dataImageIdList) {
                    if (!currentImageIdList.contains(imageId)) {
                        deleteImageByLogic(dto.getAnswerId(), imageId);
                    } else {
                        ImageSortDto.ImageSortDetailDto detailDto = currentImageMap.get(imageId);
                        CorrectImage correctImage = essayCorrectImageRepository.findOneByQuestionAnswerIdAndId(answerCardId, imageId);
                        if (null != correctImage) {
                            essayCorrectImageRepository.modifyImageSortById(detailDto.getSort(), new Date(), detailDto.getImageId(), answerCardId);
                        }
                    }
                }
            }
        }
        return SuccessMessage.create("操作成功");
    }


    /**
     * 逻辑删除图片long
     *
     * @param answerId
     * @param imageId
     * @return
     */
    @Override
    public Object deleteImageByLogic(long answerId, long imageId) {
        CorrectImage correctImage = essayCorrectImageRepository.findOne(imageId);
        if (null == correctImage) {
            throw new BizException(ErrorResult.create(1000010, "数据不存在！"));
        }
        int result = essayCorrectImageRepository.deleteImageByLogic(EssayStatusEnum.DELETED.getCode(), new Date(), imageId, answerId);
        if (result == 1) {
            return SuccessMessage.create("删除成功！");
        } else {
            return SuccessMessage.create("删除失败！");
        }
    }

    @Override
    public BiFunction<Integer, CreateAnswerCardVO, Long> getUnFinishedCount(CreateAnswerCardVO createAnswerCardVO) {
        if (createAnswerCardVO.getType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            return (Integer userId, CreateAnswerCardVO answerCardVO) -> essayQuestionAnswerRepository
                    .countByUserIdAndQuestionBaseIdAndPaperIdAndCorrectModeAndStatusAndBizStatusAndAnswerCardType(userId, answerCardVO.getQuestionBaseId(),
                            0, answerCardVO.getCorrectMode(),
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(), EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
        } else {
            return (Integer userId, CreateAnswerCardVO answerCardVO) -> essayPaperAnswerRepository
                    .countByUserIdAndPaperBaseIdAndTypeAndCorrectModeAndStatusAndBizStatusAndAnswerCardType(userId, answerCardVO.getPaperBaseId(),
                            AdminPaperConstant.TRUE_PAPER, answerCardVO.getCorrectMode(),
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(), EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
        }
    }

    @Override
    public ResponseVO createAnswerCardV2(int userId, CreateAnswerCardVO createAnswerCardVO, int terminal, BiFunction<Integer, CreateAnswerCardVO, Long> unFinishedCount, int answerCardType) {
        Integer type = createAnswerCardVO.getType();// 题目类型 0单题 1套题
        Integer correctMode = createAnswerCardVO.getCorrectMode();// 批改类型
        Long questionBaseId = createAnswerCardVO.getQuestionBaseId();// 题目的baseId
        Long paperBaseId = createAnswerCardVO.getPaperBaseId();// 试卷baseId
        log.info("创建答题卡,type:{},correctMode:{},questionBaseId:{},terminal:{}", type, correctMode, questionBaseId, terminal);
        ResponseVO vo = new ResponseVO();
        // 判断是是单题还是试卷(答题卡类型 0 单题 1 试卷 )
        long answerId = 0;
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            // 判断用户是否该题已存在答题卡（未完成）
            long unfinishedCount = unFinishedCount.apply(userId, createAnswerCardVO);
            if (0 != unfinishedCount) {
                log.info("该题存在未完成的答题卡，答题卡创建失败");
                throw new BizException(EssayErrors.UNFINISHED_PAPER);
            }

            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
            if (null == questionBase) {
                log.info("试卷不存在 ,questionBaseId {}" + questionBaseId);
                throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
            }
            long questionDetailId = questionBase.getDetailId();// 题目的detailId
            EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(questionDetailId);
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder().userId(userId).terminal(terminal)
                    .questionType(essayQuestionDetail.getType()).areaId(questionBase.getAreaId())
                    .areaName(questionBase.getAreaName()).questionBaseId(questionBaseId)
                    .questionYear(questionBase.getQuestionYear()).questionDetailId(questionDetailId)
                    .score(essayQuestionDetail.getScore()).paperId(0)// 单题答题卡对应的paperId是0
                    .labelStatus(0).subScore(-1D).correctType(essayQuestionDetail.getCorrectType()).subScoreRatio(0D)
                    .correctMode(correctMode)// 批改类型
                    .build();
            essayQuestionAnswer.setCreator(userId + "");
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayQuestionAnswer.setAnswerCardType(answerCardType);
            essayQuestionAnswerRepository.save(essayQuestionAnswer);
            answerId = essayQuestionAnswer.getId();
            log.info("创建试题答题卡,答题卡ID是:{}", answerId);
        } else {

            // 判断用户是否该试卷已存在答题卡（未完成）
            long unfinishedCount = unFinishedCount.apply(userId, createAnswerCardVO);

            if (0 != unfinishedCount) {
                log.info("该题存在未完成的答题卡，答题卡创建失败");
                throw new BizException(EssayErrors.UNFINISHED_PAPER);
            }
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperBaseId);
            if (null == paperBase) {
                log.info("答题卡创建失败,试卷信息获取为空 ==>>,userId = {},terminal => {},createAnswerCardVO => {}", userId, terminal,
                        createAnswerCardVO);
                throw new BizException(ErrorResult.create(5000513, "获取试卷信息失败"));
            }
            // 如果是估分试卷，不允许答题
            if (9999 == paperBase.getAreaId()) {
                log.info("估分试卷暂不支持作答，paperBaseId：{}", paperBaseId);
                throw new BizException(EssayErrors.GUFEN_CANOT_ANSWER);
            }
            // 如果是模考，判断是否是已结束
            if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
                EssayMockExam essayMockExam = essayMockExamRepository.findOne(paperBaseId);
                if (essayMockExam.getEndTime().getTime() > System.currentTimeMillis()) {
                    log.info("当前模考暂未结束，暂时不可答题");
                    throw new BizException(EssayErrors.MOCK_NOT_FINISH_CANOT_ANSWER);
                }
            }
            int questionCountByPaper = essayQuestionBaseRepository.countByPaperId(paperBaseId);
            EssayPaperAnswer essayPaperAnswer = EssayPaperAnswer.builder().paperBaseId(paperBaseId)
                    .areaId(paperBase.getAreaId()).areaName(paperBase.getAreaName()).name(paperBase.getName())
                    .score(paperBase.getScore()).userId(userId).unfinishedCount(questionCountByPaper)
                    .type(AdminPaperConstant.TRUE_PAPER)// 真题
                    .correctMode(correctMode).build();
            if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
                // 0226 名字是行测考试的名字-申论
                EssayPaperBase mock = PaperManager.getPaperBase(essayPaperBaseRepository, essayMockExamRepository,
                        redisTemplate, paperBaseId, mockRedisExpireTime);
                essayPaperAnswer.setName(mock.getName());
            }
            essayPaperAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayPaperAnswer.setBizStatus(
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayPaperAnswer.setCreator(userId + "");
            essayPaperAnswer.setAnswerCardType(answerCardType);
            essayPaperAnswer = essayPaperAnswerRepository.save(essayPaperAnswer);
            // log.info("创建试卷答题卡信息是:{}", JsonUtil.toJson(essayPaperAnswer));
            answerId = essayPaperAnswer.getId();
            // 创建试题答题卡
            List<EssayQuestionAnswerSimpleVO> questionAnswerCardList = createQuestionCard(paperBase, answerId, userId,
                    terminal, correctMode, answerCardType);
            vo.setQuestionAnswerCardList(questionAnswerCardList);
        }
        if (0 != answerId) {
            vo.setAnswerCardId(answerId);
        }
        return vo;
    }

    /**
     * 根据试卷信息和试卷答题卡id创建试题答题卡信息
     *
     * @param paperBase
     * @param paperAnswerId
     * @param userId
     * @param terminal
     * @param answerCardType 1普通 2课后
     * @return
     */
    private List<EssayQuestionAnswerSimpleVO> createQuestionCard(EssayPaperBase paperBase, long paperAnswerId, int userId,
                                                                 int terminal, int correctMode, int answerCardType) {
        List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus(
                paperBase.getId(), new Sort(Sort.Direction.ASC, "sort"),
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        List<EssayQuestionAnswer> essayQuestionAnswers = new LinkedList<>();
        List<EssayQuestionAnswerSimpleVO> essayQuestionAnswerVos = new LinkedList<>();
        essayQuestionBaseList.forEach(questionBase -> {
            long questionDetailId = questionBase.getDetailId();// 题目的detailId
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionDetailId);
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder().userId(userId).terminal(terminal)
                    .questionType(questionDetail.getType()).areaId(questionBase.getAreaId())
                    .areaName(questionBase.getAreaName()).questionBaseId(questionBase.getId())
                    .questionYear(questionBase.getQuestionYear()).questionDetailId(questionDetailId)
                    .score(questionDetail.getScore()).paperId(paperBase.getId()).labelStatus(0).subScore(-1D)
                    .paperAnswerId(paperAnswerId).correctType(questionDetail.getCorrectType()).subScoreRatio(0D)
                    .correctMode(correctMode)
                    .answerCardType(answerCardType)
                    .build();
            essayQuestionAnswer.setCreator(userId + "");
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());

            essayQuestionAnswers.add(essayQuestionAnswer);
        });
        // log.info("试题答题卡:{},terminal类型是:{}", JsonUtil.toJson(essayQuestionAnswers), terminal);
        essayQuestionAnswerRepository.save(essayQuestionAnswers);
        essayQuestionAnswers.forEach(questionAnswer -> {

            essayQuestionAnswerVos.add(EssayQuestionAnswerSimpleVO.builder().id(questionAnswer.getId())
                    .questionBaseId(questionAnswer.getQuestionBaseId()).build());
        });

        return essayQuestionAnswerVos;

    }

    /**
     * 获取指定图内容
     */
    @Override
    public Object getImageContent(int uid, Long answerId) {
        List<CorrectImage> imagesContentList = essayCorrectImageRepository
                .findByQuestionAnswerIdAndStatusOrderBySort(answerId, EssayStatusEnum.NORMAL.getCode());
        StringBuilder sb = new StringBuilder();
        imagesContentList.forEach(correctImage -> {
            sb.append(correctImage.getContent());
        });
        return sb.toString();
    }

    @Override
    public Object paperCommitV2(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, String cv) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("msg", "");
        TeacherOrderTypeEnum teacherOrderTypeEnum = null;
        int userId = userSession.getId();
        // 替换答案中的加号
        paperCommitVO.getAnswerList().forEach(answer -> {
            String content = answer.getContent();
            if (content != null) {
                answer.setContent(content.replaceAll("\\+", "＋"));
            }
        });
        // 批改是否免费
        String essayGoodsFree = String.valueOf(redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY));
        int type = paperCommitVO.getType();// 答题类型 0单题 1套题
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();// 答案列表
        Long answerCardId = paperCommitVO.getAnswerCardId();// 答题卡id

        Integer correctMode = paperCommitVO.getCorrectMode();// 批改类型

        if (null == answerCardId) {
            log.warn("缺少答题卡ID 参数 {}", answerCardId);
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        if (null == correctMode) {
            log.warn("缺少批改模式 参数 {}", correctMode);
            throw new BizException(EssayErrors.ERROR_CORRECT_MODE_NULL);
        }
        List<EssayQuestionVO> essayQuestionVOS = new ArrayList<EssayQuestionVO>();
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            Integer questionType = singleQuestionCommitV2(answerList, answerCardId, paperCommitVO, userSession, essayGoodsFree, type,
                    terminal);
            if (null != questionType) {
                teacherOrderTypeEnum = TeacherOrderTypeEnum.convert(questionType.intValue());
            }
        } else {
            correctPaperV2(userSession, paperCommitVO, terminal);
            teacherOrderTypeEnum = TeacherOrderTypeEnum.SET_QUESTION;
        }
        // 如果是智能择返回人工择返回空
        if (correctMode == CorrectModeEnum.INTELLIGENCE.getMode()) {
            essayQuestionVOS = answerDetail(userId, type, answerCardId, terminal, cv);
        } else {
            if (paperCommitVO.getSaveType() == AnswerSaveTypeConstant.COMMIT) {
                data.put("msg", TeacherOrderTypeEnum.submitContent(teacherOrderTypeEnum, paperCommitVO.getDelayStatus()));
            }
        }
        data.put("list", essayQuestionVOS);
        return data;
    }
}
