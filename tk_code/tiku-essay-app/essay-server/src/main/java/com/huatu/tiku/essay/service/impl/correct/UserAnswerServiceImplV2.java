package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.service.correct.UserAnswerServiceV2;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.resp.EssayAnswerV2VO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;


/**
 * 描述：答题卡记录 v2 service
 *
 * @author biguodong
 * Create time 2019-07-11 4:51 PM
 **/

@Service
@Slf4j
public class UserAnswerServiceImplV2 implements UserAnswerServiceV2 {


    @Autowired
    private EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;

    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    private CorrectOrderRepository correctOrderServiceRepository;

    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    private EssaySimilarQuestionRepository essaySimilarQuestionRepository;

    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;

    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;

    @Value("${paper_report_start_time}")
    private long paperReportStartTime;

    @Value("${default_correct_return_memo}")
    private String defaultCorrectMemo;

    private String correctMemo = "本次人工批改申请因“%s”被驳回，申请时消耗的批改次数已退回账户，如需继续申请批改请修改后重新提交。";

    /**
     * 我的套题 - 批改记录
     *
     * @param userId
     * @param pageRequest
     * @param modeTypeEnum
     * @return
     */
    @Override
    public List<EssayAnswerV2VO> correctPaperList(int userId, Pageable pageRequest, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        List<EssayAnswerV2VO> list = Lists.newArrayList();
        LinkedList<Integer> bizStatusList = new LinkedList<>();

        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());

        List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndStatusAndAnswerCardTypeAndTypeAndBizStatusIn
                (userId,
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType(),
                        AdminPaperConstant.TRUE_PAPER, bizStatusList, pageRequest);

        for (EssayPaperAnswer paperAnswer : paperAnswerList) {
            EssayAnswerV2VO answerVO = new EssayAnswerV2VO();
            answerVO.setPaperName(paperAnswer.getName());
            answerVO.setPaperId(paperAnswer.getPaperBaseId());//base试卷id
            answerVO.setCorrectDate(DateUtil.convertDateFormat(paperAnswer.getCorrectDate()));//批改时间
            answerVO.setExamScore(paperAnswer.getExamScore());//学员得分
            answerVO.setScore(paperAnswer.getScore());//试卷总分
            answerVO.setAnswerId(paperAnswer.getId());
            answerVO.setBizStatus(paperAnswer.getBizStatus());//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
            answerVO.setPaperReportFlag((null != paperAnswer.getCorrectDate() && paperAnswer.getCorrectDate().getTime() > paperReportStartTime) ? true : false);
            answerVO.setCorrectMode(paperAnswer.getCorrectMode());
            /**
             * 如果批注被驳回，查询订单信息获取驳回信息
             */
            if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus()) {
                CorrectOrder correctOrder = correctOrderServiceRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(paperAnswer.getId(),
                        EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                if (null != correctOrder) {
                    answerVO.setCorrectMemo(null == correctOrder.getCorrectMemo() ? StringUtils.EMPTY : String.format(correctMemo, correctOrder.getCorrectMemo()));
                    log.info("驳回原因¬是:{}", String.format(correctMemo, correctOrder.getCorrectMemo()));
                } else {
                    answerVO.setCorrectMemo(StringUtils.EMPTY);
                }
            }
            if ((paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() || paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECTING.getBizStatus()) && paperAnswer.getCorrectMode() == CorrectModeEnum.MANUAL.getMode()) {
                CorrectOrder correctOrder = correctOrderServiceRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(paperAnswer.getId(), EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
                if (null != correctOrder) {
                    answerVO.setClickContent(TeacherOrderTypeEnum.reportContent(TeacherOrderTypeEnum.SET_QUESTION, correctOrder.getDelayStatus()));
                }
            }
            //所属地区
            EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
            if (essayPaperBase != null) {
                answerVO.setAreaId(essayPaperBase.getAreaId());//地区id
                answerVO.setAreaName(essayPaperBase.getAreaName());//地区名称
                answerVO.setPaperType(essayPaperBase.getType());//试卷类型
            }
            answerVO.setVideoAnalyzeFlag(essayPaperBase.getVideoAnalyzeFlag());
            list.add(answerVO);
        }
        return list;
    }


