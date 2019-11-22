package com.huatu.tiku.essay.mq.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.service.CorrectPushService;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.courseExercises.CourseExercisesReportService;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import com.huatu.tiku.essay.service.paper.EssayPaperLabelService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.v2.question.QuestionCorrectDetailService;
import com.huatu.tiku.essay.util.file.Label2AppUtil;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.tiku.essay.vo.video.YunVideoInfo;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.SystemConstant.ESSAY_MANUAL_CORRECT_FINISH_QUEUE;

/**
 * @author huangqingpeng
 * @title: ManualCorrectFinishListener
 * @description: TODO
 * @date 2019-07-2318:39
 */
@Component
@Slf4j
public class ManualCorrectFinishListener {
    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    private EssayLabelDetailRepository labelDetailRepository;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    EssayPaperAnswerService essayPaperAnswerService;

    @Autowired
    QuestionCorrectDetailService questionCorrectDetailService;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayPaperLabelService essayPaperLabelService;

    @Autowired
    BjyHandler bjyHandler;

    @Autowired
    CorrectPushService correctPushService;

    @Autowired
    CourseExercisesReportService courseExercisesReportService;

    @RabbitListener(queues = ESSAY_MANUAL_CORRECT_FINISH_QUEUE, containerFactory = "rabbitFactory")
    public void onMessage(Message message) {
        try {
            Map finishMap = (Map) messageConverter.fromMessage(message);
            Integer answerType = MapUtils.getInteger(finishMap, "answerType", -1);
            Long answerId = MapUtils.getLong(finishMap, "answerId");
            log.info("完成批改:answerId={},answerType={}", answerId, answerType);
            if (EssayAnswerCardEnum.TypeEnum.PAPER.getType() == answerType.intValue()) {
                EssayPaperAnswer paperAnswer = essayPaperAnswerService.findById(answerId);
                if (paperAnswer.getStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
                    return;
                }
                List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerService.findByAnswerId(answerId);
                double score = 0D;
                int spendTime = 0;
                boolean isQuestionVideoConvert = true;
                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                    boolean b = assemblingManualCorrectCommentContent(questionAnswer);
                    if (!b) {
                        isQuestionVideoConvert = false;
                    }
                    score += questionAnswer.getExamScore();
                    spendTime += questionAnswer.getSpendTime();
                }
                if (StringUtils.isBlank(paperAnswer.getCorrectRemark())) {
                    String paperRemark = getPaperRemark(answerId, LabelFlagEnum.STUDENT_LOOK);
                    if (StringUtils.isNotEmpty(paperRemark)) {
                        paperAnswer.setCorrectRemark(paperRemark);
                    }
                }
                EssayPaperLabelTotal paperLabelTotal = essayPaperLabelService.findByPaperAnswerId(paperAnswer.getId(), LabelFlagEnum.STUDENT_LOOK);
                paperAnswer.setCorrectDate(paperLabelTotal.getGmtModify());
                boolean isConvert = paperLabelTotal.getAudioId() <= 0 || checkoutVideoStatus(paperLabelTotal.getAudioId());      //是否转码
                log.info("调用消息推送 - paper isConvert:{}, isQuestionVideoConvert:{}, answerId:{}, answerType:{}", isConvert, isQuestionVideoConvert, answerId, answerType);
                paperAnswer.setExamScore(score);
                paperAnswer.setSpendTime(spendTime);
                if (isConvert && isQuestionVideoConvert) {
                    paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
                    courseExercisesReportService.addPaperCourseReport(paperAnswer);
                    essayPaperLabelService.updateLabelStatus(paperLabelTotal, EssayLabelStatusConstant.EssayLabelBizStatusEnum.FINISH);
                    correctPushService.correctReport4Push(answerId,answerType);
                    paperAnswer.setGmtModify(new Date());
                }
                log.info("完成试卷答题卡批改{} && {},答题卡ID是:{},更新批改信息:{}",isConvert , isQuestionVideoConvert ,paperAnswer.getId(), JsonUtil.toJson(paperAnswer));
                essayPaperAnswerService.save(paperAnswer);
            } else if (EssayAnswerCardEnum.TypeEnum.QUESTION.getType() == answerType.intValue()) {
                EssayQuestionAnswer answer = essayQuestionAnswerRepository.findOne(answerId);
                if (null == answer) {
                    log.info("答题卡不存在或者已回收！{},删除相关批注！！！！",answerId);
                    List<EssayLabelTotal> labelTotals = essayLabelTotalRepository.findByAnswerIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
                    for (EssayLabelTotal labelTotal : labelTotals) {
                        labelTotal.setStatus(EssayStatusEnum.DELETED.getCode());
                        essayLabelTotalRepository.save(labelTotals);
                    }
                    throw new BizException(ErrorResult.create(1021321, "答题卡不存在"));
                }
                assemblingManualCorrectCommentContent(answer);
            }

        } catch (MessageConversionException e) {
            log.error("convert error，data={}", message, e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch (Exception e) {
            log.error("deal message error，data={}", message, e);
        }
    }

    /**
     * 是否转码成功
     *
     * @param videoId
     * @return
     */
    private boolean checkoutVideoStatus(int videoId) {
        if (videoId == 0) {
            return true;
        }
        YunVideoInfo yunVideoInfo = bjyHandler.getYunVideoInfo(videoId);
        if (null != yunVideoInfo) {
            Integer videoStatus = yunVideoInfo.getVideoStatus();
            if (null != videoStatus && videoStatus.intValue() == 100) {
                return true;
            }
        }
        return false;
    }


    public String getPaperRemark(long answerId, LabelFlagEnum labelFlagEnum) {
        EssayPaperLabelTotal paperLabelTotal = essayPaperLabelService.findByPaperAnswerId(answerId, labelFlagEnum);
        log.info("综合阅卷paperLabelTotal是:{}", paperLabelTotal);
        if (null != paperLabelTotal) {
            //更新试卷评价
            RemarkListVo paperRemarkList = questionCorrectDetailService.getPaperRemarkList(EssayAnswerCardEnum.TypeEnum.PAPER.getType(), paperLabelTotal.getId(), paperLabelTotal.getElseRemark());
            if (null != paperRemarkList && CollectionUtils.isNotEmpty(paperRemarkList.getPaperRemarkList())) {
                String remarkContent = JsonUtil.toJson(paperRemarkList);
                log.info("试卷ID是:{},评价内容是:{}", answerId, remarkContent);
                return remarkContent;
            }
        }
        return null;
    }

    public boolean assemblingManualCorrectCommentContent(EssayQuestionAnswer questionAnswer) {
        if (null == questionAnswer) {
            log.info("答题卡不存在或者已回收！");
            throw new BizException(ErrorResult.create(1021321, "答题卡不存在"));
        }
        if (questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() &&
                StringUtils.isNotBlank(questionAnswer.getCorrectedContent())) {
            return true;
        }
        long id = questionAnswer.getId();
        List<EssayLabelTotal> totals = essayLabelTotalRepository.findByAnswerIdAndStatusAndLabelFlag(id,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                LabelFlagEnum.STUDENT_LOOK.getCode());
        if (CollectionUtils.isEmpty(totals)) {
            throw new BizException(ErrorResult.create(1002131, "没有批改记录"));
        }
        EssayLabelTotal total = totals.get(0);

        long labelId = total.getId();
        if (total.getBizStatus() == EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus()) {
            throw new BizException(ErrorResult.create(1002131, "答题卡批注状态不对"));
        }
        List<EssayLabelDetail> details = labelDetailRepository.findByTotalIdAndStatus(labelId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        String s = Strings.EMPTY;
        if (CollectionUtils.isNotEmpty(details)) {
            s = Label2AppUtil.label2App(total, details, this::commentInfo);
        } else if (StringUtils.isNotBlank(total.getLabeledContent())) {
            s = Strings.EMPTY;
        }
        s = s.replaceAll("<[/]?br>","\n");
        System.out.println("s = " + s);
        String elseRemark = total.getElseRemark();
        RemarkListVo questionRemarkList = questionCorrectDetailService.getQuestionRemarkListInfo(EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), labelId, elseRemark);
        if (CollectionUtils.isNotEmpty(questionRemarkList.getQuestionRemarkList()) || CollectionUtils.isNotEmpty(questionRemarkList.getDeRemarkList())) {
            String correctRemark = JsonUtil.toJson(questionRemarkList);
            //更新试题评语
            questionAnswer.setCorrectRemark(correctRemark);
        }
        //修改答题卡批改状态为已批改
        boolean flag = false;
        Double score = total.getScore() == null ? 0D : total.getScore();
        questionAnswer.setExamScore(score);
        if (null == total.getAudioId() || checkoutVideoStatus(total.getAudioId())) {      //如果没有视频，不做视频转码判断
            questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
            questionAnswer.setGmtModify(new Date());
            total.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.FINISH.getBizStatus());
            courseExercisesReportService.addQuestionCourseReport(questionAnswer);
            essayLabelTotalRepository.save(total);
            if(questionAnswer.getPaperAnswerId() <= 0){     //试卷答题卡ID为0则表示为单题答题卡，不是套题的答题卡
                correctPushService.correctReport4Push(questionAnswer.getId(),EssayAnswerCardEnum.TypeEnum.QUESTION.getType());
            }
            flag = true;
        }

        questionAnswer.setCorrectedContent(s.replaceAll("&nbsp;"," "));
        questionAnswer.setCorrectDate(total.getGmtModify());
        log.info("完成试题答题卡批改{},答题卡ID是:{},更新批改信息:{}", flag, id, JsonUtil.toJson(questionAnswer));
        essayQuestionAnswerRepository.save(questionAnswer);
        return flag;
    }


