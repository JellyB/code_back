package com.huatu.tiku.essay.service.impl.paper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.BaseEntity;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.entity.correct.LabelCommentRelation;
import com.huatu.tiku.essay.essayEnum.CommonOperateEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.v2.EssayLabelCommentRelationRepository;
import com.huatu.tiku.essay.repository.v2.EssayPaperLabelTotalRepository;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.impl.correct.TeacherOrderUtil;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import com.huatu.tiku.essay.service.paper.EssayPaperLabelService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.vo.admin.answer.AdminPaperAnswerSimpleVO;
import com.huatu.tiku.essay.vo.admin.answer.AdminQuestionAnswerSimpleVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSimpleVO;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;

@Service
public class EssayPaperLabelServiceImpl implements EssayPaperLabelService {

    private static final Logger logger = LoggerFactory.getLogger(EssayPaperLabelServiceImpl.class);

    @Autowired
    EssayPaperLabelTotalRepository essayPaperLabelTotalRepository;

    @Autowired
    EssayLabelCommentRelationRepository labelCommentRelationRepository;

    @Autowired
    EssayQuestionService essayQuestionService;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    CorrectOrderService correctOrderService;
    @Autowired
    EssayPaperAnswerService essayPaperAnswerService;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    QuestionCorrectDetailService correctDetailService;

    @Autowired
    EssayTeacherService essayTeacherService;


    @Autowired
    EssayPaperLabelService paperLabelService;


    @Override
    public EssayPaperLabelTotal findByPaperAnswerId(Long paperAnswerId, LabelFlagEnum labelFlagEnum) {
        EssayPaperLabelTotal paperLabelTotal = essayPaperLabelTotalRepository.findByAnswerIdAndStatusAndLabelFlag(
                paperAnswerId,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                labelFlagEnum.getCode());
        return paperLabelTotal;
    }