    /**
     * 套题批改 count
     *
     * @param userId
     * @param modeTypeEnum
     * @return
     */
    @Override
    public long countCorrectPaperList(int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());


        return essayPaperAnswerRepository.countByUserIdAndStatusAndAnswerCardTypeAndBizStatusIn(userId,
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                modeTypeEnum.getType(),
                bizStatusList);
    }


    @Override
    public List<EssayAnswerV2VO> questionCorrectList(int userId, Integer type, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum, Pageable pageRequest) {
        List<EssayAnswerV2VO> list = new LinkedList<EssayAnswerV2VO>();

        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());
        //查询EssayQuestionAnswer表
        List<EssayQuestionAnswer> questionAnswerList = ListUtils.EMPTY_LIST;
        if (type == QuestionTypeConstant.SINGLE_QUESTION) {
            //type不是5
            questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeNotAndStatusAndBizStatusInAndAnswerCardType
                    (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            bizStatusList, modeTypeEnum.getType(), pageRequest);
        } else if (type == QuestionTypeConstant.ARGUMENTATION) {
            //type是5
            questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndQuestionTypeAndStatusAndBizStatusInAndAnswerCardType
                    (userId, 0, 5, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), bizStatusList,
                            modeTypeEnum.getType(),pageRequest);
        }
        //遍历题目
        for (EssayQuestionAnswer questionAnswer : questionAnswerList) {
            EssayAnswerV2VO answerVO = new EssayAnswerV2VO();
            answerVO.setAnswerId(questionAnswer.getId());
            answerVO.setQuestionDetailId(questionAnswer.getQuestionDetailId());//detail试题id
            answerVO.setQuestionBaseId(questionAnswer.getQuestionBaseId());//base试题id
            answerVO.setCorrectDate(DateUtil.convertDateFormat(questionAnswer.getCorrectDate()));//批改时间
            answerVO.setExamScore(questionAnswer.getExamScore());//学员得分
            answerVO.setScore(questionAnswer.getScore());//题目总分
            answerVO.setAnswerId(questionAnswer.getId());//答题卡id
            answerVO.setBizStatus(questionAnswer.getBizStatus());
            answerVO.setCorrectMode(questionAnswer.getCorrectMode() == null ? EssayCorrectGoodsConstant.CorrectTypeEnum.INTELLIGENCE.getType() : questionAnswer.getCorrectMode());
            answerVO.setPaperReportFlag(true);
            /**
             * 如果批注被驳回，查询订单信息获取驳回信息
             */
            if (questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus()) {
                CorrectOrder correctOrder = correctOrderServiceRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(questionAnswer.getId(),
                        EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), EssayStatusEnum.NORMAL.getCode());

                if (null != correctOrder && StringUtils.isNotEmpty(correctOrder.getCorrectMemo())) {
                    answerVO.setCorrectMemo(String.format(correctMemo, correctOrder.getCorrectMemo()));
                    log.info("套卷记录驳回原因:{}", String.format(correctMemo, correctOrder.getCorrectMemo()));
                } else {
                    answerVO.setCorrectMemo(StringUtils.EMPTY);
                }
            }
            if((questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECTING.getBizStatus() || questionAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus()) && questionAnswer.getCorrectMode() != CorrectModeEnum.INTELLIGENCE.getMode()){
                CorrectOrder correctOrder = correctOrderServiceRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(questionAnswer.getId(), EssayAnswerCardEnum.TypeEnum.QUESTION.getType(), EssayStatusEnum.NORMAL.getCode());
                if (null != correctOrder) {
                    answerVO.setClickContent(TeacherOrderTypeEnum.reportContent(TeacherOrderTypeEnum.convert(questionAnswer.getQuestionType()), correctOrder.getDelayStatus()));
                }
            }
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
}