    public Map<String, String> commentInfo(Long detailId) {
        Map<String, String> result = Maps.newHashMap();
        QuestionLabelDetailVO detailVO = essayQuestionLabelService.findDetailById(detailId);
        if (null == detailId) {
            return result;
        }
        List<LabelCommentRelationVO> remarkList = detailVO.getRemarkList();
        if (CollectionUtils.isEmpty(remarkList)) {
            return result;
        }
        List<Long> templateIds = Lists.newArrayList();
        List<Long> commentIds = Lists.newArrayList();
        StringBuilder description = new StringBuilder();
        double score = 0;
        for (LabelCommentRelationVO labelCommentRelationVO : remarkList) {
            score += labelCommentRelationVO.getScore();
            templateIds.add(labelCommentRelationVO.getTemplateId());
            description.append("<templateId>").append(labelCommentRelationVO.getTemplateId()).append("</templateId>");
            commentIds.add(labelCommentRelationVO.getCommentId());
            description.append("<commentId>").append(labelCommentRelationVO.getCommentId()).append("</commentId>");
            if (labelCommentRelationVO.getBizType() == TemplateEnum.BizTypeEnum.REGULAR.getId()) {
                String bizId = labelCommentRelationVO.getBizId();
                if (StringUtils.isBlank(bizId)) {
                    description.append("，");
                    continue;
                }
                String[] split = bizId.split("[,|，]");
                long count = Arrays.stream(split).filter(NumberUtils::isDigits).count();
                if (count > 0) {
                    commentIds.addAll(Arrays.stream(split).filter(NumberUtils::isDigits).map(Long::parseLong).collect(Collectors.toList()));
                    description.append("(")
                            .append(Arrays.stream(split).filter(NumberUtils::isDigits).map(i -> "<commentId>" + i + "</commentId>").collect(Collectors.joining("、")))
                            .append(")");
                }
            }
            description.append("，");
        }
        String elseRemark = detailVO.getElseRemark();
        if (StringUtils.isNotBlank(elseRemark)) {
            description.append(elseRemark);
        } else {
            description.deleteCharAt(description.length() - 1);
        }
        List<CommentTemplate> templateList = adminCommentTemplateService.findTemplateByIds(templateIds);
        List<CommentTemplateDetail> commentList = adminCommentTemplateService.findCommentByIds(commentIds);
        boolean underLine = isUnderLine(templateList);
        boolean highLight = isHighLight(templateList);
        result.put("score", String.valueOf(score));
        result.put("underLine", String.valueOf(underLine));
        result.put("highLight", String.valueOf(highLight));
        Pattern pattern = Pattern.compile("<[^>]+Id>([0-9]+)</[^>]+Id>");
        Matcher matcher = pattern.matcher(description);
        int start = 0;
        while (matcher.find(start)) {
            String group = matcher.group();
            String id = matcher.group(1);
            String name = "";
            if (group.indexOf("templateId") > -1) {
                Optional<CommentTemplate> first = templateList.stream().filter(i -> i.getId() == Long.parseLong(id)).findFirst();
                if (first.isPresent()) {
                    name = first.get().getName();
                }
            } else if (group.indexOf("commentId") > -1) {
                Optional<CommentTemplateDetail> first = commentList.stream().filter(i -> i.getId() == Long.parseLong(id)).findFirst();
                if (first.isPresent()) {
                    name = first.get().getContent();
                }
            }
            description.replace(matcher.start(), matcher.end(), name);
            start = matcher.start();
        }
        result.put("description", description.toString());
        return result;
    }


    //TODO 之后补充对应关系（非必要）
    private boolean isHighLight(List<CommentTemplate> templateList) {
        return false;
    }

    //TODO 之后补充对应关系（非必要）
    private boolean isUnderLine(List<CommentTemplate> templateList) {
        return true;
    }
}
