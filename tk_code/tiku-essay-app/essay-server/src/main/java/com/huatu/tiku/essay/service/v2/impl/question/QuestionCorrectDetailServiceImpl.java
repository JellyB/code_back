package com.huatu.tiku.essay.service.v2.impl.question;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.QuestionTypeConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.v2.*;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.correct.IntelligenceConvertManualRecordService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.SINGLE_QUESTION;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 app端 试题批改详情
 */
@Service
public class QuestionCorrectDetailServiceImpl implements QuestionCorrectDetailService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionCorrectDetailServiceImpl.class);

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserAnswerService userAnswerService;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    EssayLabelDetailRepository essayLabelDetailRepository;

    @Autowired
    EssayCorrectImageRepository essayCorrectImageRepository;

    @Autowired
    QuestionTypeService questionTypeService;

    @Autowired
    EssayLabelCommentRelationRepository essayLabelCommentRelationRepository;

    @Autowired
    BjyHandler bjyHandler;

    @Autowired
    EssayCommentTemplateRepository commentTemplateRepository;

    @Autowired
    EssayCommentTemplateDetailRepository commentTemplateDetailRepository;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    IntelligenceConvertManualRecordRepository recordRepository;

    @Autowired
    IntelligenceConvertManualRecordService recordService;


    @Override
    public List<EssayQuestionVO> answerDetailV3(int userId, int type, long answerId, int terminal, String cv) {
        LinkedList<EssayQuestionVO> essayQuestionVOList = new LinkedList<>();

        //单题详情
        if (SINGLE_QUESTION == type) {
            String userPaperAnswerDetailKey = RedisKeyConstant.getUserPaperAnswerDetailKey(answerId, SINGLE_QUESTION);
            try {
                essayQuestionVOList = (LinkedList<EssayQuestionVO>) redisTemplate.opsForValue().get(userPaperAnswerDetailKey);
                if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                    EssayQuestionVO essayQuestionVO = essayQuestionVOList.get(0);
                    if (null != essayQuestionVO) {
                        essayQuestionVO.setConvertCount(getConvertCount(essayQuestionVOList.get(0).getCorrectMode(), answerId));
                    }
                    essayQuestionVOList.add(essayQuestionVO);
                    return essayQuestionVOList;
                }
            } catch (Exception e) {
                redisTemplate.delete(userPaperAnswerDetailKey);
                logger.info("get  question correct detail error,answerId:{},error:{}", answerId, e);
            }
            EssayQuestionVO vo = userAnswerService.answerDetailV2(answerId);
            //组装批改信息
            dealQuestionAnswerInfo(vo, vo.getCorrectMode(), SINGLE_QUESTION, answerId);
            essayQuestionVOList = new LinkedList<>();
            essayQuestionVOList.add(vo);
            if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                redisTemplate.opsForValue().set(userPaperAnswerDetailKey, essayQuestionVOList, 15, TimeUnit.MINUTES);
            }
        } else {
            //套卷详情
            String userPaperAnswerKey = RedisKeyConstant.getUserPaperAnswerKey(answerId);
            try {
                essayQuestionVOList = (LinkedList<EssayQuestionVO>) redisTemplate.opsForValue().get(userPaperAnswerKey);
                if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                    return essayQuestionVOList;
                }
            } catch (Exception e) {
                redisTemplate.delete(userPaperAnswerKey);
                logger.info("get paper correct detail error,answerId:{},error:{}", answerId, e);
            }
            essayQuestionVOList = new LinkedList<>();
            //根据试卷答题卡查询试题答题卡
            entityManager.clear();
            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerId);
            if (null == paperAnswer) {
                throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
            }
            List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                    (answerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));

            if (CollectionUtils.isNotEmpty(answers)) {
                for (EssayQuestionAnswer answer : answers) {
                    EssayQuestionVO vo = userAnswerService.answerDetailV2(answer.getId());
                    dealQuestionAnswerInfo(vo, paperAnswer.getCorrectMode(), QuestionTypeConstant.PAPER, answer.getId());
                    vo.setTotalExamScore(paperAnswer.getExamScore());
                    vo.setTotalSpendTime(paperAnswer.getSpendTime());
                    essayQuestionVOList.add(vo);
                }
            }
            if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()
                    && CollectionUtils.isNotEmpty(essayQuestionVOList)) {
                redisTemplate.opsForValue().set(userPaperAnswerKey, essayQuestionVOList, 15, TimeUnit.MINUTES);
            }
        }
        return essayQuestionVOList;
    }


    /**
     * 添加评语信息
     *
     * @param essayQuestionVo 用户试题信息
     * @param correctMode     批改模式 1 智能批改 1 人工批改
     * @param answerId        答题卡ID
     */
    private void getUserManualCorrectInfo(EssayQuestionVO essayQuestionVo, int correctMode, long answerId) {
        if (null == essayQuestionVo || correctMode == CorrectModeEnum.INTELLIGENCE.getMode()) {
            return;
        }
        //本题阅卷批注 && 扣分项
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(answerId);
        if (null != questionAnswer) {
            String correctRemark = questionAnswer.getCorrectRemark();
            if (StringUtils.isNotEmpty(correctRemark)) {
                RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
                essayQuestionVo.setRemarkList(remarkListVo.getQuestionRemarkList());
                essayQuestionVo.setDeRemarkList(remarkListVo.getDeRemarkList());
            }
        }

        List<EssayLabelTotal> labelTotals = essayQuestionLabelService.findTotalByAnswerIds(Lists.newArrayList(answerId),
                LabelFlagEnum.STUDENT_LOOK);
        if (CollectionUtils.isEmpty(labelTotals)) {
            return;
        }
        //学员得分信息
        essayQuestionVo.setUserMeta(getUserInfo(answerId));
        //名师之声
        Optional<EssayLabelTotal> labelTotal = labelTotals.stream().findFirst();
        Integer audioId = labelTotal.get().getAudioId();
        essayQuestionVo.setAudioId(audioId);
        essayQuestionVo.setAudioToken(bjyHandler.getToken(audioId));
    }

    /**
     * 智能批改～议论文添加本题阅卷
     */
    public void addArgument(EssayQuestionVO essayQuestionVo, long answerId) {
        //TODO 待找到相应的枚举值
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(answerId);
        if (null != questionAnswer) {
            String correctRemark = questionAnswer.getCorrectRemark();
            if (StringUtils.isNotEmpty(correctRemark)) {
                RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
                essayQuestionVo.setRemarkList(remarkListVo.getQuestionRemarkList());
            }
        }
    }


    /**
     * 学员得分(批注图片信息)
     *
     * @param questionAnswerCardId
     * @return
     */
    public List<CorrectImageVO> getUserInfo(long questionAnswerCardId) {
        List<CorrectImage> correctImages = essayCorrectImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(questionAnswerCardId,
                EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(correctImages)) {
            return Lists.newArrayList();
        }

        List<CorrectImageVO> collect = correctImages.stream().map(image -> {
            CorrectImageVO correctImageVo = new CorrectImageVO();
            correctImageVo.setId(image.getId());
            correctImageVo.setFinalUrl(image.getFinalUrl());
            correctImageVo.setSort(image.getSort());
            return correctImageVo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 本题阅卷 && 扣分项
     *
     * @param type
     */
    public RemarkListVo getQuestionRemarkListInfo(int type, long totalId, String elseRemark) {
        RemarkListVo remarkListVo = new RemarkListVo();
        if (type == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            List<LabelCommentRelationVO> relationVOList = adminCommentTemplateService.findRemarkListById(totalId, TemplateEnum.LabelTypeEnum.QUESTION_TOTAL);
            List<RemarkVo> remarkResultVoList = transRemarkVo(relationVOList);
            if (CollectionUtils.isEmpty(remarkResultVoList)) {
                return remarkListVo;
            }
            List<RemarkVo> questionRemarkList = remarkResultVoList.stream().filter(remarkVo -> remarkVo.getLabelType() == TemplateEnum.CommentTemplateEnum.BTYJ.getType())
                    .collect(Collectors.toList());
            List<RemarkVo> deRemarkList = remarkResultVoList.stream().filter(remarkVo -> remarkVo.getLabelType() == TemplateEnum.CommentTemplateEnum.KFX.getType())
                    .collect(Collectors.toList());
            List<RemarkVo> questionResult = formatRemarkResult(questionRemarkList, TemplateEnum.CommentTemplateEnum.BTYJ.getType(), elseRemark);
            List<RemarkVo> deResult = formatRemarkResult(deRemarkList, TemplateEnum.CommentTemplateEnum.KFX.getType(), elseRemark);

            remarkListVo.setQuestionRemarkList(questionResult);
            remarkListVo.setDeRemarkList(deResult);
        }
        return remarkListVo;
    }

    /**
     * 模版+评语拼接为展示格式
     *
     * @param userRemarkList
     * @param labelType
     * @return
     */
    public List<RemarkVo> formatRemarkResult(List<RemarkVo> userRemarkList, int labelType, String elseRemark) {
        List<RemarkVo> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userRemarkList)) {
            int i = 1;
            for (RemarkVo remark : userRemarkList) {
                StringBuffer content = new StringBuffer();
                //本题阅卷 && 单个批注,需要拼接模版名称
                if (labelType == TemplateEnum.CommentTemplateEnum.BTYJ.getType() ||
                        labelType == TemplateEnum.CommentTemplateEnum.DDPZ.getType()) {
                    content.append(remark.getTemplateContent());
                    content.append(":");
                }
                content.append(remark.getCommentContent());
                RemarkVo remarkVo = new RemarkVo();
                remarkVo.setContent(content == null ? "" : content.toString());
                remarkVo.setScore(remark.getScore());
                remarkVo.setCommentId(remark.getCommentId());
                remarkVo.setLabelId(remark.getLabelId());
                remarkVo.setSort(i);
                i++;
                list.add(remarkVo);
            }
            if (StringUtils.isNotEmpty(elseRemark) && labelType != TemplateEnum.CommentTemplateEnum.KFX.getType()) {
                RemarkVo elseRemarkVo = new RemarkVo();
                elseRemarkVo.setContent(elseRemark);
                elseRemarkVo.setSort(i);
                elseRemarkVo.setLabelType(labelType);
                list.add(elseRemarkVo);
            }
        }
        return list;
    }


    /**
     * 套卷 综合评价
     *
     * @param type
     * @param totalId
     * @return
     */
    public RemarkListVo getPaperRemarkList(int type, long totalId, String elseRemark) {
        RemarkListVo remarkListVo = new RemarkListVo();
        if (type == EssayAnswerCardEnum
                .TypeEnum.PAPER.getType()) {
            List<LabelCommentRelationVO> relationVOList = adminCommentTemplateService.findRemarkListById(totalId, TemplateEnum.LabelTypeEnum.PAPER_TOTAL);
            if (CollectionUtils.isNotEmpty(relationVOList)) {
                List<RemarkVo> remarkVos = this.transRemarkVo(relationVOList);
                List<RemarkVo> paperRemarkVos = formatRemarkResult(remarkVos, TemplateEnum.LabelTypeEnum.PAPER_TOTAL.getType(), elseRemark);
                remarkListVo.setPaperRemarkList(paperRemarkVos);
            }
        }
        return remarkListVo;
    }


    /**
     * 根据用户评语查询模版评语完整信息
     *
     * @param relationVOList
     * @return
     */
    public List<RemarkVo> transRemarkVo(List<LabelCommentRelationVO> relationVOList) {
        List<RemarkVo> remarkResultVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(relationVOList)) {
            //查询模版信息,评语信息
            List<Long> commentIds = relationVOList.stream().map(LabelCommentRelationVO::getCommentId).distinct().collect(Collectors.toList());
            List<Long> templateIds = relationVOList.stream().map(LabelCommentRelationVO::getTemplateId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(commentIds) && CollectionUtils.isNotEmpty(templateIds)) {
                List<CommentTemplate> commentTemplates = commentTemplateRepository.findByIdInAndStatus(templateIds, EssayStatusEnum.NORMAL.getCode());
                List<CommentTemplateDetail> commentTemplateDetailList = commentTemplateDetailRepository.findByIdInAndStatus(commentIds, EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(commentTemplates) && CollectionUtils.isNotEmpty(commentTemplateDetailList)) {
                    int i = 1;
                    for (LabelCommentRelationVO relationVO : relationVOList) {
                        Optional<CommentTemplate> commentTemplateList = commentTemplates.stream().filter(template -> template.getId() == relationVO.getTemplateId()).findFirst();
                        Optional<CommentTemplateDetail> templateDetailList = commentTemplateDetailList.stream().filter(detail -> detail.getId() == relationVO.getCommentId()).findFirst();

                        RemarkVo remarkVo = RemarkVo.builder().commentId(relationVO.getCommentId())
                                .commentContent(templateDetailList.get().getContent())
                                .templateId(relationVO.getTemplateId())
                                .templateContent(commentTemplateList.get().getName())
                                .sort(i)
                                .labelType(commentTemplateList.get().getLabelType())
                                .score(relationVO.getScore())
                                .labelId(relationVO.getLabelId())
                                .build();
                        remarkResultVoList.add(remarkVo);
                    }
                }
            }
        }
        return remarkResultVoList;
    }


    public void dealQuestionAnswerInfo(EssayQuestionVO vo, int correctMode, int answerType, long answerId) {
        if (null == vo) {
            return;
        }
        //人工批改,需要添加批注评语内容
        if (correctMode != CorrectModeEnum.INTELLIGENCE.getMode()) {
            getUserManualCorrectInfo(vo, correctMode, answerId);
        }
        //智能批改议论文 添加本题阅卷
        if (correctMode == CorrectModeEnum.INTELLIGENCE.getMode() && vo.getType() == 5) {
            addArgument(vo, answerId);
        }
        //智能转人工批改次数
        if (correctMode == CorrectModeEnum.INTELLIGENCE.getMode() && answerType == SINGLE_QUESTION) {
            Integer convertCount = getConvertCount(correctMode, answerId);
            logger.info("count是:{}", convertCount);
            vo.setConvertCount(convertCount);
        }
    }

    /**
     * 获取智能转人工答题卡数量
     *
     * @param
     * @param correctMode
     * @return
     */
    public int getConvertCount(int correctMode, long answerCardId) {
        if (correctMode == CorrectModeEnum.INTELLIGENCE.getMode()) {
            List<Long> convertOrderIds = recordService.getConvertOrderIds(answerCardId, QuestionTypeConstant.SINGLE_QUESTION);
            if (CollectionUtils.isNotEmpty(convertOrderIds)) {
                return convertOrderIds.size();
            }
        }
        return 0;
    }
}
