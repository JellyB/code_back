package com.huatu.tiku.essay.service.impl.question;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.error.EssayLabelErrors;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.impl.correct.TeacherOrderUtil;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.snapshot.QuestionLabelContentSnapshotService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.util.file.LabelXmlUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSimpleVO;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EssayQuestionLabelServiceImpl implements EssayQuestionLabelService {

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayLabelDetailRepository essayLabelDetailRepository;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    CorrectOrderService correctOrderService;

    @Autowired
    EssayCorrectImageRepository correctImageRepository;

    @Autowired
    QuestionLabelContentSnapshotService questionLabelContentSnapshotService;

    @Autowired
    EssayLabelService essayLabelService;

    @Autowired
    LabelXmlUtil labelXmlUtil;

    @Autowired
    EssayQuestionDetailRepository questionDetailRepository;

    @Autowired
    QuestionCorrectDetailService questionCorrectDetailService;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayTeacherService essayTeacherService;

    /**
     * 多个答题卡批量查询相关的批注信息
     *
     * @param answerIds
     * @param labelFlagEnum
     * @return
     */
    @Override
    public List<EssayLabelTotal> findTotalByAnswerIds(List<Long> answerIds, LabelFlagEnum labelFlagEnum) {
        List<EssayLabelTotal> totals = essayLabelTotalRepository.findByAnswerIdInAndStatusAndLabelFlagOrderByGmtCreate(
                answerIds, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), labelFlagEnum.getCode());
        return totals;
    }

    /**
     * 用户详细批注信息查询
     *
     * @param labelDetailId
     * @return
     */
    @Override
    public QuestionLabelDetailVO findDetailById(long labelDetailId) {
        EssayLabelDetail detail = essayLabelDetailRepository.findOne(labelDetailId);
        QuestionLabelDetailVO labelDetail = QuestionLabelDetailVO.builder()
                .id(detail.getId())
                .imageId(detail.getImageId())
                .questionAnswerCardId(detail.getAnswerId())
                .totalId(detail.getTotalId())
                .content(detail.getContent())
                .elseRemark(detail.getElseRemark())
                .imageAxis(detail.getImageAxis())
                .labeledContent(detail.getLabeledContent())
                .build();
        EssayLabelTotal one = essayLabelTotalRepository.findOne(detail.getTotalId());
        if (StringUtils.isNotBlank(one.getLabeledContent())) {
            labelDetail.setLabeledContent(one.getLabeledContent());
        }
        List<LabelCommentRelationVO> remarkList = adminCommentTemplateService.findRemarkListById(labelDetailId, TemplateEnum.LabelTypeEnum.QUESTION_DETAIL);
        labelDetail.setRemarkList(remarkList);
        return labelDetail;
    }

    @Override
    public Object saveLabelDetail(QuestionLabelDetailVO labelDetailVO) {
        validateLabelDetailInfo(labelDetailVO);
        EssayLabelDetail essayLabelDetail = convertLabelDetail(labelDetailVO);
        BiFunction<Long, QuestionLabelDetailVO, List<LabelCommentRelationVO>> saveLabelDetailRemarks =
                adminCommentTemplateService.getSaveLabelDetailRemarks();
        /**
         * 正式写入数据库
         */
        EssayLabelDetail detail = essayLabelDetailRepository.save(essayLabelDetail);
        essayLabelTotalRepository.updateLabeledContentById(labelDetailVO.getLabeledContent(), labelDetailVO.getTotalId());
        questionLabelContentSnapshotService.saveSnapshot(essayLabelDetail);
        List<LabelCommentRelationVO> remarks = saveLabelDetailRemarks.apply(detail.getId(), labelDetailVO);
        /**
         * 转换返回数据结构
         */
        return findQuestionLabelDetail(labelDetailVO.getTotalId(), labelDetailVO.getLabeledContent());
    }

    @Override
    public Object delQuestionDetail(long labelDetailId, String labelContent) {
        EssayLabelDetail one = essayLabelDetailRepository.findOne(labelDetailId);
        one.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus());
        essayLabelDetailRepository.save(one);
        adminCommentTemplateService.delLabelCommentRelation(labelDetailId, TemplateEnum.LabelTypeEnum.QUESTION_DETAIL);
        if (StringUtils.isNotBlank(labelContent)) {
            essayLabelTotalRepository.updateLabeledContentById(labelContent, one.getTotalId());
        }
        return findQuestionLabelDetail(one.getTotalId(), labelContent);
    }

    @Override
    public QuestionLabelTotalVO findTotalInfoById(long totalId) {
        EssayLabelTotal labelTotal = essayLabelTotalRepository.findOne(totalId);
        if (null == labelTotal) {
            throw new BizException(EssayErrors.INVALID_TOTAL_ID);
        }
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(labelTotal.getAnswerId());
        if (null == questionAnswer) {
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        EssayQuestionDetail detail = questionDetailRepository.findById(questionAnswer.getQuestionDetailId());
        QuestionLabelTotalVO labelTotalVO = QuestionLabelTotalVO.builder()
                .id(labelTotal.getId())
                .audioId(null == labelTotal.getAudioId() ? -1 : labelTotal.getAudioId())
                .questionAnswerCardId(labelTotal.getAnswerId())
                .copyRatio(essayLabelService.getCopyRatio().apply(questionAnswer))
                .questionId(questionAnswer.getQuestionBaseId())
                .elseRemark(labelTotal.getElseRemark())
                .totalScore(questionAnswer.getScore())
                .inputWordNumMax(detail.getInputWordNumMax())
                .inputWordNumMin(detail.getInputWordNumMin())
                .articleLevel(labelTotal.getArticleLevel())
                .wordNumScore(labelTotal.getWordNumScore())
                .score(labelTotal.getScore()).build();
        List<LabelCommentRelationVO> remarks = adminCommentTemplateService.findRemarkListById(totalId, TemplateEnum.LabelTypeEnum.QUESTION_TOTAL);
        List<CommentTemplate> commentTemplates = adminCommentTemplateService.findTemplateByIds(remarks.stream().map(i -> i.getTemplateId()).collect(Collectors.toList()));

        handlerLabelTotalRemarks(labelTotalVO, remarks, commentTemplates);
        return labelTotalVO;
    }

    /**
     * 评语数据组装
     */
    public void handlerLabelTotalRemarks(QuestionLabelTotalVO labelTotalVO, List<LabelCommentRelationVO> remarks, List<CommentTemplate> commentTemplates) {
        List<LabelCommentRelationVO> deRemarks = Lists.newArrayList();
        List<LabelCommentRelationVO> addRemark = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(commentTemplates) && CollectionUtils.isNotEmpty(remarks)) {
            for (LabelCommentRelationVO remark : remarks) {
                Optional<CommentTemplate> any = commentTemplates.stream().filter(i -> i.getId() == remark.getTemplateId()).findAny();
                if (!any.isPresent()) {
                    continue;
                }
                CommentTemplate commentTemplate = any.get();
                boolean isDeFlag = commentTemplate.getLabelType() == TemplateEnum.CommentTemplateEnum.KFX.getType();
                if (isDeFlag) {
                    deRemarks.add(remark);
                } else {
                    addRemark.add(remark);
                }
            }
        }
        labelTotalVO.setRemarkList(addRemark);
        labelTotalVO.setDeRemarkList(deRemarks);
    }

    @Override
    public void labelFinish(long totalId) {
        EssayLabelTotal one = essayLabelTotalRepository.findOne(totalId);
        if (null == one) {
            throw new BizException(ErrorResult.create(1002131, "没有批改记录"));
        }
        one.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus());
        one.setGmtModify(new Date());
        EssayQuestionAnswer answer = essayQuestionAnswerService.findById(one.getAnswerId());
        List<EssayLabelDetail> details = essayLabelDetailRepository.findByTotalIdAndStatus(one.getId(), EssayStatusEnum.NORMAL.getCode());

        List<CorrectImage> imageList = correctImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(answer.getId(), EssayStatusEnum.NORMAL.getCode());
        imageList.forEach(image -> {
            if (StringUtils.isEmpty(image.getFinalUrl())) {
                throw new BizException(ErrorResult.create(1031124, "批注图片未绑定完成，请刷新页面后重试"));
            }
        });
        //TODO  判断规则暂定
        if (CollectionUtils.isEmpty(details) && checkIsAnswer(answer)) {
            throw new BizException(ErrorResult.create(1031123, "没有批改详情"));
        }
        essayLabelTotalRepository.save(one);
        //订单完成
        correctOrderService.finished(one.getAnswerId(), EssayAnswerCardEnum.TypeEnum.QUESTION);
    }

    @Override
    public List<EssayLabelDetail> getThesisList(long totalId) {
        EssayLabelTotal total = essayLabelTotalRepository.findOne(totalId);
        if (null == total) {
            return Lists.newArrayList();
        }
        if (total.getLabelFlag().intValue() != LabelFlagEnum.STUDENT_LOOK.getCode()) {
            return essayLabelService.getThesisList(totalId);
        }
        List<EssayLabelDetail> details = essayLabelDetailRepository.findByTotalIdAndStatus(totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(details)) {
            return Lists.newArrayList();
        }
        List<Long> detailIds = details.stream()
                .map(detail -> adminCommentTemplateService.findRemarkListById(detail.getId(), TemplateEnum.LabelTypeEnum.QUESTION_DETAIL))
                .filter(CollectionUtils::isNotEmpty)
                .filter(list -> list.parallelStream().filter(i -> i.getBizType() == TemplateEnum.BizTypeEnum.THESIS.getId()).findAny().isPresent())
                .map(list -> list.get(0).getLabelId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailIds)) {
            return Lists.newArrayList();
        }
        return details.stream().filter(i -> detailIds.contains(i.getId())).collect(Collectors.toList());
    }

    /**
     * 判断用户是否已答题
     *
     * @param answer
     * @return
     */
    private boolean checkIsAnswer(EssayQuestionAnswer answer) {
        Integer correctMode = answer.getCorrectMode();
        CorrectModeEnum correctModeEnum = CorrectModeEnum.create(correctMode);
        if (correctModeEnum == CorrectModeEnum.MANUAL) {
            List<CorrectImage> correctImages = correctImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(answer.getId(),
                    EssayStatusEnum.NORMAL.getCode());
            if (CollectionUtils.isNotEmpty(correctImages)) {
                return true;
            }
        } else if (correctModeEnum == CorrectModeEnum.INTELLIGENCE_2_MANUAL) {
            if (StringUtils.isNotBlank(answer.getContent())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object saveLabelTotal(QuestionLabelTotalVO questionLabelTotalVO) {
        validateLabelTotalInfo(questionLabelTotalVO);
        EssayLabelTotal essayLabelTotal = convertLabelTotal(questionLabelTotalVO);
        BiFunction<Long, QuestionLabelTotalVO, List<LabelCommentRelationVO>> saveLabelTotalRemarks = adminCommentTemplateService.getSaveLabelTotalRemarks();
        /**
         * 正式写入数据库
         */
        Consumer<EssayLabelTotal> fillLabelTotal = (total -> {
            EssayLabelTotal one = essayLabelTotalRepository.findOne(total.getId());
            total.setLabeledContent(one.getLabeledContent());
        });
        fillLabelTotal.accept(essayLabelTotal);
        EssayLabelTotal labelTotal = essayLabelTotalRepository.save(essayLabelTotal);
        List<LabelCommentRelationVO> labelCommentRelations = saveLabelTotalRemarks.apply(labelTotal.getId(), questionLabelTotalVO);
        /**
         * 转换返回数据结构
         */
        BeanUtils.copyProperties(labelTotal, questionLabelTotalVO);
        List<CommentTemplate> commentTemplates = adminCommentTemplateService.findTemplateByIds(labelCommentRelations.stream().map(i -> i.getTemplateId()).collect(Collectors.toList()));
        handlerLabelTotalRemarks(questionLabelTotalVO, labelCommentRelations, commentTemplates);
        return questionLabelTotalVO;
    }

    private void validateLabelTotalInfo(QuestionLabelTotalVO questionLabelTotalVO) {
        validateEmpty(questionLabelTotalVO.getSpendTime(), "批注耗时字段不能为空");
        validateEmpty(questionLabelTotalVO.getCopyRatio(), "答案抄袭率字段不能为空");
        validateEmpty(questionLabelTotalVO.getTotalScore(), "试题总分数字段不能为空");
        validateEmpty(questionLabelTotalVO.getScore(), "阅卷得分字段不能为空");
        validateEmpty(questionLabelTotalVO.getQuestionAnswerCardId(), "答题卡ID不能为空");

        //校验扣分项不能大于总分
        List<LabelCommentRelationVO> deRemarkList = questionLabelTotalVO.getDeRemarkList();
        if (CollectionUtils.isNotEmpty(deRemarkList)) {
            Double sum = deRemarkList.stream().mapToDouble(score -> Math.abs(score.getScore())).sum();
            EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(questionLabelTotalVO.getQuestionAnswerCardId());
            if (null == questionAnswer) {
                return;
            }
            Double totalScore = questionAnswer.getScore();
            log.info("扣分项总分是:{},试题总分是:{}", sum, totalScore);
            if (sum.compareTo(totalScore) > 0) {
                log.info("扣分项总分不能大于试题总分");
                throw new BizException(EssayErrors.KFX_CAN_NOT_MORE_THAN_TOTAL_SCORE);
            }
            //校验 试题得分 不能大于 总分-扣分项
            BigDecimal kfSumDec = new BigDecimal(sum);
            BigDecimal totalDec = new BigDecimal(totalScore);
            Double subtract = totalDec.subtract(kfSumDec).doubleValue();
            log.info("总分-扣分项得分是:{},试题得分是:{}", subtract, questionLabelTotalVO.getScore());
            if (questionLabelTotalVO.getScore().compareTo(subtract) > 0) {
                throw new BizException(EssayErrors.BTYJ_SCORE_CAN_NOT_MORE_THAN_TOTAL_SCORE);
            }
        }
    }

    private void validateEmpty(Object param, String message) {
        boolean flag = false;
        if (param instanceof Number) {
            if (null == param || ((Number) param).doubleValue() < 0) {
                flag = true;
            }
        } else if (param instanceof String) {
            if (StringUtils.isBlank((String) param)) {
                flag = true;
            }
        } else if (param instanceof Collection) {
            if (CollectionUtils.isNotEmpty((Collection) param)) {
                flag = true;
            }
        }


        if (flag) {
            throw new BizException(ErrorResult.create(1000591, message));
        }
    }

    @Override
    public void delQuestionTotal(long totalId) {
        EssayLabelTotal total = essayLabelTotalRepository.findOne(totalId);
        total.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus());
        essayLabelTotalRepository.save(total);
        adminCommentTemplateService.delLabelCommentRelation(totalId, TemplateEnum.LabelTypeEnum.QUESTION_TOTAL);
        List<EssayLabelDetail> details = essayLabelDetailRepository.findByTotalIdAndStatus(totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(details)) {
            for (EssayLabelDetail detail : details) {
                delQuestionDetail(detail.getId(), null);
            }
        }
    }

    /**
     * 查询试题答题卡相关的批注主要信息
     *
     * @param questionAnswerCardId
     * @param commonOperateEnum
     * @return
     */
    @Override
    public QuestionLabelVO getMainLabelInfo(long questionAnswerCardId, CommonOperateEnum commonOperateEnum) {
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(questionAnswerCardId);
        if (null == questionAnswer) {
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        //根据用户答题卡批改方式信息，生成相应的批注
        Integer correctMode = questionAnswer.getCorrectMode();
        CorrectModeEnum correctModeEnum = CorrectModeEnum.create(correctMode);
        /**
         * 对象初始化
         */
        QuestionLabelVO questionLabelVO = initQuestionLabelVO(questionAnswer, correctModeEnum);
        switch (correctModeEnum) {
            case INTELLIGENCE:
                throw new BizException(ErrorResult.create(1000561, "暂不支持智能批改"));
            case MANUAL:
                /**
                 * 人工批改覆盖图片
                 */
                List<CorrectImage> correctImages = correctImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(questionAnswerCardId,
                        EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(correctImages)) {
                    List<CorrectImageVO> collect = correctImages.stream().map(i -> {
                        CorrectImageVO correctImageVO = new CorrectImageVO();
                        BeanUtils.copyProperties(i, correctImageVO);
                        return correctImageVO;
                    }).collect(Collectors.toList());
                    questionLabelVO.setImageInfoList(collect);
                }

            case INTELLIGENCE_2_MANUAL:
                /**
                 * 写入订单信息（如果是套卷下的试题，则订单信息不存在）
                 */
                CorrectOrderSimpleVO correctOrderSimpleVO = findOrderInfoByAnswerCardId(questionAnswer);
                if (null != correctOrderSimpleVO) {
                    if (commonOperateEnum != CommonOperateEnum.READ) {
                        essayTeacherService.validTeacherIsMe(correctOrderSimpleVO.getReceiveOrderTeacher(), "该订单只允许接单人%s执行批改操作");
                    }
                    questionLabelVO.setOrderInfo(correctOrderSimpleVO);
                }
                List<EssayLabelTotal> labelTotals = essayLabelTotalRepository.findByAnswerIdAndStatus(questionAnswerCardId,
                        EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
                if (CollectionUtils.isNotEmpty(labelTotals)) {
                    Optional<EssayLabelTotal> any = labelTotals.stream()
                            .filter(i -> null != i.getLabelFlag())
                            .filter(i -> LabelFlagEnum.STUDENT_LOOK.getCode() == i.getLabelFlag().intValue())
                            .findFirst();
                    if (any.isPresent()) {        //totalId存在
                        EssayLabelTotal essayLabelTotal = any.get();
                        if (essayLabelTotal.getBizStatus() != EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus() &&
                                commonOperateEnum != CommonOperateEnum.READ) {
                            throw new BizException(EssayErrors.ANSWER_CARD_CORRECTED);
                        }
                        questionLabelVO.setTotalId(essayLabelTotal.getId());
                        int bizStatus = essayLabelTotal.getBizStatus();
                        questionLabelVO.setBizStatus(bizStatus);
                        /**
                         * 批注未完成，则耗时=当前时间-创建批注时间；批注完成，耗时=spendTime=批注完成时-创建批注时间
                         */
                        if (bizStatus != EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus() && null != essayLabelTotal.getSpendTime()) {
                            questionLabelVO.setSpendTime(essayLabelTotal.getSpendTime());
                        } else {
                            questionLabelVO.setSpendTime(System.currentTimeMillis() - essayLabelTotal.getGmtCreate().getTime());
                        }
                        QuestionLabelDetailInfoVO questionLabelDetail = findQuestionLabelDetail(essayLabelTotal.getId(), essayLabelTotal.getLabeledContent());
                        questionLabelVO.setDetailList(questionLabelDetail.getLabelDetails());
                        questionLabelVOConsumer.accept(questionLabelVO, questionLabelDetail);
                    }
                }
        }
        if (questionLabelVO.getTotalId() <= 0 && commonOperateEnum != CommonOperateEnum.READ) {      //补充total数据
            EssayLabelTotal total = getInitLabelTotalFunction().apply(questionAnswer, LabelFlagEnum.STUDENT_LOOK);
            questionLabelVO.setTotalId(total.getId());
            questionLabelVO.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
            correctOrderService.startLabel(questionAnswerCardId, EssayAnswerCardEnum.TypeEnum.QUESTION);
        }
        return questionLabelVO;
    }

    @Override
    public BiFunction<EssayQuestionAnswer, LabelFlagEnum, EssayLabelTotal> getInitLabelTotalFunction() {
        return initEssayLabelTotal;
    }

    public BiFunction<EssayQuestionAnswer, LabelFlagEnum, EssayLabelTotal> initEssayLabelTotal = ((questionAnswer, labelFlagEnum) -> {
        EssayLabelTotal build = EssayLabelTotal.builder().copyRatio(questionAnswer.getCopyRatio())
                .inputWordNum(questionAnswer.getInputWordNum())
                .labeledContent(questionAnswer.getContent())
                .labelFlag(LabelFlagEnum.STUDENT_LOOK.getCode())
                .spendTime(0L).build();
        build.setAnswerId(questionAnswer.getId());
        build.setLabelFlag(labelFlagEnum.getCode());
        build.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        build.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
        essayLabelTotalRepository.save(build);
        return build;
    });

    private BiConsumer<QuestionLabelVO, QuestionLabelDetailInfoVO> questionLabelVOConsumer = ((questionLabelVO, detailInfoVO) -> {
        List<QuestionLabelDetailSimpleVO> questionLabelDetails = detailInfoVO.getLabelDetails();
        List<CorrectImageVO> imageInfoList = questionLabelVO.getImageInfoList();
        if (CollectionUtils.isNotEmpty(imageInfoList)) {
            imageInfoList.forEach(i -> i.setImageAllAxis(""));
        }
        if (CollectionUtils.isNotEmpty(questionLabelDetails)) {
            String labelContent = questionLabelVO.getLabelContent();
            questionLabelDetails.sort(Comparator.comparing(QuestionLabelDetailSimpleVO::getId));
            for (QuestionLabelDetailSimpleVO questionLabelDetail : questionLabelDetails) {
                labelContent = questionLabelDetail.getLabeledContent();
                if (null != questionLabelDetail.getImageId() && CollectionUtils.isNotEmpty(imageInfoList)) {
                    Optional<CorrectImageVO> any = imageInfoList.stream().filter(i -> i.getId() == questionLabelDetail.getImageId().longValue())
                            .findAny();
                    if (any.isPresent()) {
                        any.get().setImageAllAxis(questionLabelDetail.getImageAllAxis());
                    }
                }
            }
            if (StringUtils.isNotBlank(labelContent)) {
                questionLabelVO.setLabelContent(labelContent);
            }
        }
    });

    /**
     * 对象初始化（查询批注主要信息专用）
     *
     * @param questionAnswer
     * @param correctMode
     * @return
     */
    private QuestionLabelVO initQuestionLabelVO(EssayQuestionAnswer questionAnswer, CorrectModeEnum correctMode) {
        QuestionLabelVO questionLabelVO = new QuestionLabelVO();
        questionLabelVO.setCorrectMode(correctMode.getMode());
        questionLabelVO.setCorrectModeName(correctMode.getName());
        questionLabelVO.setSpendTime(0);
        questionLabelVO.setQuestionBaseId(questionAnswer.getQuestionBaseId());
        questionLabelVO.setQuestionDetailId(questionAnswer.getQuestionDetailId());
        questionLabelVO.setImageInfoList(Lists.newArrayList());
        questionLabelVO.setContent(questionAnswer.getContent());
        //初始化labelContent
        questionLabelVO.setLabelContent(StringUtils.isBlank(questionAnswer.getContent()) ? Strings.EMPTY : questionAnswer.getContent());
        questionLabelVO.setCopyRatio(essayLabelService.getCopyRatio().apply(questionAnswer));
        questionLabelVO.setTotalId(-1);
        questionLabelVO.setDetailList(Lists.newArrayList());
        return questionLabelVO;
    }

    private QuestionLabelDetailInfoVO findQuestionLabelDetail(long totalId, String labelContent) {
        QuestionLabelDetailInfoVO questionLabelDetailInfoVO = new QuestionLabelDetailInfoVO();
        EssayLabelTotal one = essayLabelTotalRepository.findOne(totalId);
        if (StringUtils.isBlank(labelContent)) {
            labelContent = one.getLabeledContent();
        }
        questionLabelDetailInfoVO.setLabelContent(labelContent);
        questionLabelDetailInfoVO.setLabelDetails(Lists.newArrayList());
        List<EssayLabelDetail> labelDetails = essayLabelDetailRepository.findByTotalIdAndStatus(totalId,
                EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(labelDetails)) {
            List<QuestionLabelDetailSimpleVO> collect = labelDetails.stream().map(i -> {
                QuestionLabelDetailSimpleVO questionLabelDetailSimpleVO = new QuestionLabelDetailSimpleVO();
                BeanUtils.copyProperties(i, questionLabelDetailSimpleVO);
                questionLabelDetailSimpleVO.setLabeledContent(questionLabelDetailInfoVO.getLabelContent());
                return questionLabelDetailSimpleVO;
            }).collect(Collectors.toList());
            questionLabelDetailInfoVO.setLabelDetails(collect);
        }
        return questionLabelDetailInfoVO;
    }

    /**
     * @param answer
     * @return
     */
    private CorrectOrderSimpleVO findOrderInfoByAnswerCardId(EssayQuestionAnswer answer) {
        if (answer.getPaperAnswerId() == 0) {
            CorrectOrder order = correctOrderService.findByAnswerId(answer.getId(), EssayAnswerCardEnum.TypeEnum.QUESTION);
            return TeacherOrderUtil.convertOrderSimpleVO(order);
        } else {
            CorrectOrder order = correctOrderService.findByAnswerId(answer.getPaperAnswerId(), EssayAnswerCardEnum.TypeEnum.PAPER);
            return TeacherOrderUtil.convertOrderSimpleVO(order);
        }
    }


    private EssayLabelTotal convertLabelTotal(QuestionLabelTotalVO questionLabelTotalVO) {
        EssayLabelTotal essayLabelTotal = EssayLabelTotal.builder()
                .elseRemark(questionLabelTotalVO.getElseRemark())
                .totalScore(questionLabelTotalVO.getTotalScore())
                .audioId(questionLabelTotalVO.getAudioId())
                .copyRatio(questionLabelTotalVO.getCopyRatio())
                .spendTime(questionLabelTotalVO.getSpendTime())
                .wordNumScore(questionLabelTotalVO.getWordNumScore())
                .articleLevel(questionLabelTotalVO.getArticleLevel())
                .labelFlag(LabelFlagEnum.STUDENT_LOOK.getCode())
                .articleLevel(questionLabelTotalVO.getArticleLevel())
                .build();
        if (null != questionLabelTotalVO.getScore() && questionLabelTotalVO.getScore() > 0) {
            essayLabelTotal.setScore(questionLabelTotalVO.getScore());
        } else {
            essayLabelTotal.setScore(0D);
        }
        if (null != questionLabelTotalVO.getId() && questionLabelTotalVO.getId() > 0) {
            essayLabelTotal.setId(questionLabelTotalVO.getId());
        }
        if (null != questionLabelTotalVO.getQuestionAnswerCardId()) {
            essayLabelTotal.setAnswerId(questionLabelTotalVO.getQuestionAnswerCardId());
        }
        if (null != questionLabelTotalVO.getQuestionId()) {
            essayLabelTotal.setQuestionId(questionLabelTotalVO.getQuestionId());
        }
        essayLabelTotal.setStatus(EssayStatusEnum.NORMAL.getCode());
        return essayLabelTotal;
    }


    /**
     * 校验详细批注内容
     *
     * @param labelDetailVO
     */
    private void validateLabelDetailInfo(QuestionLabelDetailVO labelDetailVO) {
        Long totalId = labelDetailVO.getTotalId();
        if (null == totalId || totalId <= 0) {
            throw new BizException(EssayErrors.INVALID_TOTAL_ID);
        }
        EssayLabelTotal one = essayLabelTotalRepository.findOne(totalId);
        if (null == one) {
            throw new BizException(EssayErrors.INVALID_TOTAL_ID);
        }
        if (CollectionUtils.isEmpty(labelDetailVO.getRemarkList())) {
            throw new BizException(EssayLabelErrors.NOTHING_LABEL_CONTENT);
        }
        EssayQuestionAnswer answer = essayQuestionAnswerService.findById(one.getAnswerId());
//        int questionType = answer.getQuestionType();
//        if (StringUtils.isBlank(labelDetailVO.getLabeledContent()) && questionType == 5) {
//            throw new BizException(ErrorResult.create(1000732, "议论文批注选中内容不能为空"));
//        }

        List<LabelCommentRelationVO> remarkList = labelDetailVO.getRemarkList();
        if (CollectionUtils.isNotEmpty(remarkList)) {
            Double sum = remarkList.stream().mapToDouble(LabelCommentRelationVO::getScore).sum();
            log.info("批注总分是:{},试卷总分是:{}", sum, answer.getScore());
            if (sum.compareTo(answer.getScore()) > 0) {
                throw new BizException(EssayLabelErrors.SINGLE_SCORE_SUM_MORE_THAN_TOTAL_SCORE);
            }
        }

        labelDetailVO.setTotalId(one.getId());
        labelDetailVO.setQuestionId(one.getQuestionId());
    }

    /**
     * 添加修改详细批注表内容
     *
     * @param labelDetailVO
     * @return
     */
    public EssayLabelDetail convertLabelDetail(QuestionLabelDetailVO labelDetailVO) {
        EssayLabelDetail essayLabelDetail = new EssayLabelDetail();
        essayLabelDetail.setTotalId(labelDetailVO.getTotalId());
        essayLabelDetail.setQuestionId(labelDetailVO.getQuestionId());
        if (StringUtils.isNotBlank(labelDetailVO.getContent())) {
            essayLabelDetail.setContent(labelDetailVO.getContent());
        }
        if (StringUtils.isNotBlank(labelDetailVO.getLabeledContent())) {
            essayLabelDetail.setLabeledContent(labelDetailVO.getLabeledContent());
        }
        if (StringUtils.isNotBlank(labelDetailVO.getElseRemark())) {
            essayLabelDetail.setElseRemark(labelDetailVO.getElseRemark());
        }
        if (null != labelDetailVO.getId() && labelDetailVO.getId() > 0) {
            essayLabelDetail.setId(labelDetailVO.getId());
        }
        if (null != labelDetailVO.getImageId() && labelDetailVO.getImageId() > 0) {
            essayLabelDetail.setImageId(labelDetailVO.getImageId());
        }
        if (StringUtils.isNotBlank(labelDetailVO.getImageAxis())) {
            essayLabelDetail.setImageAxis(labelDetailVO.getImageAxis());
        }
        if (StringUtils.isNotBlank(labelDetailVO.getImageAllAxis())) {
            essayLabelDetail.setImageAllAxis(labelDetailVO.getImageAllAxis());
        }
        essayLabelDetail.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        essayLabelDetail.setAnswerId(labelDetailVO.getQuestionAnswerCardId());
        return essayLabelDetail;
    }
}
