package com.huatu.tiku.interview.service.impl;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.ChoiceInfo;
import com.huatu.tiku.interview.entity.po.PaperInfo;
import com.huatu.tiku.interview.entity.po.QuestionAnswer;
import com.huatu.tiku.interview.entity.po.QuestionInfo;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;
import com.huatu.tiku.interview.entity.vo.request.QuestionCommitVO;
import com.huatu.tiku.interview.entity.vo.response.ChoiceDetailVO;
import com.huatu.tiku.interview.entity.vo.response.PaperDetailVO;
import com.huatu.tiku.interview.entity.vo.response.QuestionDetailVO;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.repository.impl.QuestionAnswerRepositoryImpl;
import com.huatu.tiku.interview.service.ClassInteractionService;
import com.huatu.tiku.interview.util.CharacterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.huatu.tiku.interview.constant.InterviewErrors.PAPER_NOT_EXIST;

/**
 * Created by x6 on 2018/4/11.
 */
@Service
@Slf4j
public class ClassInteractionServiceImpl implements ClassInteractionService {
    @Autowired
    private QuestionAnswerRepositoryImpl answerRepositoryImpl;
    @Autowired
    PaperInfoRepository paperInfoRepository;
    @Autowired
    QuestionInfoRepository questionInfoRepository;
    @Autowired
    ChoiceInfoRepository choiceInfoRepository;
    @Autowired
    QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    private PaperPushLogRepository paperPushLogRepository;
    @Autowired
    private PaperInfoServiceImpl paperInfoServiceImpl;

    /**
     * 查询试卷详情
     * @param openId
     * @param paperId
     * @return
     */
    @Override
    public Object getPaperDetail(String openId, long paperId) {
        //构建paperVO
        PaperInfo paperInfo = paperInfoRepository.findOne(paperId);
        if(null == paperInfo){
            throw new BizException(PAPER_NOT_EXIST);
        }
        PaperDetailVO.PaperDetailVOBuilder paperBuilder =  PaperDetailVO.builder()
                .paperName(paperInfo.getPaperName())
                .type(paperInfo.getType());
        boolean hasCommitted = false;
        //构建试题列表
        LinkedList<QuestionDetailVO> questionDetailVOList = new LinkedList<>();
        List<QuestionInfo> questionInfoList = questionInfoRepository.findByPaperIdAndStatus
                (paperId, WXStatusEnum.Status.NORMAL.getStatus());
        if(CollectionUtils.isNotEmpty(questionInfoList)){
            LinkedList<Long> questionIds = new LinkedList<>();
            questionInfoList.forEach(i -> {
                questionIds.add(i.getId());
            });
            List<ChoiceInfo> choiceInfoList = choiceInfoRepository.findByQuestionIdInAndStatus(questionIds, WXStatusEnum.Status.NORMAL.getStatus());
            //查询用户答案，填充用户答案,并且update是否已提交字段
            List<QuestionAnswer> answerList = questionAnswerRepository.findByOpenIdAndQuestionIdInAndStatus(openId, questionIds, WXStatusEnum.Status.NORMAL.getStatus());
            if(CollectionUtils.isNotEmpty(answerList)){
                hasCommitted = true;
            }
            for(QuestionInfo questionInfo:questionInfoList){
                //构建选项列表
                LinkedList<ChoiceDetailVO> choiceDetailList = new LinkedList<>();
                QuestionDetailVO questionDetailVO =  QuestionDetailVO.builder()
                        .id(questionInfo.getId())
                        .stem(questionInfo.getStem())
                        .questionType(questionInfo.getQuestionType())
                        .build();
                //填充答案
                List<String> userAnswerList = new LinkedList<>();
                for(QuestionAnswer i:answerList){
                    if(i.getQuestionId() == questionInfo.getId()){
                        String content = i.getContent();
                        String[] split = content.split(",");
                        userAnswerList = Arrays.asList(split);
                        questionDetailVO.setAnswer(i.getContent());
                    }
                }

                for(ChoiceInfo i:choiceInfoList){
                    if(i.getQuestionId() == questionInfo.getId()){
                        ChoiceDetailVO.ChoiceDetailVOBuilder choiceVOBuilder =  ChoiceDetailVO.builder();
                        choiceVOBuilder.id(i.getId())
                                .content(i.getContent())
                                .sort(i.getSort())
                                .questionId(i.getQuestionId())
                                .flag((userAnswerList.contains(""+i.getId()))?true:false);
                        choiceDetailList.add(choiceVOBuilder.build());
                    }
                }
                questionDetailVO.setChoiceList(choiceDetailList);
               
               

                questionDetailVOList.add(questionDetailVO);
            }
        }
        return  paperBuilder.questionList(questionDetailVOList)
                .hasCommitted(hasCommitted)
                .build();

    }