    /**
     * 查询--套卷阅卷批注
     *
     * @param paperAnswerCardId
     * @param labelFlagEnum
     * @return
     */
    @Override
    public EssayPaperLabelTotalVo getPaperLabelMark(long paperAnswerCardId, LabelFlagEnum labelFlagEnum) {

        //套题阅卷批注
        EssayPaperLabelTotal essayPaperLabelTotal = findByPaperAnswerId(paperAnswerCardId, labelFlagEnum);
        if (null == essayPaperLabelTotal) {
            return new EssayPaperLabelTotalVo();
        }
        EssayPaperLabelTotalVo essayPaperLabelTotalVo = new EssayPaperLabelTotalVo();
        BeanUtils.copyProperties(essayPaperLabelTotal, essayPaperLabelTotalVo);
        //评语信息
        List<LabelCommentRelation> labelCommentRelationList = labelCommentRelationRepository.findByLabelIdAndTypeAndStatus(essayPaperLabelTotal.getId(),
                TemplateEnum.LabelTypeEnum.PAPER_TOTAL.getType(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
        List<LabelCommentRelationVO> labelCommentRelationVOS = adminCommentTemplateService.convertRelation2VO(labelCommentRelationList);

        essayPaperLabelTotalVo.setRemarkList(labelCommentRelationVOS);
        //计算试卷总分
        essayPaperLabelTotalVo.setPaperScore(getPaperSumScore(paperAnswerCardId));
        return essayPaperLabelTotalVo;
    }

    /**
     * 获取试题批改基础信息
     *
     * @param paperAnswerCardId
     * @return
     */
    public Double getPaperSumScore(long paperAnswerCardId) {
        List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus(paperAnswerCardId,
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                new Sort(Sort.Direction.ASC, "id")

        );

        Double sumScore = 0D;
        if (CollectionUtils.isNotEmpty(questionAnswerList)) {
            List<Double> questionScore = questionAnswerList.stream().map(EssayQuestionAnswer::getScore).collect(Collectors.toList());
            for (Double score : questionScore) {
                BigDecimal bigDecimalFirstScore = new BigDecimal(score.toString());
                BigDecimal bigDecimalSecondScore = new BigDecimal(sumScore.toString());
                sumScore = bigDecimalSecondScore.add(bigDecimalFirstScore).doubleValue();
            }
        }

        return sumScore;
    }

    /**
     * 保存--套卷阅卷批注
     *
     * @return
     */
    @Override
    @Transactional
    public void save(EssayPaperLabelTotalVo paperLabelTotalVo, LabelFlagEnum labelFlagEnum) {

        //保存其他批注
        EssayPaperLabelTotal essayPaperLabelTotal = EssayPaperLabelTotal.builder()
                .answerId(paperLabelTotalVo.getPaperAnswerCardId())
                .audioId(paperLabelTotalVo.getAudioId())
                .elseRemark(paperLabelTotalVo.getElseRemark())
                .paperId(paperLabelTotalVo.getPaperId())
                .labelFlag(labelFlagEnum.getCode())
                .build();
        essayPaperLabelTotal.setId(paperLabelTotalVo.getId());
        essayPaperLabelTotal.setStatus(EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
        essayPaperLabelTotalRepository.save(essayPaperLabelTotal);

        List<LabelCommentRelationVO> remarkList = paperLabelTotalVo.getRemarkList();
        if (CollectionUtils.isEmpty(remarkList)) {
            return;
        }

        //保存评语
        adminCommentTemplateService.delLabelCommentRelation(essayPaperLabelTotal.getId(),
                TemplateEnum.LabelTypeEnum.PAPER_TOTAL);
        List<LabelCommentRelation> collect = remarkList.stream().map(remark -> {
            LabelCommentRelation labelCommentRelation = LabelCommentRelation.builder()
                    .commentId(remark.getCommentId())
                    .labelId(essayPaperLabelTotal.getId())
                    .bizId(remark.getBizId())
                    .bizType(remark.getBizType())
                    .type(TemplateEnum.LabelTypeEnum.PAPER_TOTAL.getType())
                    .score(remark.getScore())
                    .templateId(remark.getTemplateId())
                    .build();
            labelCommentRelation.setId(remark.getId());
            return labelCommentRelation;
        }).collect(Collectors.toList());
        labelCommentRelationRepository.save(collect);
    }

    /**
     * 删除--套卷阅卷批注
     *
     * @return
     */
    @Override
    public void delete(long labelId) {
        EssayPaperLabelTotal essayPaperLabelTotal = essayPaperLabelTotalRepository.findOne(labelId);
        if (null != essayPaperLabelTotal) {
            essayPaperLabelTotalRepository.deletePaperLabel(labelId);
            //删除评语
            labelCommentRelationRepository.updateStatusByLabelId(essayPaperLabelTotal.getId(),
                    TemplateEnum.LabelTypeEnum.PAPER_TOTAL.getType(),
                    EssayStatusEnum.DELETED.getCode());
        }
    }


    @Override
    public Object getMainLabelInfo(long paperAnswerCardId, CommonOperateEnum commonOperateEnum, LabelFlagEnum labelFlagEnum) {
        HashMap<Object, Object> result = Maps.newHashMap();
        CorrectOrderSimpleVO correctOrderSimpleVO = findOrderInfoByAnswerCardId(paperAnswerCardId);
        if (commonOperateEnum != CommonOperateEnum.READ) {
            essayTeacherService.validTeacherIsMe(correctOrderSimpleVO.getReceiveOrderTeacher(), "该订单只允许接单人%s执行批改操作");
        }
        result.put("orderInfo", correctOrderSimpleVO);
        EssayPaperAnswer paperAnswer = essayPaperAnswerService.findById(paperAnswerCardId);
        if (null == paperAnswer) {
            throw new BizException(EssayErrors.NO_ANSWER_MSG_IN_REDIS);
        }
        EssayPaperLabelTotal paperLabelTotal = findByPaperAnswerId(paperAnswerCardId, labelFlagEnum);
        if (null != paperLabelTotal &&
                paperLabelTotal.getBizStatus() != EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus() &&
                commonOperateEnum != CommonOperateEnum.READ) {
            throw new BizException(EssayErrors.ANSWER_CARD_CORRECTED);
        }
        List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerService.findByAnswerId(paperAnswerCardId);
        List<AdminQuestionAnswerSimpleVO> adminQuestionAnswerSimpleVOS = Lists.newArrayList();
        long total = 0L;

        //试题基础信息
        if (CollectionUtils.isNotEmpty(questionAnswers)) {
            List<EssayLabelTotal> labelTotals = essayQuestionLabelService.findTotalByAnswerIds(
                    questionAnswers.stream().map(BaseEntity::getId).collect(Collectors.toList()),
                    LabelFlagEnum.STUDENT_LOOK);

            for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                long id = questionAnswer.getId();
                long questionBaseId = questionAnswer.getQuestionBaseId();
                long questionDetailId = questionAnswer.getQuestionDetailId();
                AdminQuestionAnswerSimpleVO adminQuestionAnswerSimpleVO = AdminQuestionAnswerSimpleVO.builder()
                        .questionAnswerCardId(id)
                        .questionBaseId(questionBaseId)
                        .questionDetailId(questionDetailId)
                        .totalId(-1)//
                        .build();
                adminQuestionAnswerSimpleVOS.add(adminQuestionAnswerSimpleVO);
                if (CollectionUtils.isNotEmpty(labelTotals)) {
                    Optional<EssayLabelTotal> any = labelTotals.stream().filter(i -> i.getAnswerId().equals(id)).findAny();
                    if (any.isPresent()) {
                        EssayLabelTotal essayLabelTotal = any.get();
                        adminQuestionAnswerSimpleVO.setTotalId(essayLabelTotal.getId());
                        adminQuestionAnswerSimpleVO.setBizStatus(essayLabelTotal.getBizStatus());
                        total += (null == essayLabelTotal.getSpendTime() ? 0 : essayLabelTotal.getSpendTime());
                    }
                }
                if (adminQuestionAnswerSimpleVO.getTotalId() < 0 && commonOperateEnum != CommonOperateEnum.READ) {
                    BiFunction<EssayQuestionAnswer, LabelFlagEnum, EssayLabelTotal> initLabelTotalFunction = essayQuestionLabelService.getInitLabelTotalFunction();
                    EssayLabelTotal apply = initLabelTotalFunction.apply(questionAnswer, LabelFlagEnum.STUDENT_LOOK);
                    if (null != apply) {
                        adminQuestionAnswerSimpleVO.setTotalId(apply.getId());
                        adminQuestionAnswerSimpleVO.setBizStatus(apply.getBizStatus());
                    }
                }
            }
        }

        //试卷基础信息
        AdminPaperAnswerSimpleVO adminPaperAnswerSimpleVO = AdminPaperAnswerSimpleVO.builder()
                .paperAnswerCardId(paperAnswerCardId)
                .paperBaseId(paperAnswer.getPaperBaseId())
                .questionAnswerCards(adminQuestionAnswerSimpleVOS)
                .build();
        if (null != paperLabelTotal) {
            adminPaperAnswerSimpleVO.setTotalId(paperLabelTotal.getId());
            adminPaperAnswerSimpleVO.setBizStatus(paperLabelTotal.getBizStatus());
        } else if (commonOperateEnum != CommonOperateEnum.READ) {
            EssayPaperLabelTotal essayPaperLabelTotal = initPaperLabel(paperAnswer, LabelFlagEnum.STUDENT_LOOK);
            adminPaperAnswerSimpleVO.setTotalId(essayPaperLabelTotal.getId());
            adminPaperAnswerSimpleVO.setBizStatus(essayPaperLabelTotal.getBizStatus());
            correctOrderService.startLabel(paperAnswerCardId, EssayAnswerCardEnum.TypeEnum.PAPER);
        }
        result.put("paperAnswerCard", adminPaperAnswerSimpleVO);
        result.put("spendTime", total);
        //订单信息
        return result;
    }

    private EssayPaperLabelTotal initPaperLabel(EssayPaperAnswer paperAnswer, LabelFlagEnum labelFlagEnum) {
        EssayPaperLabelTotal build = EssayPaperLabelTotal.builder()
                .answerId(paperAnswer.getId())
                .elseRemark(Strings.EMPTY)
                .paperId(paperAnswer.getPaperBaseId())
                .build();
        build.setLabelFlag(labelFlagEnum.getCode());
        build.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        build.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
        essayPaperLabelTotalRepository.save(build);
        return build;
    }

    @Override
    public void labelFinish(long labelId) {
        EssayPaperLabelTotal one = essayPaperLabelTotalRepository.findOne(labelId);
        long answerId = one.getAnswerId();
        List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus(answerId, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));
        if (CollectionUtils.isEmpty(answers)) {
            throw new BizException(ErrorResult.create(1000550, "不存在试题答题卡"));
        }
        List<EssayLabelTotal> totals = essayQuestionLabelService.findTotalByAnswerIds(answers.stream().map(BaseEntity::getId).collect(Collectors.toList()), LabelFlagEnum.STUDENT_LOOK);
        if (CollectionUtils.isEmpty(totals) ||       //无批注信息
                totals.size() < answers.size() ||          //学员相关批注信息小于试题答题卡数量
                totals.stream().filter(i -> i.getBizStatus() == EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus()).findAny().isPresent()) {    //有批注状态为未完成
            logger.info("存在未完成的试题批注--答题卡数量:{},批注对象:{}", answers.size(), new Gson().toJson(totals));
            throw new BizException(ErrorResult.create(1000550, "存在未完成的试题批注"));
        }
        CorrectOrder order = correctOrderService.findByAnswerId(answerId, EssayAnswerCardEnum.TypeEnum.PAPER);
        if (null == order) {
            throw new BizException(ErrorResult.create(1000550, "无关联的订单信息"));
        }
        one.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus());
		one.setGmtModify(new Date());
        essayPaperLabelTotalRepository.save(one);
        correctOrderService.finished(answerId, EssayAnswerCardEnum.TypeEnum.PAPER);
    }

    @Override
    public void updateLabelStatus(EssayPaperLabelTotal paperLabelTotal, EssayLabelStatusConstant.EssayLabelBizStatusEnum finish) {
        paperLabelTotal.setBizStatus(finish.getBizStatus());
        essayPaperLabelTotalRepository.save(paperLabelTotal);
    }

    /**
     * 试卷答题卡ID 查询订单信息
     *
     * @param paperAnswerCardId
     * @return
     */
    private CorrectOrderSimpleVO findOrderInfoByAnswerCardId(long paperAnswerCardId) {
        CorrectOrder order = correctOrderService.findByAnswerId(paperAnswerCardId, EssayAnswerCardEnum.TypeEnum.PAPER);
        return TeacherOrderUtil.convertOrderSimpleVO(order);
    }


}
