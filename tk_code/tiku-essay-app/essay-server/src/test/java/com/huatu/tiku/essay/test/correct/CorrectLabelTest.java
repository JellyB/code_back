package com.huatu.tiku.essay.test.correct;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.mq.listeners.ManualCorrectFinishListener;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.util.file.Label2AppUtil;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: CorrectLabelTest
 * @description: TODO
 * @date 2019-07-2016:38
 */
public class CorrectLabelTest extends TikuBaseTest {

    private final static Gson gson = new Gson();

    @Autowired
    private EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    private EssayLabelDetailRepository labelDetailRepository;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    ManualCorrectFinishListener manualCorrectFinishListener;
    @Test
    public void test() {
        long labelId = 7161;
        EssayLabelTotal total = essayLabelTotalRepository.findOne(labelId);
        if (total.getBizStatus() == EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus()) {
            return;
        }
        List<EssayLabelDetail> details = labelDetailRepository.findByTotalIdAndStatus(labelId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        System.out.println("total = " + total.getLabeledContent());
        System.out.println("details = " + details.size());
        for (EssayLabelDetail detail : details) {
            System.out.println(detail.getStartPosition() + "|" + detail.getEndPosition() + "|" + detail.getContent());
        }
        String s = Label2AppUtil.label2App(total, details, getCommentInfo());
        System.out.println("s = " + s);
    }

    Function<Long, Map<String, String>> getCommentInfo() {
        return manualCorrectFinishListener::commentInfo;
    }

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    CorrectOrderRepository correctOrderRepository;

    @Test
    public void clearPaperAnswerCard(){
        List<EssayPaperAnswer> all = essayPaperAnswerRepository.findAll();
        for (EssayPaperAnswer answer : all) {
            long id = answer.getId();
            List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndUserIdAndStatus(id,
                    answer.getUserId(),
                    EssayStatusEnum.NORMAL.getCode());
            if(CollectionUtils.isEmpty(answers)){
                CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(answer.getId(), EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                if(null!=correctOrder){
                    correctOrderRepository.delete(correctOrder);
                }
                essayPaperAnswerRepository.delete(answer.getId());
            }
        }
    }


    @Test
    public void clearNoOrderAnswer(){
        List<Integer> modes = Lists.newArrayList(CorrectModeEnum.MANUAL.getMode(),
                CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
        List<EssayPaperAnswer> paperAnswers = essayPaperAnswerRepository.findByCorrectModeIn(modes);
        if(CollectionUtils.isNotEmpty(paperAnswers)){
            for (EssayPaperAnswer paperAnswer : paperAnswers) {
                CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(paperAnswer.getId(),
                        EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                if(null == correctOrder){
                    essayPaperAnswerRepository.delete(paperAnswer.getId());
                }
            }
        }
        List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByCorrectModeIn(modes);
        if(CollectionUtils.isNotEmpty(questionAnswers)){
            for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(questionAnswer.getId(),
                        EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), EssayStatusEnum.NORMAL.getCode());
                if(null == correctOrder){
                    essayQuestionAnswerRepository.delete(questionAnswer.getId());
                }
            }
        }

    }

    @Test
    public void manGetCommentInfo(){
        Map<String, String> stringStringMap = commentInfo(8659L);
        System.out.println("new Gson().toJson(stringStringMap) = " + new Gson().toJson(stringStringMap));
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
        List<Long> templateIds = com.google.common.collect.Lists.newArrayList();
        List<Long> commentIds = com.google.common.collect.Lists.newArrayList();
        StringBuilder description = new StringBuilder();
        double score = 0;
        System.out.println("remarkList = " + gson.toJson(remarkList));
        for (LabelCommentRelationVO labelCommentRelationVO : remarkList) {
            templateIds.add(labelCommentRelationVO.getTemplateId());
            description.append("<templateId>").append(labelCommentRelationVO.getTemplateId()).append("</templateId>");
            commentIds.add(labelCommentRelationVO.getCommentId());
            description.append("<commentId>").append(labelCommentRelationVO.getCommentId()).append("</commentId>");
            if (labelCommentRelationVO.getBizType() == TemplateEnum.BizTypeEnum.REGULAR.getId()) {
                String bizId = labelCommentRelationVO.getBizId();
                if (StringUtils.isBlank(bizId)) {
                    description.append(",");
                    continue;
                }
                score += labelCommentRelationVO.getScore();
                String[] split = bizId.split("[,|，]");
                long count = Arrays.stream(split).filter(NumberUtils::isDigits).count();
                if (count > 0) {
                    commentIds.addAll(Arrays.stream(split).filter(NumberUtils::isDigits).map(Long::parseLong).collect(Collectors.toList()));
                    description.append("(")
                            .append(Arrays.stream(split).filter(NumberUtils::isDigits).map(i -> "<commentId>" + i + "</commentId>").collect(Collectors.joining("、")))
                            .append(")");
                }
            }
            description.append(",");
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