    @Override
    public Result answer(PaperCommitVO vo) {
        String openId = vo.getOpenId();
        List<QuestionCommitVO> questionCommitVOList = vo.getQuestionCommitVOList();
        questionCommitVOList.forEach(i -> {
            QuestionAnswer answer =  QuestionAnswer.builder()
                    .content(( null == i ||StringUtils.isEmpty(i.getAnswer()))?"":CharacterUtil.removeFourChar(i.getAnswer()))
                    .openId(openId)
                    .questionId(i.getQuestionId())
                    .build();
            answer.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            answer.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());
            answer.setCreator(openId);
            answer.setModifier(openId);
            questionAnswerRepository.save(answer);
        });


        return Result.ok();
    }

    @Override
    public Object getPaperDetailV2(String openId, long paperId, long pushId) {
        //构建paperVO
        PaperInfo paperInfo = paperInfoRepository.findOne(paperId);
        if(null == paperInfo){
            throw new BizException(PAPER_NOT_EXIST);
        }
        PaperDetailVO.PaperDetailVOBuilder paperBuilder =  PaperDetailVO.builder()
                .paperName(paperInfo.getPaperName())
                .type(paperInfo.getType());
        boolean hasCommitted = false;
        //构建试题列表
        LinkedList<QuestionDetailVO> questionDetailVOList = new LinkedList<>();
        List<QuestionInfo> questionInfoList = questionInfoRepository.findByPaperIdAndStatus
                (paperId, WXStatusEnum.Status.NORMAL.getStatus());
        if(CollectionUtils.isNotEmpty(questionInfoList)){
            LinkedList<Long> questionIds = new LinkedList<>();
            questionInfoList.forEach(i -> {
                questionIds.add(i.getId());
            });
            List<ChoiceInfo> choiceInfoList = choiceInfoRepository.findByQuestionIdInAndStatus(questionIds, WXStatusEnum.Status.NORMAL.getStatus());
            //查询用户答案，填充用户答案,并且update是否已提交字段
            List<QuestionAnswer> answerList = questionAnswerRepository.findByOpenIdAndPushIdAndStatus(openId, pushId, WXStatusEnum.Status.NORMAL.getStatus());
            if(CollectionUtils.isNotEmpty(answerList)){
                hasCommitted = true;
            }
            for(QuestionInfo questionInfo:questionInfoList){
                //构建选项列表
                LinkedList<ChoiceDetailVO> choiceDetailList = new LinkedList<>();
                QuestionDetailVO questionDetailVO =  QuestionDetailVO.builder()
                        .id(questionInfo.getId())
                        .stem(questionInfo.getStem())
                        .questionType(questionInfo.getQuestionType())
                        .build();
                //填充答案
                List<String> userAnswerList = new LinkedList<>();
                for(QuestionAnswer i:answerList){
                    if(i.getQuestionId() == questionInfo.getId()){
                        String content = i.getContent();
                        String[] split = content.split(",");
                        userAnswerList = Arrays.asList(split);
                        questionDetailVO.setAnswer(i.getContent());
                    }
                }

                for(ChoiceInfo i:choiceInfoList){
                    if(i.getQuestionId() == questionInfo.getId()){
                        ChoiceDetailVO.ChoiceDetailVOBuilder choiceVOBuilder =  ChoiceDetailVO.builder();
                        choiceVOBuilder.id(i.getId())
                                .content(i.getContent())
                                .sort(i.getSort())
                                .questionId(i.getQuestionId())
                                .flag((userAnswerList.contains(""+i.getId()))?true:false);
                        choiceDetailList.add(choiceVOBuilder.build());
                    }
                }
                questionDetailVO.setChoiceList(choiceDetailList);
                questionDetailVOList.add(questionDetailVO);
            }
        }
        return  paperBuilder.questionList(questionDetailVOList)
                .hasCommitted(hasCommitted)
                .build();

    }

    @Override
    public Result answerV2(PaperCommitVO vo) {
        String openId = vo.getOpenId();
        long pushId = vo.getPushId();
        List<QuestionCommitVO> questionCommitVOList = vo.getQuestionCommitVOList();
        questionCommitVOList.forEach(i -> {
            QuestionAnswer answer = QuestionAnswer.builder()
                    .content(( null == i ||StringUtils.isEmpty(i.getAnswer()))?"":CharacterUtil.removeFourChar(i.getAnswer()))
                    .openId(openId)
                    .questionId(i.getQuestionId())
                    .pushId(pushId)
                    .build();
            answer.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            answer.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());
            answer.setCreator(openId);
            answer.setModifier(openId);
            questionAnswerRepository.save(answer);
        });


        return Result.ok();
    }

    @Override
    public Object getPaperDetailV3(String openId, long paperId, long pushId) {
        Object paperDetail = getPaperDetailV2(openId, paperId, pushId);
        //统计所有用户作答情况

        return null;
    }

}
