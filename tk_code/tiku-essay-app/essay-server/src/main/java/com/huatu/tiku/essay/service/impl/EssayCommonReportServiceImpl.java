package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.CorrectFeedBackEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.service.EssayCommonReportService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.paper.EssayPaperLabelService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;
import com.huatu.tiku.essay.vo.resp.EssayPaperReportQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.ManualCorrectReportVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/30
 * @描述 报告公共数据查询
 */
@Service
public class EssayCommonReportServiceImpl implements EssayCommonReportService {


    @Autowired
    EssayPaperLabelService essayPaperLabelService;

    @Autowired
    CorrectFeedBackService correctFeedBackService;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayPaperService essayPaperService;

    @Autowired
    EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    BjyHandler bjyHandler;


    /**
     * 套卷～人工批改，添加名师之声 和 综合评语相关
     *
     * @param
     */
    public ManualCorrectReportVo addPaperRemarkList(Long answerId, Integer answerType, String correctRemark) {
        ManualCorrectReportVo reportVo = new ManualCorrectReportVo();
        EssayPaperLabelTotalVo paperLabelMark = essayPaperLabelService.getPaperLabelMark(answerId, LabelFlagEnum.STUDENT_LOOK);
        if (null == paperLabelMark) {
            return null;
        }
        //1.名师之声
        reportVo.setAudioId(paperLabelMark.getAudioId());
        reportVo.setAudioToken(bjyHandler.getToken(paperLabelMark.getAudioId()));
        //2.综合阅卷评语信息
        if (StringUtils.isNotEmpty(correctRemark)) {
            RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
            if (null != remarkListVo) {
                reportVo.setRemarkList(remarkListVo.getPaperRemarkList());
            }
        }
        return reportVo;
    }

    /**
     * 单题～人工批改，添加名师之声 和 综合评语相关
     *
     * @param answerId
     * @param answerType
     * @param correctRemark
     * @return
     */
    public ManualCorrectReportVo addQuestionRemarkList(Long answerId, Integer answerType, String correctRemark) {

        ManualCorrectReportVo reportVo = new ManualCorrectReportVo();
        List<EssayLabelTotal> essayLabelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(essayLabelTotalList)) {
            EssayLabelTotal essayLabelTotal = essayLabelTotalList.stream().findFirst().get();
            int audioId = essayLabelTotal.getAudioId() == null ? -1 : essayLabelTotal.getAudioId();
            reportVo.setAudioId(audioId);
            reportVo.setAudioToken(bjyHandler.getToken(audioId));
            if (StringUtils.isNotEmpty(correctRemark)) {
                RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
                reportVo.setRemarkList(remarkListVo.getQuestionRemarkList());
            }
        }
        return reportVo;
    }

    /**
     * 学员报告评价
     *
     * @param answerCardId
     * @param answerCardId
     */
    public ManualCorrectReportVo addFeedBack(long answerCardId, int answerCardType) {
        ManualCorrectReportVo manualCorrectReportVo = new ManualCorrectReportVo();
        List<CorrectFeedBackVo> correctFeedBackVoList = correctFeedBackService.findByAnswerId(answerCardId, answerCardType);
        if (CollectionUtils.isNotEmpty(correctFeedBackVoList)) {
            manualCorrectReportVo.setFeedBackStatus(CorrectFeedBackEnum.YES.getCode());
            CorrectFeedBackVo correctFeedBackVo = correctFeedBackVoList.get(0);
            manualCorrectReportVo.setFeedBackStar(correctFeedBackVo.getStar());
            manualCorrectReportVo.setFeedBackContent(correctFeedBackVo.getContent());
        } else {
            manualCorrectReportVo.setFeedBackStatus(CorrectFeedBackEnum.NO.getCode());
        }
        return manualCorrectReportVo;
    }


    /**
     * 套卷每个试题的考试情况
     *
     * @param paperAnswerCardId
     * @param paperBaseId
     * @return
     */
    public LinkedList<EssayPaperReportQuestionVO> getPaperQuestionList(Long paperAnswerCardId, Long paperBaseId) {

        //2.1.1 组装基础数据
        //批改报告列表
        LinkedList<EssayPaperReportQuestionVO> questionList = new LinkedList<>();
        //查询学员试题答案
        List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus(paperAnswerCardId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "questionDetailId"));
        //查询试题信息(走缓存,5分钟失效)
        List<AdminSingleQuestionVO> questionInfoList = essayPaperService.findQuestionListByPaper(paperBaseId, true);
        Map<Long, Integer> questionSortMap = questionInfoList.stream()
                .filter(vo -> null != vo)
                .collect(Collectors.toMap(AdminSingleQuestionVO::getId, vo -> vo.getSort()));
        //题目类型（走缓存，永久）
        LinkedList<EssayQuestionTypeVO> questionTypeList = (LinkedList) essaySimilarQuestionService.findQuestionType();
        Map<Long, String> questionTypeMap = questionTypeList.stream()
                .filter(vo -> null != vo)
                .collect(Collectors.toMap(EssayQuestionTypeVO::getId, vo -> vo.getName()));

        questionAnswerList.stream().filter(questionAnswer -> questionSortMap.get(questionAnswer.getQuestionBaseId()) != null).forEach(questionAnswer -> {
            EssayPaperReportQuestionVO reportQuestionVO = EssayPaperReportQuestionVO.builder()
                    .questionBaseId(questionAnswer.getQuestionBaseId())
                    .sort(questionSortMap.get(questionAnswer.getQuestionBaseId()))
                    .examScore(questionAnswer.getExamScore())
                    .type(questionAnswer.getQuestionType())
                    .typeName(questionTypeMap.get(Long.valueOf(questionAnswer.getQuestionType())))
                    .score(questionAnswer.getScore())
                    .spendTime(questionAnswer.getSpendTime())
                    .inputWordNum(questionAnswer.getInputWordNum())
                    .build();
            questionList.add(reportQuestionVO);
        });
        questionList.sort((a, b) -> (a.getSort() - b.getSort()));
        return questionList;
    }


}
